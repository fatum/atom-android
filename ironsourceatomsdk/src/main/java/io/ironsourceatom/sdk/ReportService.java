package io.ironsourceatom.sdk;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import org.json.JSONException;
import org.json.JSONObject;

import static io.ironsourceatom.sdk.Report.Action.REPORT_ERROR;

/**
 * Created on 19/01/2017 16:33.
 */

public class ReportService
		extends IntentService {

	private static final String TAG = "SaveReportService";

	private static final String PACKAGE = FlushDatabaseService.class.getPackage()
	                                                                .getName();

	static final String EXTRA_REPORT_JSON                = PACKAGE + ".EXTRA_REPORT_JSON";
	static final String EXTRA_REPORT_ACTION_ENUM_ORDINAL = PACKAGE + ".EXTRA_REPORT_ACTION_ENUM_ORDINAL";

	private NetworkManager networkManager;
	private StorageApi     storage;

	private IsaConfig config;

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
		networkManager = getNetManager(context);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		if (intent == null || intent.getExtras() == null) {
			Logger.log(TAG, "Intent is null or no extras - exiting", Logger.SDK_DEBUG);
			return;
		}


		try {
			final JSONObject reportJsonObject = new JSONObject(intent.getStringExtra(EXTRA_REPORT_JSON));

			saveReport(reportJsonObject);


		} catch (JSONException e) {
			Logger.log(TAG, "Failed to create report from json - exiting", Logger.SDK_DEBUG);
			return;
		}


		final Report.Action action = Report.Action.values()[intent.getExtras()
		                                                          .getInt(EXTRA_REPORT_ACTION_ENUM_ORDINAL, REPORT_ERROR.ordinal())];
		switch(action) {
			case ERROR:
			case POST_SYNC:
				FlushDatabaseService.flush(this);
				break;
			case REPORT_ERROR:
		}
	}

	private void saveReport(JSONObject dataObject) {
		final StorageApi.Table table = new StorageApi.Table(dataObject.optString(Report.TABLE_KEY), dataObject.optString(Report.TOKEN_KEY));
		final int dbRowCount = getStorage(this).addEvent(table, dataObject.optString(Report.DATA_KEY));
		Logger.log(TAG, "Added event to " + table + " table (size: " + dbRowCount + " rows)", Logger.SDK_DEBUG);
		if (dbRowCount > config.getBulkSize()) {
			Logger.log(TAG, "Exceeded configured bulk size (" + config.getBulkSize() + " rows) - flushing data", Logger.SDK_DEBUG);
			FlushDatabaseService.flush(this);
		}
	}

	//////////////////// For testing purpose - to allow mocking this behavior /////////////////////

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

	public static void report(Context context, Report report, Report.Action reportAction) {
		final Intent intent = new Intent(context, FlushDatabaseService.class);
		intent.putExtra(EXTRA_REPORT_ACTION_ENUM_ORDINAL, reportAction.ordinal());
		intent.putExtra(EXTRA_REPORT_JSON, report.asJsonString());
		context.startService(intent);
	}
}