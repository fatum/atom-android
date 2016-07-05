package io.ironsourceatom.sample;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.ironsourceatom.sdk.IronSourceAtomFactory;
import io.ironsourceatom.sdk.IronSourceAtom;
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
        ironSourceAtomFactory.enableErrorReporting();
        ironSourceAtomFactory.setBulkSize(5);
        ironSourceAtomFactory.setFlushInterval(3000);
        ironSourceAtomFactory.setAllowedNetworkTypes(IronSourceAtomFactory.NETWORK_MOBILE | IronSourceAtomFactory.NETWORK_WIFI);
        ironSourceAtomFactory.setAllowedOverRoaming(true);
    }

    public void sendReport(View v) {
        int id = v.getId();
        String stream = "sdkdev_sdkdev.public.atom_demo_events";
        String url = "http://track.atom-data.io/";
        String authKey = ""; // Pre-shared HMAC auth key

        // Configure sender to use methods putEvent() or putEvents()
        IronSourceAtom atom = ironSourceAtomFactory.newAtom(authKey); // SET AUTH KEY HERE
        atom.setEndPoint(url);

        // Configure tracker
        IronSourceAtomTracker tracker = ironSourceAtomFactory.newTracker(authKey);
        tracker.setISAEndPoint(url);

        JSONObject params = new JSONObject();
        switch (id) {
            case R.id.btnPutEvent:
                try {
                    params.put("event_name", "ANDROID_PUT_EVENT");
                    params.put("id", "" + (int) (100 * Math.random()));
                } catch (JSONException e) {
                    Log.d(TAG, "Failed to track your json");
                }
                Log.d("[putEvent]", params.toString());
                atom.putEvent(stream, params.toString());
                break;

            case R.id.btnPutEvents:
                Gson gson = new Gson(); // Used for Array to json conversion.
                List<ExampleData> bulkList = new ArrayList<>();
                for (int i = 0; i < 10; i++) {
                    bulkList.add(new ExampleData((int) (Math.random() * 100), "ANDROID_PUT_EVENTS"));
                }
                Log.d("[putEvents]", gson.toJson(bulkList));
                atom.putEvents(stream, gson.toJson(bulkList));
                break;
            case R.id.btnTrackReport:
                try {
                    params.put("event_name", "ANDROID_TRACKER");
                    params.put("id", "" + (int) (100 * Math.random()));
                } catch (JSONException e) {
                    Log.d(TAG, "Failed to track your json");
                }
                Log.d("[Tracking event]", params.toString());
                tracker.track(stream, params);
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
                tracker.track(stream, params, true);
                break;
            case R.id.btnFlushReports:
                Log.d("[TRACKER_FLUSH]", "FLUSHING TRACKED EVENTS");
                tracker.flush();
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
}
