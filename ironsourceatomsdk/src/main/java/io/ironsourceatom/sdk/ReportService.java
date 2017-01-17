package io.ironsourceatom.sdk;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import static io.ironsourceatom.sdk.ReportService.HandleStatus.FLUSH_INTERVAL;
import static io.ironsourceatom.sdk.ReportService.HandleStatus.HANDLED;
import static io.ironsourceatom.sdk.ReportService.HandleStatus.RETRY;
import static java.lang.Math.ceil;

/**
 * Intent service to handle tracker functionality
 */
public class ReportService
		extends IntentService {

	private static final String TAG = "ReportService";

	private   NetworkManager   networkManager;
	private   StorageApi       storage;
	private   RemoteConnection client;
	private   IsaConfig        config;
	protected BackOff          backOff;

	public enum SendStatus {
		SUCCESS,
		DELETE,
		RETRY
	}

	public enum HandleStatus {
		HANDLED,
		RETRY,
		FLUSH_INTERVAL
	}

	public ReportService() {
		super(TAG);
	}

	@Override
	public void onCreate() {
		super.onCreate();
		init(getApplicationContext());
	}

	void init(Context context) {
		backOff = BackOff.getInstance(context);
		client = getClient();
		config = getConfig(context);
		storage = getStorage(context);
		networkManager = getNetManager(context);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		try {
			final HandleStatus status = handleReport(intent);
			switch (status) {
				case RETRY:
					retrySendReport();
					break;
				case FLUSH_INTERVAL:
					scheduleAlarm(System.currentTimeMillis() + config.getFlushInterval());
					// Intentional fall-through
				case HANDLED:
					backOff.reset();
					break;
			}
		} catch (Throwable th) {
			Logger.log(TAG, "Failed to handle intent: " + th, th, Logger.SDK_ERROR);
		}
	}

	/**
	 * handleReport responsible to handle the given ReportIntent based on the
	 * event-type(that could be one of the 3: FLUSH, ENQUEUE or POST_SYNC).
	 *
	 * @param intent
	 * @return result of the handleReport if success true or failed false
	 */
	public HandleStatus handleReport(Intent intent) {
		HandleStatus status = HANDLED;
		try {
			if (intent == null || intent.getExtras() == null) {
				Logger.log(TAG, "Failed to handle intent - is null or no extras", Logger.SDK_DEBUG);
				return status;
			}
			final Bundle extras = intent.getExtras();
			final JSONObject dataObject = new JSONObject();
			try {
				String[] fields = {ReportData.TABLE, ReportData.TOKEN, ReportData.DATA};
				for (String key : fields) {
					Object value = extras.get(key);
					dataObject.put(key, value);
				}
			} catch (JSONException e) {
				Logger.log(TAG, "Failed extracting the data from Intent", Logger.SDK_DEBUG);
			}

			final boolean connectedToValidNetwork = networkManager.isOnline() && canUseNetwork();
			List<StorageApi.Table> tablesToFlush = new ArrayList<>();
			final int sdkEvent = extras.getInt(ReportData.EXTRA_SDK_EVENT, SdkEvent.ERROR);
			switch (sdkEvent) {
				case SdkEvent.FLUSH_QUEUE:
					Logger.log(TAG, "Requested to flush database", Logger.SDK_DEBUG);
					if (connectedToValidNetwork) {
						tablesToFlush = storage.getTables();
						break;
					}
					Logger.log(TAG, "Device is offline or cannot use network", Logger.SDK_DEBUG);

					return HandleStatus.RETRY;
				case SdkEvent.POST_SYNC:
				case SdkEvent.REPORT_ERROR:
					if (connectedToValidNetwork) {
						final String message = createMessage(dataObject, false);
						final String url = config.getAtomEndPoint(dataObject.getString(ReportData.TOKEN));
						final SendStatus sendStatus = send(message, url);
						if (sendStatus != SendStatus.RETRY || sdkEvent == SdkEvent.REPORT_ERROR) {
							break;
						}
					}
					// Intentional fall-through
				case SdkEvent.ENQUEUE:
					final StorageApi.Table table = new StorageApi.Table(dataObject.getString(ReportData.TABLE), dataObject.getString(ReportData.TOKEN));
					final int nRows = storage.addEvent(table, dataObject.getString(ReportData.DATA));
					Logger.log(TAG, "Added event to " + table + " table (size: " + nRows + " rows)", Logger.SDK_DEBUG);
					if (connectedToValidNetwork && config.getBulkSize() <= nRows) {
						Logger.log(TAG, "Exceeded configured bulk size (" + config.getBulkSize() + " rows) - flushing data", Logger.SDK_DEBUG);
						tablesToFlush.add(table);
					}
					else {
						// Wait for flush interval or retry on valid network
						return connectedToValidNetwork ? FLUSH_INTERVAL : RETRY;
					}
			}
			// If there's something to flush, it'll not be empty.
			for (StorageApi.Table table : tablesToFlush)
				flush(table);
		} catch (Exception e) {
			status = HandleStatus.RETRY;
			Logger.log(TAG, e.getMessage(), Logger.SDK_DEBUG);
		}

		return status;
	}

	/**
	 * First, we peek the batch the fits with the `MaximumRequestLimit`
	 * after that we prepare the request and send it.
	 * if the send failed, we stop here, and "continue later".
	 * if everything goes-well, we do it recursively until wil drain and
	 * delete the table.
	 *
	 * @param table
	 * @throws Exception
	 */
	public void flush(StorageApi.Table table) throws
			Exception {
		int bulkSize = config.getBulkSize();
		StorageApi.Batch batch;
		while (true) {
			batch = storage.getEvents(table, bulkSize);
			if (batch != null && batch.events.size() > 1) {
				int byteSize = batch.events.toString()
				                           .getBytes("UTF-8").length;
				if (byteSize <= config.getMaximumRequestLimit()) {
					break;
				}
				bulkSize = (int) (bulkSize / ceil(byteSize / config.getMaximumRequestLimit()));
			}
			else {
				break;
			}
		}
		if (batch != null) {
			final JSONObject event = new JSONObject();
			event.put(ReportData.TABLE, table.name);
			event.put(ReportData.TOKEN, table.token);
			event.put(ReportData.DATA, batch.events.toString());

			Logger.log(TAG, "Sending " + batch.events.size() + " rows of " + table.name + " to server...", Logger.SDK_DEBUG);
			final SendStatus res = send(createMessage(event, true), config.getAtomBulkEndPoint(table.token));

			if (res == SendStatus.RETRY) {
				// This will be caught by handleReport() and return a HandleStatus.RETRY
				throw new IllegalStateException("Failed flush entries for table: " + table.name);
			}

			if (storage.deleteEvents(table, batch.lastId) < bulkSize || storage.count(table) == 0) {
				storage.deleteTable(table);
			}
			else {
				flush(table);
			}
		}
	}

	/**
	 * Prepare the giving object before sending it to IronSourceAtom(Do auth, etc..)
	 *
	 * @param obj  - the given event to working on.
	 * @param bulk - indicate if it need to add a bulk field.
	 * @return
	 */
	private String createMessage(JSONObject obj, boolean bulk) {
		String message = "";
		try {
			final JSONObject clone = new JSONObject(obj.toString());
			final String data = clone.getString(ReportData.DATA);
			if (!clone.getString(ReportData.TOKEN)
			          .isEmpty()) {
				clone.put(ReportData.AUTH, Utils.auth(data, (String) clone.remove(ReportData.TOKEN)));
			}
			else {
				clone.remove(ReportData.TOKEN);
			}
			if (bulk) {
				clone.put(ReportData.BULK, true);
			}
			message = clone.toString();
		} catch (Exception e) {
			Logger.log(TAG, "Failed to create message" + e, Logger.SDK_DEBUG);
		}
		return message;
	}

	/**
	 * @param data - Stringified JSON. used as a request body.
	 * @param url  - IronSourceAtomFactory url endpoint.
	 * @return sendStatus ENUM that indicate what to do later on.
	 */
	protected SendStatus send(String data, String url) {
		int nRetry = config.getNumOfRetries();

		Logger.log(TAG, "Tracking data:" + data, Logger.SDK_DEBUG);
		while (nRetry-- > 0) {
			try {
				RemoteConnection.Response response = client.post(data, url);
				if (response.code == HttpURLConnection.HTTP_OK) {
					Logger.log(TAG, "Server Response: HTTP " + response.code, Logger.SDK_DEBUG);
					return SendStatus.SUCCESS;
				}
				if (response.code >= HttpURLConnection.HTTP_BAD_REQUEST && response.code < HttpURLConnection.HTTP_INTERNAL_ERROR) {
					Logger.log(TAG, "Server Response: HTTP " + response.code, Logger.SDK_DEBUG);
					//  PENDING: Are we sure we want to DELETE when getting here? What about temporary 404?
					return SendStatus.DELETE;
				}
			} catch (SocketTimeoutException | UnknownHostException | SocketException e) {
				Logger.log(TAG, "Connectivity error: " + e, Logger.SDK_DEBUG);
			} catch (IOException e) {
				Logger.log(TAG, "Service IronSourceAtomFactory is unavailable: " + e, Logger.SDK_DEBUG);
			}
		}
		return SendStatus.RETRY;
	}

	/**
	 * Check if the handler can use the network.
	 *
	 * @return
	 */
	private boolean canUseNetwork() {
		if ((config.getAllowedNetworkTypes() & networkManager.getNetworkAtomType()) == 0) {
			return false;
		}

		return config.isAllowedOverRoaming() || !networkManager.isDataRoamingEnabled();
	}

	private void retrySendReport() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			retryWithJobScheduler();
		}
		else {
			Logger.log(TAG, "Job Scheduler not supported - Scheduling retry with AlarmManager", Logger.SDK_DEBUG);
			retryWithAlarmManager();
		}
	}

	/**
	 * On Android Nougat: use JobScheduler<BR>
	 * On Android Marshmallow/Lollipop: use JobScheduler only when roaming is not a consideration
	 */
	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	private void retryWithJobScheduler() {
		final boolean allowedOverRoaming = config.isAllowedOverRoaming();
		final boolean allowedOverMobileData = (config.getAllowedNetworkTypes() & IronSourceAtomFactory.NETWORK_MOBILE) != 0;
		if (Utils.isBackgroundDataRestricted(this)) {
			// We can't use JobScheduler when bg data is restricted since it will instantly start the job
			Logger.log(TAG, "Can't reschedule with JobScheduler since background data is restricted - using AlarmManager", Logger.SDK_DEBUG);
			retryWithAlarmManager();
		}
		else if (networkManager.isDataRoamingEnabled() && allowedOverMobileData && Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
			// We'll get here in the case where the device is pre-Nougat (no NETWORK_TYPE_ROAMING) and we're roaming while it's not allowed
			Logger.log(TAG, "Can't reschedule with JobScheduler - type 'not roaming' cannot be enforced on Android Marshmallow JobScheduler", Logger.SDK_DEBUG);
		}
		else {
			// Prepare JobScheduler
			JobInfo.Builder builder = new JobInfo.Builder(ReportJobService.JOB_ID, new ComponentName(this, ReportJobService.class));
			int desiredNetwork;
			if (!allowedOverRoaming && !allowedOverMobileData) {
				desiredNetwork = JobInfo.NETWORK_TYPE_UNMETERED;
			}
			else if (allowedOverMobileData && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
				// Introduced in Android N
				desiredNetwork = JobInfo.NETWORK_TYPE_NOT_ROAMING;
			}
			else {
				desiredNetwork = JobInfo.NETWORK_TYPE_ANY;
			}

			// Use only desired network
			builder.setRequiredNetworkType(desiredNetwork);
			if (checkCallingOrSelfPermission("android.permission.RECEIVE_BOOT_COMPLETED") == PackageManager.PERMISSION_GRANTED) {
				// Survive reboots
				builder.setPersisted(true);
			}
			// Retry anyway after 24 hours
			builder.setOverrideDeadline(24 * 60 * 60 * 1000);

			// Schedule with JobScheduler
			JobScheduler scheduler = (JobScheduler) this.getSystemService(Context.JOB_SCHEDULER_SERVICE);
			int result = scheduler.schedule(builder.build());
			if (result == JobScheduler.RESULT_SUCCESS) {
				Logger.log(TAG, "Successfully scheduled with JobScheduler (Required network = " + desiredNetwork + ")...", Logger.SDK_DEBUG);
			}
			else {
				// Should never happen
				Logger.log(TAG, "Failed to schedule with JobScheduler - bad parameters supplied to builder(Required network = " + desiredNetwork + ")...", Logger.SDK_DEBUG);
				retryWithAlarmManager();
			}
		}
	}

	private void retryWithAlarmManager() {
		if (backOff.hasNext()) {
			boolean backoffExpired = backOff.getNextBackoffTime() < System.currentTimeMillis();
			if (backoffExpired) {
				scheduleAlarm(backOff.next());
			}
			else {
				final long backoffSecondsLeft = (backOff.getNextBackoffTime() - System.currentTimeMillis()) / 1000;
				Logger.log(TAG, "Backoff not yet expired (" + backoffSecondsLeft + " seconds left)  - no need to reschedule alarm", Logger.SDK_DEBUG);
			}
		}
		else {
			Logger.log(TAG, "Reached max retry attempts", Logger.SDK_DEBUG);
			backOff.reset();
		}
	}

	private void scheduleAlarm(long epochTime) {
		final long delayInMillis = epochTime - System.currentTimeMillis();
		Logger.log(TAG, "Scheduling to retry flushing data in " + (delayInMillis / 1000) + " seconds...", Logger.SDK_DEBUG);
		final Intent reportIntent = new Intent(this, ReportService.class);
		reportIntent.putExtras(new ReportData(SdkEvent.FLUSH_QUEUE).getExtras());

		final AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		alarmManager.set(AlarmManager.RTC, epochTime, PendingIntent.getService(this, 0, reportIntent, PendingIntent.FLAG_UPDATE_CURRENT));
	}

	/**
	 * For testing purpose. to allow mocking this behavior.
	 */
	protected RemoteConnection getClient() {
		return HttpClient.getInstance();
	}

	protected IsaConfig getConfig(Context context) {
		return IsaConfig.getInstance(context);
	}

	protected StorageApi getStorage(Context context) {
		return DbAdapter.getInstance(context);
	}

	protected NetworkManager getNetManager(Context context) {
		return NetworkManager.getInstance(context);
	}


	////////////////////////////////////////////////////////////////////////////////////////////

	public static void sendReport(Context context, Report report) {

		// PENDING: Might be better to save to DB before starting the intent service

		final Intent intent = new Intent(context, ReportService.class);
		intent.putExtras(report.getExtras());
		context.startService(intent);
	}
}