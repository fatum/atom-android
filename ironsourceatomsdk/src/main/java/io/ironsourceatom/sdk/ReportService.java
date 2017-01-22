package io.ironsourceatom.sdk;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

/**
 * Created on 19/01/2017 16:33.
 */

public class ReportService
		extends IntentService {

	private static final String TAG = "SaveReportService";

	public ReportService() {
		super(TAG);
	}

	@Override
	public void onCreate() {
		super.onCreate();
		setIntentRedelivery(true);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		saveReport(intent);
	}

	private void saveReport(Intent intent) {
		//		try {
		//			final JSONObject dataObject = SendReportService.getDataFromIntent(intent);
		//			final StorageApi.Table table = new StorageApi.Table(dataObject.getString(Report.TABLE), dataObject.getString(Report.TOKEN));
		//			final int dbRowCount = getStorage(this).addEvent(table, dataObject.getString(Report.DATA));
		//			Logger.log(TAG, "Added event to " + table + " table (size: " + dbRowCount + " rows)", Logger.SDK_DEBUG);
		//			if (dbRowCount > config.getBulkSize() && connectedToValidNetwork) {
		//				Logger.log(TAG, "Exceeded configured bulk size (" + config.getBulkSize() + " rows) - flushing data", Logger.SDK_DEBUG);
		//				flush(table);
		//			}
		//			else {
		//				// Wait for flush interval or retry on valid network
		//				return connectedToValidNetwork ? SendReportService.HandleStatus.FLUSH_INTERVAL : SendReportService.HandleStatus.RETRY;
		//			}
		//		}
	}

	protected StorageApi getStorage(Context context) {
		return DbAdapter.getInstance(context);
	}

	////////////////////////////////////////////////////////////////////////////////////////////

	public static void report(Context context, Report report, Report.Action reportAction) {

	}
}