package io.ironsourceatom.trackertest;

<<<<<<< HEAD
import android.content.Context;
=======
import android.os.Bundle;
>>>>>>> 8225b5074630b7f2184a1cc59f36a82074d3dce3
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

<<<<<<< HEAD
import io.ironsourceatom.sdk.RemoteConnection;
import io.ironsourceatom.sdk.Report;
=======
import io.ironsourceatom.sdk.HttpClient;
import io.ironsourceatom.sdk.Report;
import io.ironsourceatom.sdk.ReportData;

>>>>>>> 8225b5074630b7f2184a1cc59f36a82074d3dce3

public class TestsUtils {

	static class MockReport
<<<<<<< HEAD
			extends Report {
=======
			implements Report {
>>>>>>> 8225b5074630b7f2184a1cc59f36a82074d3dce3

		@Override
		public MockReport setData(String value) {
			return this;
		}

		@Override
		public MockReport setTable(String table) {
			return this;
		}

		@Override
		public MockReport setToken(String token) {
			return this;
		}

		@Override
<<<<<<< HEAD
		public Report setEndpoint(String endpoint) {
=======
		public Report setEndPoint(String endpoint) {
>>>>>>> 8225b5074630b7f2184a1cc59f36a82074d3dce3
			return null;
		}

		@Override
		public Report setBulk(boolean b) {
			return null;
		}
<<<<<<< HEAD
	}

	static class MockPoster
			implements RemoteConnection {
=======

		@Override
		public Bundle getExtras() {
			return null;
		}

		public int mType;
	}

	static class MockPoster
			implements HttpClient {
>>>>>>> 8225b5074630b7f2184a1cc59f36a82074d3dce3

		static final String TAG = "TrackerTest";

		public static final Map<String, Boolean> TRACKER_TASKS;

		static int TRACKER_ERROR_400_COUNT = 0;
		static int TRACKER_ERROR_503_COUNT = 0;

		static {
<<<<<<< HEAD
			TRACKER_TASKS = new TreeMap<>();

			TRACKER_TASKS.put("TRACKNOW_API18", false);
			TRACKER_TASKS.put("TRACKNOW_API21", false);

			TRACKER_TASKS.put("TRACK_BULK_SIZE_API21", false);

			TRACKER_TASKS.put("TRACK_TIMER_API21", false);
			TRACKER_TASKS.put("TRACK_TIMER_API18", false);

			TRACKER_TASKS.put("TRACK_400_ERROR_API21", false);

=======
			TRACKER_TASKS = new HashMap<>();
			TRACKER_TASKS.put("TRACKNOW_API21", false);
			TRACKER_TASKS.put("TRACK_BULK_SIZE_API21", false);
			TRACKER_TASKS.put("TRACK_TIMER_API21", false);
			TRACKER_TASKS.put("TRACK_400_ERROR_API21", false);
>>>>>>> 8225b5074630b7f2184a1cc59f36a82074d3dce3
			TRACKER_TASKS.put("TRACK_503_ERROR_API21", false);
		}

		private static void setTaskStatus(String name, Boolean status) {
			TRACKER_TASKS.put(name, status);

			printTrackerTaskStatus();
		}

		public static void printTrackerTaskStatus() {
			StringBuilder taskStatus = new StringBuilder();
			taskStatus.append("Tracker tasks:\n");
			for (Map.Entry<String, Boolean> entry : TRACKER_TASKS.entrySet()) {
				taskStatus.append("  - ")
				          .append(entry.getKey())
				          .append(": ")
				          .append(entry.getValue())
				          .append(";\n");
			}
			taskStatus.append("\n");

			Log.i(TAG, taskStatus.toString());
		}

		public static boolean isAllTrackerTasksCompleted() {
			for (Map.Entry<String, Boolean> entry : TRACKER_TASKS.entrySet()) {
				if (!entry.getValue()) {
					return false;
				}
			}

			return true;
		}

		public void checkRequest(String data) throws
				IOException {
			try {
				JSONObject jsonObject = new JSONObject(data);

				String apiLevel = jsonObject.getString("data");
				String taskName = jsonObject.getString("table");

				if (TRACKER_TASKS.containsKey(taskName)) {
					if (taskName.equals("TRACK_400_ERROR_API21")) {
						if (TRACKER_ERROR_400_COUNT++ >= 1) {
							setNext(200);
							setTaskStatus(taskName, false);
						}
						else {
							setNext(400);
							setTaskStatus(taskName, true);
						}
					}
					else if (taskName.equals("TRACK_503_ERROR_API21")) {
						if (TRACKER_ERROR_503_COUNT++ >= 3) {
							setNext(200);
							setTaskStatus(taskName, true);
						}
						else {
							setNext(503);
						}
					}
					else {
						setTaskStatus(taskName, true);
						setNext(200);
					}
				}
			} catch (JSONException ex) {
			}
		}

		@Override
<<<<<<< HEAD
		public Response post(Context context, String data, String url) throws
=======
		public Response post(String data, String url) throws
>>>>>>> 8225b5074630b7f2184a1cc59f36a82074d3dce3
				IOException {
			checkRequest(data);

			Response res = new Response();
			if (mCode == 200) {
				try {
					JSONObject event = new JSONObject(data);
<<<<<<< HEAD
					String table = event.getString(Report.TABLE_KEY);
=======
					String table = event.getString(ReportData.TABLE);
>>>>>>> 8225b5074630b7f2184a1cc59f36a82074d3dce3
					if (!mBackedMock.containsKey(table)) {
						mBackedMock.put(table, new ArrayList<String>());
					}
					mBackedMock.get(table)
					           .add(data);
					res.code = 200;
					res.body = "OK";
				} catch (JSONException e) {
					res.code = 400;
					res.body = "invalid JSON";
				}
			}
			else if (mCode == 400) {
				res.code = 400;
				res.body = "invalid JSON";
			}
			else {
				res.code = 503;
			}
			return res;
		}


		public MockPoster setNext(int code) {
			this.mCode = code;
			return this;
		}

		// Hack to ignore keys ordering
		public String get(String key) {
			JSONArray events = new JSONArray();
			for (String event : mBackedMock.get(key)) {
				try {
					events.put(new JSONObject(event));
				} catch (JSONException e) {
				}
			}
			return events.toString();
		}

		// catch all incoming requests
<<<<<<< HEAD
		final public Map<String, List<String>> mBackedMock = new HashMap<>();
		private      int                       mCode       = 200;
	}
}
=======
		final public Map<String, List<String>> mBackedMock = new HashMap<String, List<String>>();
		private      int                       mCode       = 200;
	}
}
>>>>>>> 8225b5074630b7f2184a1cc59f36a82074d3dce3
