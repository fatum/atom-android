package io.ironsourceatom.trackertest;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import io.ironsourceatom.sdk.IronSourceAtomFactory;
import io.ironsourceatom.sdk.IronSourceAtomTracker;
import io.ironsourceatom.sdk.IsaConfig;
import io.ironsourceatom.sdk.RemoteConnection;
import io.ironsourceatom.sdk.ReportService;

public class MainActivity
		extends Activity {

	protected static final String TAG = "TrackerTest";

	protected Boolean isFlushCalled        = false;
	protected Boolean isHandleReportCalled = false;

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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final TestsUtils.MockPoster httpClient = new TestsUtils.MockPoster();
		final IsaConfig isaConfig = IsaConfig.getInstance(this);

		ReportService reportHandler = new ReportService() {

			@Override
			protected RemoteConnection getClient() {
				return httpClient;
			}

			@Override
			protected IsaConfig getConfig(Context context) {
				return isaConfig;
			}
		};

		isaConfig.setBulkSize(3);
		isaConfig.setFlushInterval(1000);

		String authKey = "";
		final IronSourceAtomTracker tracker = new IronSourceAtomTracker(this, authKey);

		IronSourceAtomFactory trackerFactory = IronSourceAtomFactory.getInstance(this);
		IronSourceAtomTracker t = trackerFactory.newTracker("");

		//Logger.setPrintErrorStackTrace(true);
		isaConfig.enableErrorReporting();

		// Check immediately track API 21
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
		tracker.track("TRACK_503_ERROR_API21", "ER1");

		Thread thread = new Thread() {
			@Override
			public void run() {
				try {
					sleep(20000);

					if (!TestsUtils.MockPoster.isAllTrackerTasksCompleted()) {
						TestsUtils.MockPoster.printTrackerTaskStatus();

						Log.e(TAG, "Can't done all tasks!");
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
