package io.ironsourceatom.sdk;

import android.content.Context;
import android.os.AsyncTask;
import android.webkit.URLUtil;

import org.json.JSONObject;

import java.io.IOException;
import java.util.Map;


/**
 * This class is the High Level SDK for ironSource Atom (with tracker)
 */
public class IronSourceAtomTracker {

    private static final String TAG = "IronSourceAtomTracker";

    private Context context;
    private String auth;
    private IsaConfig config;

    /**
     * This class is the High Level SDK for ironSource Atom (with tracker).
     * </p>
     * You should use <code>IronSourceAtomFactory.newTracker(Auth String)</code> to create
     * an instance of this class.
     * </p>
     * While tracking events, IronSourceAtomTracker will queue them to disk (using SQLite)
     * and each period of time or batch count/length it will track the events to IronSourceAtom.
     *
     * @param context current context object
     * @param auth    pre shared auth key for Atom cluster
     */
    IronSourceAtomTracker(Context context, String auth) {
        this.context = context;
        this.auth = auth;
        config = IsaConfig.getInstance(context);
    }

    /**
     * Track an event that's already stringified,
     * send data mechanism is controlled by sendNow parameter.
     *
     * @param streamName The name on IronSourceAtom stream.
     * @param data       String, containing the data to send.
     * @param sendNow    flag if true report will send immediately else will postponed
     */
    public void track(String streamName, String data, boolean sendNow) {
        ReportService.sendReport(context, newReport(sendNow ? SdkEvent.POST_SYNC : SdkEvent.ENQUEUE).setTable(streamName)
                .setToken(auth)
                .setData(data));
    }

    /**
     * Track an event, send data mechanism is controlled by sendNow parameter.
     *
     * @param streamName The name on IronSourceAtom stream.
     * @param data       Map, containing the data to send.
     * @param sendNow    Send flag if true report will send immediately else will postponed
     */
    public void track(String streamName, Map<String, ?> data, boolean sendNow) {
        track(streamName, new JSONObject(data), sendNow);
    }

    /**
     * Track an event, send data mechanism is controlled by sendNow parameter.
     *
     * @param streamName The name on IronSourceAtom stream.
     * @param data       JSONObject, containing the data to send.
     * @param sendNow    Send flag if true report will send immediately else will postponed
     */
    public void track(String streamName, JSONObject data, boolean sendNow) {
        track(streamName, data.toString(), sendNow);
    }

    /**
     * Track an event that already stringify send data postponed.
     *
     * @param streamName The name on IronSourceAtom stream.
     * @param data       String, containing the data to send.
     */
    public void track(String streamName, String data) {
        track(streamName, data, false);
    }

    /**
     * Track an event, send data postponed.
     *
     * @param table IronSourceAtomFactory destination.
     * @param data  Map, containing the data to send.
     */
    public void track(String table, Map<String, ?> data) {
        track(table, new JSONObject(data), false);
    }

    /**
     * Track an event, send data postponed.
     *
     * @param streamName The name on IronSourceAtom stream.
     * @param data       JSONObject, containing the data to send.
     */
    public void track(String streamName, JSONObject data) {
        track(streamName, data.toString(), false);
    }

    /**
     * Flush immediately all reports
     */
    public void flush() {
        ReportService.sendReport(context, newReport(SdkEvent.FLUSH_QUEUE));
    }

    /**
     * Flush error info to error stream
     */
    public void trackError(String streamName, JSONObject data) {
        try {
            String dataStr = data.toString();
            JSONObject message = new JSONObject();

            if (!auth.isEmpty()) {
                message.put(ReportData.AUTH, Utils.auth(dataStr, auth));
            }
            message.put(ReportData.TABLE, streamName);
            message.put(ReportData.DATA, dataStr);

            String url = config.getAtomEndPoint(auth);

            // Cause of a bug in the Async task init
            try {
                new SendHttpRequestTask().execute(message.toString(), url);
            } catch (IllegalStateException ex) {
                new SendHttpRequestTask().execute(message.toString(), url);
            }
        } catch (Exception e) {
            Logger.log(TAG, "Failed to create message" + e, Logger.SDK_DEBUG);
        }
    }

    protected Report newReport(int eventCode) {
        return new ReportData(eventCode);
    }

    /**
     * Set custom endpoint to send reports
     *
     * @param url Custom publisher destination url.
     */
    public void setISAEndPoint(String url) {
        if (URLUtil.isValidUrl(url)) {
            config.setAtomEndPoint(auth, url);
        }
    }

    /**
     * Set custom bulk endpoint to send reports
     *
     * @param url
     */
    public void setISABulkEndPoint(String url) {
        if (URLUtil.isValidUrl(url)) {
            config.setAtomBulkEndPoint(auth, url);
        }
    }

    /**
     * Class for Asynchronously sending HTTP requests (case of error in the SDK)
     * Opens a separate thread
     */
    private class SendHttpRequestTask
            extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... parameters) {
            String message = parameters[0];
            String url = parameters[1];

            try {
                IronSourceAtomFactory.getInstance(context).getHttpClient().post(message, url);
            } catch (IOException ex) {
                Logger.log(TAG, ex.toString(), Logger.SDK_DEBUG);
            }
            return null;
        }
    }
}