package io.ironsourceatom.sdk;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import org.json.JSONObject;


/**
 * Created on 19/01/2017 16:33.
 */
public class ReportService
		extends IntentService {

	private static final String TAG = "ReportService";

	private static final String PACKAGE = FlushDatabaseService.class.getPackage()
	                                                                .getName();

	public static final String EXTRA_REPORT_JSON                = PACKAGE + ".EXTRA_REPORT_JSON";
	public static final String EXTRA_REPORT_ACTION_ENUM_ORDINAL = PACKAGE + ".EXTRA_REPORT_ACTION_ENUM_ORDINAL";

	private IsaConfig  config;
	private StorageApi storage;

	public ReportService() {
		super(TAG);
	}

	@Override
	public void onCreate() {
		super.onCreate();
		setIntentRedelivery(true);
		init(getApplicationContext());
	}

	void init(Context context) {
		config = getConfig(context);
		storage = getStorage(context);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		if (intent == null || intent.getExtras() == null || intent.getStringExtra(EXTRA_REPORT_JSON) == null || intent.getStringExtra(EXTRA_REPORT_ACTION_ENUM_ORDINAL) == null) {
			Logger.log(TAG, "Intent is null, no extras, missing extras or extras are null - exiting", Logger.SDK_ERROR);
			return;
		}

		try {
			// Intent returned null here on some rare cases resulting in an NPE
			final JSONObject reportJsonObject = new JSONObject(intent.getStringExtra(EXTRA_REPORT_JSON));
			final Report.Action action = Report.Action.values()[intent.getExtras()
			                                                          .getInt(EXTRA_REPORT_ACTION_ENUM_ORDINAL, -1)];

			handleReport(reportJsonObject, action);
		} catch (Exception e) {
			Logger.log(TAG, "Failed to create report from json - exiting", Logger.SDK_ERROR);
		}
	}

	void handleReport(JSONObject reportJsonObject, Report.Action action) {
		final boolean exceededBulkSize = saveReport(reportJsonObject);
		final boolean flushNow = exceededBulkSize || action == Report.Action.POST_SYNC;
		flushDatabase(flushNow ? 0 : config.getFlushInterval());
	}

	private boolean saveReport(JSONObject dataObject) {
		final StorageApi.Table table = new StorageApi.Table(dataObject.optString(Report.TABLE_KEY), dataObject.optString(Report.TOKEN_KEY));
		final int dbRowCount = storage.addEvent(table, dataObject.optString(Report.DATA_KEY));
		boolean addSuccessful = dbRowCount != -1;
		if (addSuccessful) {
			Logger.log(TAG, "Added event to " + table.name + " table (size: " + dbRowCount + " rows)", Logger.SDK_DEBUG);
			if (storage.countAll() == config.getBulkSize()) {
				Logger.log(TAG, "Reached configured bulk size (" + config.getBulkSize() + " rows) - flushing database", Logger.SDK_DEBUG);
				return true;
			}
		}
		else {
			Logger.log(TAG, "Failed to add event to " + table + " table", Logger.SDK_DEBUG);
		}

		// No need to flushTable yet
		return false;
	}

	//////////////////// Allow overriding for testing purposes - to allow mocking this behavior /////////////////////

	protected void flushDatabase(long delay) {
		final long flushAtEpoch = System.currentTimeMillis() + delay;
		FlushDatabaseService.flushDatabase(this, flushAtEpoch);
	}

	protected IsaConfig getConfig(Context context) {
		return IsaConfig.getInstance(context);
	}

	protected StorageApi getStorage(Context context) {
		return DbAdapter.getInstance(context);
	}

	////////////////////////////////////////////////////////////////////////////////////////////

	public static void report(Context context, Report report, Report.Action reportAction) {
		final Intent intent = new Intent(context, ReportService.class);
		intent.putExtra(EXTRA_REPORT_ACTION_ENUM_ORDINAL, reportAction.ordinal());
		final String reportAsJsonString = report.asJsonString();
		if (reportAsJsonString != null) {
			intent.putExtra(EXTRA_REPORT_JSON, reportAsJsonString);
			context.startService(intent);
		}
		else {
			// Trying to catch a corner case where the extra report json is received as null inside the service
			final String streamName = report.getJsonObject()
			                                .optString(Report.TABLE_KEY);
			final String data = report.getJsonObject()
			                          .optString(Report.DATA_KEY);
			Logger.log(TAG, "Failed to convert Report to json using JSONObject.toString(). stream = " + streamName + ", data = " + data, Logger.SDK_ERROR);
		}
	}
}