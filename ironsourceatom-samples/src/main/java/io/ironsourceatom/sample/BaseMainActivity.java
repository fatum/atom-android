package io.ironsourceatom.sample;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;

import org.json.JSONException;
import org.json.JSONObject;

import io.ironsourceatom.sdk.IronSourceAtomFactory;
import io.ironsourceatom.sdk.IronSourceAtomTracker;

public class BaseMainActivity extends Activity {
    private IronSourceAtomFactory ironSourceAtomFactory;
    private static final String TAG = "SDK_EXAMPLE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_v2);



        // Create and config IronSourceAtomFactory instance
        ironSourceAtomFactory = IronSourceAtomFactory.getInstance(this);
        ironSourceAtomFactory.setErrorStream("sdkdev_android_sdk_errors");
        ironSourceAtomFactory.setErrorStreamAuth("I40iwPPOsG3dfWX30labriCg9HqMfL");
        ironSourceAtomFactory.enableErrorReporting();
        ironSourceAtomFactory.setBulkSize(5);
        ironSourceAtomFactory.setFlushInterval(10000);
        ironSourceAtomFactory.setAllowedNetworkTypes(IronSourceAtomFactory.NETWORK_MOBILE | IronSourceAtomFactory.NETWORK_WIFI);
        ironSourceAtomFactory.setAllowedOverRoaming(true);
    }

    public void sendReport(View v) {
        int id = v.getId();
        String atomStream = "sdkdev_sdkdev.public.zeev";
        String authKey = "I40iwPPOsG3dfWX30labriCg9HqMfL"; // Pre-shared HMAC auth key
        // Default ip for the Android studio VM.
        // String bulkURL = "http://10.0.2.2:3000/bulk";
        // String url = "http://10.0.2.2:3000";

        // Atom tracking url
        String url = "http://track.atom-data.io";
        String bulkURL = "http://track.atom-data.io/bulk";

        // Configure tracker
        IronSourceAtomTracker tracker = ironSourceAtomFactory.newTracker(authKey);
        tracker.setISAEndPoint(url);
        tracker.setISABulkEndPoint(bulkURL);

        JSONObject params = new JSONObject();
        switch (id) {
            case R.id.btnTrackReport:
                try {
                    params.put("event_name", "ANDROID_TRACKER");
                    params.put("id", "" + (int) (100 * Math.random()));
                } catch (JSONException e) {
                    Log.d(TAG, "Failed to track your json");
                }
                Log.d("[Tracking event]", params.toString());
                tracker.track(atomStream, params);
                break;
            case R.id.btnPostReport:
                try {
                    params.put("event_name", "ANDROID_TRACKER_SEND_NOW");
                    params.put("id", "" + (int) (100 * Math.random()));
                } catch (JSONException e) {
                    Log.d(TAG, "Failed to track your json");
                }
                // Will send this event immediately
                Log.d("[TRACKER_SEND_NOW]", params.toString());
                tracker.track(atomStream, params, true);
                break;
            case R.id.btnFlushReports:
                Log.d("[TRACKER_FLUSH]", "FLUSHING TRACKED EVENTS");
                tracker.flush();
                break;
        }
    }
}
