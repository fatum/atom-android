package io.ironsourceatom.sdk;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
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

import static java.lang.Math.ceil;

/**
 * Intent service to handle tracker functionality
 */
public class ReportService
		extends IntentService {

	private static final String TAG = "ReportService";

	private NetworkManager   networkManager;
	private StorageApi       storage;
	private RemoteConnection client;
	private IsaConfig        config;
	protected BackOff       backOff;

	public enum SendStatus {
		SUCCESS,
		DELETE,
		RETRY
	}

	public enum HandleStatus {
		HANDLED,
		RETRY
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
			if (status == HandleStatus.RETRY && backOff.hasNext()) {
				setAlarm(backOff.next());
			}
			else {
				backOff.reset();
			}
		} catch (Throwable th) {
			Logger.log(TAG, "failed to handle intent: " + th, th, Logger.SDK_ERROR);
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
		HandleStatus status = HandleStatus.HANDLED;
		boolean isOnline = networkManager.isOnline() && canUseNetwork();
		try {
			if (intent==null || intent.getExtras() == null) {
				Logger.log(TAG, "Failed to handle intent - null or no extras", Logger.SDK_DEBUG);
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
			List<StorageApi.Table> tablesToFlush = new ArrayList<>();
			final int sdkEvent = extras.getInt(ReportData.EXTRA_SDK_EVENT, SdkEvent.ERROR);
			switch (sdkEvent) {
				case SdkEvent.FLUSH_QUEUE:
					if (isOnline) {
						tablesToFlush = storage.getTables();
						break;
					}
					return HandleStatus.RETRY;
				case SdkEvent.POST_SYNC:
				case SdkEvent.REPORT_ERROR:
					if (isOnline) {
						final String message = createMessage(dataObject, false);
						final String url = config.getAtomEndPoint(dataObject.getString(ReportData.TOKEN));
						final SendStatus sendStatus = send(message, url);
						if (sendStatus != SendStatus.RETRY || sdkEvent == SdkEvent.REPORT_ERROR) {
							break;
						}
					}
				case SdkEvent.ENQUEUE:
					final StorageApi.Table table = new StorageApi.Table(dataObject.getString(ReportData.TABLE), dataObject.getString(ReportData.TOKEN));
					final int nRows = storage.addEvent(table, dataObject.getString(ReportData.DATA));
					if (isOnline && config.getBulkSize() <= nRows) {
						tablesToFlush.add(table);
					}
					else {
						return HandleStatus.RETRY;
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

			final SendStatus res = send(createMessage(event, true), config.getAtomBulkEndPoint(table.token));

			if (res == SendStatus.RETRY) {
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
					Logger.log(TAG, "Server Response Status: " + response.code, Logger.SDK_DEBUG);
					return SendStatus.SUCCESS;
				}
				if (response.code >= HttpURLConnection.HTTP_BAD_REQUEST && response.code < HttpURLConnection.HTTP_INTERNAL_ERROR) {
					Logger.log(TAG, "Server Response Status: " + response.code, Logger.SDK_DEBUG);
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
	 * Test if the handler can use the network.
	 *
	 * @return
	 */
	private boolean canUseNetwork() {
		if ((config.getAllowedNetworkTypes() & networkManager.getNetworkAtomType()) == 0) {
			return false;
		}
		return config.isAllowedOverRoaming() || !networkManager.isDataRoamingEnabled();
	}


	protected void setAlarm(long delayInMillis) {
		Logger.log(TAG, "Setting alarm, Will send in: " + (delayInMillis - backOff.currentTimeMillis()) + "ms", Logger.SDK_DEBUG);
		final Intent reportIntent = new Intent(this, ReportService.class);
		reportIntent.putExtras(new ReportData(SdkEvent.FLUSH_QUEUE).getExtras());
		final PendingIntent intent = PendingIntent.getService(this, 0, reportIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		// PENDING: The call to cancel() here might cause pending alarms to stop and lose event!
		alarmManager.cancel(intent);
		alarmManager.set(AlarmManager.RTC, delayInMillis, intent);
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

}