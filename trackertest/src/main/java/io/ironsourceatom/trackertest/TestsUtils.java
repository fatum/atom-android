package io.ironsourceatom.trackertest;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.ironsourceatom.sdk.HttpClient;
import io.ironsourceatom.sdk.Report;


public class TestsUtils {

	static class MockReport
			extends Report {

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
		public Report setEndpoint(String endpoint) {
			return null;
		}

		@Override
		public Report setBulk(boolean b) {
			return null;
		}
	}


	static class MockPoster
			implements HttpClient {

		static final String TAG = "TrackerTest";

		public static final Map<String, Boolean> TRACKER_TASKS;

		static int TRACKER_ERROR_400_COUNT = 0;
		static int TRACKER_ERROR_503_COUNT = 0;

		static {
			TRACKER_TASKS = new HashMap<>();
			TRACKER_TASKS.put("TRACKNOW_API21", false);
			TRACKER_TASKS.put("TRACK_BULK_SIZE_API21", false);
			TRACKER_TASKS.put("TRACK_TIMER_API21", false);
			TRACKER_TASKS.put("TRACK_400_ERROR_API21", false);
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

		public Response post(String data, String url) throws
				IOException {
			checkRequest(data);

			Response res = new Response();
			if (mCode == 200) {
				try {
					JSONObject event = new JSONObject(data);
					String table = event.getString(Report.TABLE_KEY);
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
		final public Map<String, List<String>> mBackedMock = new HashMap<>();
		private      int                       mCode       = 200;
	}
}