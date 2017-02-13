package io.ironsourceatom.trackertest;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

<<<<<<< HEAD
import io.ironsourceatom.sdk.DbAdapter;
import io.ironsourceatom.sdk.FlushDatabaseService;
import io.ironsourceatom.sdk.IronSourceAtomFactory;
import io.ironsourceatom.sdk.IronSourceAtomTracker;
import io.ironsourceatom.sdk.IsaConfig;
import io.ironsourceatom.sdk.RemoteConnection;
import io.ironsourceatom.sdk.ReportService;
import io.ironsourceatom.sdk.StorageApi;
=======
import io.ironsourceatom.sdk.IronSourceAtomFactory;
import io.ironsourceatom.sdk.IronSourceAtomTracker;
import io.ironsourceatom.sdk.IsaConfig;
>>>>>>> 8225b5074630b7f2184a1cc59f36a82074d3dce3

public class MainActivity
		extends Activity {

	protected static final String TAG = "TrackerTest";

	protected Boolean isFlushCalled        = false;
	protected Boolean isHandleReportCalled = false;

<<<<<<< HEAD
	protected int currentAndroidAPIVersion = Build.VERSION_CODES.LOLLIPOP;

	public void setHandlerFlushed(Boolean isFlushCalled) {
		this.isFlushCalled = isFlushCalled;
	}

	public void setHandlerReported(Boolean isHandleReportCalled) {
		this.isHandleReportCalled = isHandleReportCalled;
	}

	public int getAndroidAPIVersion() {
		return currentAndroidAPIVersion;
	}

=======
>>>>>>> 8225b5074630b7f2184a1cc59f36a82074d3dce3
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final TestsUtils.MockPoster httpClient = new TestsUtils.MockPoster();
<<<<<<< HEAD
		final StorageApi dbAdapter = new DbAdapter(this);
		final IsaConfig isaConfig = IsaConfig.getInstance(this);

		final FlushDatabaseService flushDatabaseService = new FlushDatabaseService() {
			@Override
			protected RemoteConnection getHttpClient() {
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
				if(delay==0) {
					//flushDatabaseService.flushDatabase();
				}
			}
		};


=======
		final IsaConfig isaConfig = IsaConfig.getInstance(this);
>>>>>>> 8225b5074630b7f2184a1cc59f36a82074d3dce3
		isaConfig.setBulkSize(3);
		isaConfig.setFlushInterval(1000);

		String authKey = "";
<<<<<<< HEAD
		final IronSourceAtomTracker tracker = new IronSourceAtomTracker(this, authKey);

		IronSourceAtomFactory trackerFactory = IronSourceAtomFactory.getInstance(this);
		IronSourceAtomTracker t = trackerFactory.newTracker("");
=======

		IronSourceAtomFactory trackerFactory = IronSourceAtomFactory.getInstance(this);
		trackerFactory.setHttpClient(httpClient);
		final IronSourceAtomTracker tracker = trackerFactory.newTracker(authKey);
>>>>>>> 8225b5074630b7f2184a1cc59f36a82074d3dce3

		//Logger.setPrintErrorStackTrace(true);
		isaConfig.enableErrorReporting();

		// Check immediately track API 21
<<<<<<< HEAD
		currentAndroidAPIVersion = Build.VERSION_CODES.LOLLIPOP;
		tracker.track("TRACKNOW_API21", "N1", true);

		currentAndroidAPIVersion = Build.VERSION_CODES.JELLY_BEAN;
		tracker.track("TRACKNOW_API18", "N1", true);

		currentAndroidAPIVersion = Build.VERSION_CODES.LOLLIPOP;
		tracker.track("TRACK_BULK_SIZE_API21", "S1");
		tracker.track("TRACK_BULK_SIZE_API21", "S2");
		tracker.track("TRACK_BULK_SIZE_API21", "S3");

		currentAndroidAPIVersion = Build.VERSION_CODES.LOLLIPOP;
		tracker.track("TRACK_TIMER_API21", "T1");

		currentAndroidAPIVersion = Build.VERSION_CODES.JELLY_BEAN;
		tracker.track("TRACK_TIMER_API18", "T2");

		// 400 error
		currentAndroidAPIVersion = Build.VERSION_CODES.LOLLIPOP;
		tracker.track("TRACK_400_ERROR_API21", "E1");

		currentAndroidAPIVersion = Build.VERSION_CODES.LOLLIPOP;
=======
		tracker.track("TRACKNOW_API21", "N1", true);
		tracker.track("TRACK_BULK_SIZE_API21", "S1");
		tracker.track("TRACK_BULK_SIZE_API21", "S2");
		tracker.track("TRACK_BULK_SIZE_API21", "S3");
		tracker.track("TRACK_TIMER_API21", "T1");
		tracker.track("TRACK_400_ERROR_API21", "E1");
>>>>>>> 8225b5074630b7f2184a1cc59f36a82074d3dce3
		tracker.track("TRACK_503_ERROR_API21", "ER1");

		Thread thread = new Thread() {
			@Override
			public void run() {
				try {
					sleep(20000);

					if (!TestsUtils.MockPoster.isAllTrackerTasksCompleted()) {
						TestsUtils.MockPoster.printTrackerTaskStatus();

<<<<<<< HEAD
						Log.e(TAG, "Can't done all tasks!");
=======
						Log.e(TAG, "Test error, not all tasks completed");
>>>>>>> 8225b5074630b7f2184a1cc59f36a82074d3dce3
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
