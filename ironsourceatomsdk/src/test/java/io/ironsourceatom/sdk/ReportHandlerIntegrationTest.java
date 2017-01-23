package io.ironsourceatom.sdk;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.HashMap;
import java.util.Map;

import static junit.framework.Assert.assertEquals;

/**
 * ReportHandler integration with the StorageService.
 */
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, emulateSdk = 18, manifest = Config.NONE)

public class ReportHandlerIntegrationTest {

	@Before
	public void reset() {
		mClient.mBackedMock.clear();
		mReportService.init(RuntimeEnvironment.application);
		mFlushDatabaseService.init(RuntimeEnvironment.application);
	}

	@Test
	public void testPostSuccess() throws
			Exception {
		mReportService.handleReport(new JSONObject(event1), Report.Action.POST_SYNC);
		assertEquals(new JSONArray("[{" +
				"\"data\":\"[ib-data]\"," +
				"\"table\":\"ib_test\"," +
				"\"bulk\":true," +
				"\"auth\":\"87015109c69035da52b0ca267a2586e0a0865ebdb369be260a504578750fff9f\"" +
				"}]").toString(), mClient.get(TABLE1));

		mReportService.handleReport(new JSONObject(event2), Report.Action.POST_SYNC);
		assertEquals(new JSONArray("[{" +
				"\"data\":\"[ic-data]\"," +
				"\"table\":\"ic_test\"," +
				"\"bulk\":true," +
				"\"auth\":\"b7e90b4f318d1e1fbbb452dab032647b19f128ab3aa2b88c1098508e1946bc76\"" +
				"}]").toString(), mClient.get(TABLE2));
	}

	@Test
	public void testPostFailed() {
		mClient.setNext(503);
		mReportService.handleReport(new JSONObject(event1), Report.Action.POST_SYNC);
		mReportService.handleReport(new JSONObject(event2), Report.Action.POST_SYNC);
		assertEquals(mAdapter.count(null), 2);
		assertEquals(mAdapter.getTables()
		                     .size(), 2);
	}

	@Test
	public void testTrackEvent() {
		mConfig.setBulkSize(Integer.MAX_VALUE);
		for (int i = 1 ; i <= 10 ; i++) {
			mReportService.handleReport(new JSONObject(event1), Report.Action.ENQUEUE);
			assertEquals(mAdapter.count(null), i);
		}
		assertEquals(mAdapter.getTables()
		                     .size(), 1);
	}

	@Test
	public void testTrackError() throws
			JSONException {
		mReportService.handleReport(new JSONObject(event1), Report.Action.REPORT_ERROR);
		assertEquals(mClient.get(TABLE1), new JSONArray("[{" +
				"\"data\":\"ib-data\"," +
				"\"table\":\"ib_test\"," +
				"\"auth\":\"fbc254c2e706a3dc3a0b35985f220a66a2e05a25011bcbbe245671a2f54c1e8c\"" +
				"}]").toString());
	}

	@Test
	public void testTrackTriggerFlush() throws
			Exception {
		mConfig.setBulkSize(2);
		for (int i = 1 ; i <= 10 ; i++) {
			final Map<String, String> event = new HashMap<>(event1);
			event.put(Report.DATA_KEY, String.valueOf(i));
			mReportService.handleReport(new JSONObject(event), Report.Action.ENQUEUE);
		}
		assertEquals(0, mAdapter.count(null));
		assertEquals(mAdapter.getTables()
		                     .size(), 0);
		assertEquals(mClient.get(TABLE1), new JSONArray("[{" +
				"\"data\":\"[1, 2]\"," +
				"\"table\":\"ib_test\"," +
				"\"bulk\":true," +
				"\"auth\":\"a2fbb1365ac648437256831a43d8a127efe824c3b45564c4165bb4f68e42af08\"" +
				"}, {" +
				"\"data\":\"[3, 4]\"," +
				"\"table\":\"ib_test\"," +
				"\"bulk\":true," +
				"\"auth\":\"b46dcb43f024bb95c418d13c4f23712f59c7f934ad5d78fa49d763eaac1df2ed\"" +
				"}, {" +
				"\"data\":\"[5, 6]\"," +
				"\"table\":\"ib_test\"," +
				"\"bulk\":true," +
				"\"auth\":\"98da95e7b92ebdffd0307324cdcfe7df309e3666db1ccb6441afbf7a63f77a97\"" +
				"}, {" +
				"\"data\":\"[7, 8]\"," +
				"\"table\":\"ib_test\"," +
				"\"bulk\":true," +
				"\"auth\":\"187a7d74ad46e656fdda29543dc2ed8fee3954a064223ffa2046752d91c5c478\"" +
				"}, {" +
				"\"data\":\"[9, 10]\"," +
				"\"table\":\"ib_test\"," +
				"\"bulk\":true," +
				"\"auth\":\"1dee8cb3b7c482050b62582fe982ab50c7c49d0beddbc254d282fbe4feee897b\"" +
				"}]").toString());
	}

	@Test
	public void testFlush() {
		mConfig.setBulkSize(5);
		for (int i = 1 ; i <= 10 ; i++) {
			final Map<String, String> event = new HashMap<>(event1);
			event.put(Report.DATA_KEY, String.valueOf(i));
			mReportService.handleReport(new JSONObject(event), Report.Action.ENQUEUE);
			event.put(Report.TABLE_KEY, TABLE2);
			mReportService.handleReport(new JSONObject(event), Report.Action.ENQUEUE);
		}

		assertEquals(0, mAdapter.countAll());
		assertEquals(mAdapter.getTables()
		                     .size(), 0);
		assertEquals(4, mClient.mBackedMock.get(TABLE1)
		                                .size());
		assertEquals(4, mClient.mBackedMock.get(TABLE2)
		                                .size());
	}

	// Events to test
	final String TABLE1 = "ib_test", TOKEN1 = "ib_token", DATA1 = "ib-data";
	final String TABLE2 = "ic_test", TOKEN2 = "ic_token", DATA2 = "ic-data";
	final Map<String, String>   event1         = new HashMap<String, String>() {{
		put(Report.DATA_KEY, DATA1);
		put(Report.TOKEN_KEY, TOKEN1);
		put(Report.TABLE_KEY, TABLE1);
	}};
	final Map<String, String>   event2         = new HashMap<String, String>() {{
		put(Report.DATA_KEY, DATA2);
		put(Report.TOKEN_KEY, TOKEN2);
		put(Report.TABLE_KEY, TABLE2);
	}};
	// MockBackend
	final TestsUtils.MockPoster mClient        = new TestsUtils.MockPoster();
	final IsaConfig             mConfig        = IsaConfig.getInstance(RuntimeEnvironment.application);
	final StorageApi            mAdapter       = new DbAdapter(RuntimeEnvironment.application);
	final ReportService         mReportService = new ReportService() {
		@Override
		protected StorageApi getStorage(Context context) {
			return mAdapter;
		}

		@Override
		void flushDatabase() {
			mFlushDatabaseService.flushDatabase();
		}
	};

	final FlushDatabaseService mFlushDatabaseService = new FlushDatabaseService() {
		@Override
		protected RemoteConnection getHttpClient() {
			return mClient;
		}

		@Override
		protected StorageApi getStorage(Context context) {
			return mAdapter;
		}
	};
}