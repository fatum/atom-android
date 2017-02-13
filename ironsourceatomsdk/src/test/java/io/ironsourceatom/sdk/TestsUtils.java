package io.ironsourceatom.sdk;

import android.content.Intent;
import android.os.Bundle;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

		@Override
		public HttpClient.Response post(String data, String url) throws
				IOException {
			HttpClient.Response res = new HttpClient.Response();
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

	// Helper method.
	// Take SdkEvent and Map and generate new MockReport
	public static Intent newReport(Report.Action action, Map<String, String> report) {
		Bundle bundle = mock(Bundle.class);
		for (String key : report.keySet()) {
			when(bundle.get(key)).thenReturn(report.get(key));
		}
		when(bundle.getInt(ReportService.EXTRA_REPORT_ACTION_ENUM_ORDINAL, Report.Action.REPORT_ERROR.ordinal())).thenReturn(action.ordinal());

		Intent intent = mock(Intent.class);
		when(intent.getExtras()).thenReturn(bundle);
		return intent;
	}

	// Helper method.
	// Take SdkEvent and Map and generate new MockReport
	public static Intent newSimpleReport(Map<String, String> report) {
		Intent intent = mock(Intent.class);
		Bundle bundle = mock(Bundle.class);
		for (String key : report.keySet())
			when(bundle.get(key)).thenReturn(report.get(key));
		when(intent.getExtras()).thenReturn(bundle);
		return intent;
	}
}