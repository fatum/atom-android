package io.ironsourceatom.trackertest;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import io.ironsourceatom.sdk.DbAdapter;
import io.ironsourceatom.sdk.FlushDatabaseService;
import io.ironsourceatom.sdk.HttpClient;
import io.ironsourceatom.sdk.IronSourceAtomFactory;
import io.ironsourceatom.sdk.IronSourceAtomTracker;
import io.ironsourceatom.sdk.IsaConfig;
import io.ironsourceatom.sdk.ReportService;
import io.ironsourceatom.sdk.StorageApi;

public class MainActivity
		extends Activity {

	protected static final String TAG = "TrackerTest";

	protected Boolean isFlushCalled        = false;
	protected Boolean isHandleReportCalled = false;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final TestsUtils.MockPoster httpClient = new TestsUtils.MockPoster();
		final StorageApi dbAdapter = new DbAdapter(this);
		final IsaConfig isaConfig = IsaConfig.getInstance(this);

		final FlushDatabaseService flushDatabaseService = new FlushDatabaseService() {
			@Override
			protected HttpClient getHttpClient() {
				return httpClient;
			}
		};


		ReportService reportHandler = new ReportService() {
			@Override
			protected StorageApi getStorage(Context context) {
				return dbAdapter;
			}

			@Override
			protected IsaConfig getConfig(Context context) {
				return isaConfig;
			}

			@Override
			protected void flushDatabase(long delay) {
				if (delay == 0) {
					//flushDatabaseService.flushDatabase();
				}
			}
		};


		isaConfig.setBulkSize(3);
		isaConfig.setFlushInterval(1000);

		String authKey = "";


		IronSourceAtomFactory trackerFactory = IronSourceAtomFactory.getInstance(this);
		trackerFactory.setHttpClient(httpClient);
		final IronSourceAtomTracker tracker = trackerFactory.newTracker(authKey);

		//Logger.setPrintErrorStackTrace(true);
		isaConfig.enableErrorReporting();

		// Check immediately track API 21

		tracker.track("TRACKNOW_API21", "N1", true);
		tracker.track("TRACK_BULK_SIZE_API21", "S1");
		tracker.track("TRACK_BULK_SIZE_API21", "S2");
		tracker.track("TRACK_BULK_SIZE_API21", "S3");
		tracker.track("TRACK_TIMER_API21", "T1");
		tracker.track("TRACK_400_ERROR_API21", "E1");
		tracker.track("TRACK_503_ERROR_API21", "ER1");

		Thread thread = new Thread() {
			@Override
			public void run() {
				try {
					sleep(20000);

					if (!TestsUtils.MockPoster.isAllTrackerTasksCompleted()) {
						TestsUtils.MockPoster.printTrackerTaskStatus();

						Log.e(TAG, "Test error, not all tasks completed");
					}
					else {
						Log.i(TAG, "All tasks done!");
					}

					// System.exit(0);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		};

		thread.start();
	}
}