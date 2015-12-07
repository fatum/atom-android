package com.ironsource.mobilcore;

import android.content.Context;
import android.content.Intent;

import com.ironsource.mobilcore.Consts.EServiceType;

public class ReportIntent extends Intent implements Report {

    public ReportIntent(Context context, int sdkEvent) {
        super(context, ReportService.class);
        mCtx = context;

        EServiceType.SERVICE_TYPE_REPORT.setValue(Consts.EXTRA_SERVICE_TYPE, this);
        putExtra(EXTRA_REPORT_TYPE, sdkEvent);
    }

    public void send() {
        mCtx.startService(this);
    }

    public ReportIntent setToken(String token) {
        putExtra(TOKEN, token);
        return this;
    }

    public ReportIntent setTable(String table) {
        putExtra(TABLE, table);
        return this;
    }

    public ReportIntent setData(String value) {
        putExtra(DATA, value);
        return this;
    }

    private Context mCtx;

    public static final String TABLE = "table";
    public static final String TOKEN = "token";
    public static final String BULK = "bulk";
    public static final String DATA = "data";
    public static final String AUTH = "auth";
    protected static final String EXTRA_REPORT_TYPE = "report_type";
}