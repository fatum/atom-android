package io.ironsourceatom.sdk;

import android.os.Bundle;

public class ReportData
        implements Report {

    public static final String EXTRA_SDK_EVENT = "sdk_event";

    public static final String TABLE = "table";
    public static final String TOKEN = "token";
    public static final String BULK = "bulk";
    public static final String DATA = "data";
    public static final String AUTH = "auth";
    public static final String ENDPOINT = "endpoint";

    protected Bundle extras = new Bundle();

    public ReportData() {
        this(-1);
    }

    public ReportData(int sdkEvent) {
        if (sdkEvent != -1) {
            extras.putInt(EXTRA_SDK_EVENT, sdkEvent);
        }
    }

    public ReportData setToken(String token) {
        extras.putString(TOKEN, token);
        return this;
    }

    @Override
    public Report setEndPoint(String endpoint) {
        extras.putString(ENDPOINT, endpoint);
        return this;
    }

    @Override
    public Report setBulk(boolean b) {
        extras.putString(BULK, String.valueOf(b));
        return this;
    }

    public ReportData setTable(String table) {
        extras.putString(TABLE, table);
        return this;
    }

    public ReportData setData(String value) {
        extras.putString(DATA, value);
        return this;
    }

    public Bundle getExtras() {
        return extras;
    }
}