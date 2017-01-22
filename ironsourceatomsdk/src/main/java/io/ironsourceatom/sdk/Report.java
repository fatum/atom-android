package io.ironsourceatom.sdk;

import org.json.JSONException;
import org.json.JSONObject;

class Report {

	enum Action {
		// save report
		ENQUEUE,
		// send all reports from storage
		FLUSH_QUEUE,
		// send report immediately
		POST_SYNC,
		// error on report
		ERROR,
		// report and error to sdk error stream
		REPORT_ERROR
	}

	static final String TABLE_KEY    = "table";
	static final String TOKEN_KEY    = "token";
	static final String ENDPOINT_KEY = "endpoint";
	static final String BULK_KEY     = "bulk";
	static final String DATA_KEY     = "data";
	static final String AUTH_KEY     = "auth";

	private JSONObject reportJsonObject;

	Report() {
		reportJsonObject = new JSONObject();
	}

	public Report setTable(String table) {
		setJsonKey(TABLE_KEY, table);
		return this;
	}

	public Report setToken(String token) {
		setJsonKey(TOKEN_KEY, token);
		return this;
	}

	public Report setEndpoint(String endpoint) {
		setJsonKey(ENDPOINT_KEY, endpoint);
		return this;
	}

	public Report setBulk(boolean bulk) {
		setJsonKey(BULK_KEY, bulk);
		return this;
	}

	public Report setData(String data) {
		setJsonKey(DATA_KEY, data);
		return this;
	}

	public Report setAuth(String auth) {
		setJsonKey(AUTH_KEY, auth);
		return this;
	}

	private void setJsonKey(String key, String value) {
		try {
			reportJsonObject.put(key, value);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private void setJsonKey(String key, boolean value) {
		try {
			reportJsonObject.put(key, value);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public String asJsonString() {
		return reportJsonObject.toString();
	}
}