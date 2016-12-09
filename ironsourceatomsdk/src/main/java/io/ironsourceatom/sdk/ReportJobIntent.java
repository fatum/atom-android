package io.ironsourceatom.sdk;


import android.content.Context;
import android.content.Intent;

/**
 * Created by valentine.pavchuk on 12/8/16.
 */

public class ReportJobIntent implements Report {
    protected static final String EXTRA_SDK_EVENT = "sdk_event";

    public static final String TABLE = "table";
    public static final String TOKEN = "token";
    public static final String BULK = "bulk";
    public static final String DATA = "data";
    public static final String AUTH = "auth";
    public static final String ENDPOINT = "endpoint";

    private Context context;
    private Intent intent;

    ReportJobIntent(Context context, int sdkEvent) {
        this.context = context;
        intent = new Intent(context, ReportJobService.class);
        intent.putExtra(EXTRA_SDK_EVENT, sdkEvent);
    }

    ReportJobIntent(Context context) {
        this.context = context;
        intent = new Intent(context, ReportJobService.class);
    }

    public void send() {
        context.startService(intent);
    }

    public ReportJobIntent setToken(String token) {
        intent.putExtra(TOKEN, token);
        return this;
    }

    @Override
    public Report setEndPoint(String endpoint) {
        intent.putExtra(ENDPOINT, endpoint);
        return this;
    }

    @Override
    public Report setBulk(boolean b) {
        intent.putExtra(BULK, String.valueOf(b));
        return this;
    }

    public ReportJobIntent setTable(String table) {
        intent.putExtra(TABLE, table);
        return this;
    }

    public ReportJobIntent setData(String value) {
        intent.putExtra(DATA, value);
        return this;
    }

    public Intent getIntent() {
        return intent;
    }
}
