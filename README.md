# ironSource.atom SDK for Android
[![License][license-image]](LICENSE)
[![Docs][docs-image]][docs-url]
[![Build status][travis-image]][travis-url]
[![Coverage Status][coveralls-image]][coveralls-url]
[![Maven Status][maven-image]][maven-url]

Atom-Android is the official [ironSource.atom](http://www.ironsrc.com/data-flow-management) SDK for the Android.

- [Sign up](https://atom.ironsrc.com/#/signup)
- [Documentation][docs-url]
- [Installation](#installation)
- [Usage](#usage)
- [Example](#example)

## Installation

### Installation for Gradle project
Add repository for you gradle config file
```java
repositories {
   maven { url "https://raw.github.com/ironSource/atom-android/mvn-repo/" }
}

```
and add dependency for Atom SDK
```java
dependencies {
   compile 'io.ironsourceatom.sdk:atom-sdk:1.1.0'
}
```

### Installation for Maven Project
Add repository for you pom.xml
```xml
<repositories>
    <repository>
        <id>atom-java</id>
        <url>https://raw.github.com/ironSource/atom-android/mvn-repo/</url>
        <snapshots>
            <enabled>true</enabled>
            <updatePolicy>always</updatePolicy>
        </snapshots>
    </repository>
</repositories>
```
and add dependency for Atom SDK
```xml
<dependencies>
    <dependency>
        <groupId>io.ironsourceatom.sdk</groupId>
        <artifactId>atom-sdk</artifactId>
        <version>1.1.0</version>
    </dependency>
</dependencies>
```

## Usage

### The SDK is divided into 2 separate services:
1. High level Tracker - contains a local db and tracks events based on certain parameters.
2. Low level - contains 2 methods: putEvent() and putEvents() to send 1 event or a batch respectively.  
**NOTE:** The low level service will be removed on the upcoming version (1.5.0).  
Check the example to see how to use the Tracker in order to immediately flush, if that is your use case.

### Error tracking:
The sdk supports an option to track internal errors to a separate stream in order to be able  
to debug errors at the client side.

Create the following table at your Redshift DB:
```sql
CREATE TABLE schema.table (
  details CHARACTER VARYING(1200),
  timestamp TIMESTAMP WITHOUT TIME ZONE,
  sdk_version CHARACTER VARYING(10),
  connection CHARACTER VARYING(20),
  platform CHARACTER VARYING(20),
  currencycode CHARACTER VARYING(10),
  os CHARACTER VARYING(40)
);
```

Setup and enable debug:
```java
ironSourceAtomFactory.setErrorStream("YOUR.ATOM.ERROR.STREAM");
ironSourceAtomFactory.setErrorStreamAuth("YOU.AUTH.KEY");
ironSourceAtomFactory.enableErrorReporting();
```

### Tracker usage
Add the following lines to AndroidManifest.xml
```java
<service android:name="io.ironsourceatom.sdk.ReportService" />
```

Add IronSourceAtom to your main activity. For example:
```java
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
        ironSourceAtomFactory.enableErrorReporting();
        ironSourceAtomFactory.setBulkSize(5);
        ironSourceAtomFactory.setFlushInterval(3000);
        ironSourceAtomFactory.setAllowedNetworkTypes(IronSourceAtomFactory.NETWORK_MOBILE | IronSourceAtomFactory.NETWORK_WIFI);
        ironSourceAtomFactory.setAllowedOverRoaming(true);
    }

    public void sendReport(View v) {
        int id = v.getId();
        String stream = "your.atom.stream";
        
        // Atom tracking url
        String url = "http://track.atom-data.io";
        String bulkURL = "http://track.atom-data.io/bulk";
        String authKey = ""; // Pre-shared HMAC auth key

        // Configure tracker
        IronSourceAtomTracker tracker = ironSourceAtomFactory.newTracker(authKey);
        tracker.setISAEndPoint(url);
        tracker.setISABulkEndPoint(bulkURL);
        
        // Android Tracker Example
        JSONObject params = new JSONObject();
        try {
            params.put("event_name", "ANDROID_TRACKER");
            params.put("id", "" + (int) (100 * Math.random()));
        } catch (JSONException e) {
            Log.d(TAG, "Failed to track your json");
        }
        Log.d("[Tracking event]", params.toString());
        tracker.track(stream, params);
        
        // Will send this event immediately
        try {
            params.put("event_name", "ANDROID_TRACKER_SEND_NOW");
            params.put("id", "" + (int) (100 * Math.random()));
        } catch (JSONException e) {
            Log.d(TAG, "Failed to track your json");
        }
        Log.d("[TRACKER_SEND_NOW]", params.toString());
        tracker.track(stream, params, true);
        Log.d("[TRACKER_FLUSH]", "FLUSHING TRACKED EVENTS");
        tracker.flush();
    }
}
```

The Tracker process:

You can use track() method in order to track the events to an Atom Stream.
The tracker accumulates events and flushes them when it meets one of the following conditions:
 
1. Flush Interval is reached (default: 10 seconds).
2. Bulk Length is reached (default: 4 events).
3. Maximum Request Limit is reached (default: 1MB).

In case of failure the tracker will preform an exponential backoff with jitter.
The tracker stores events in a local SQLITE database.

### Low level API Usage
The Low level SDK method putEvent() or array of events with method putEvents() as shown below.
This methods start new service and execute http post to the pipeline in it.

Add the following lines to AndroidManifest.xml
```java
<service android:name="io.ironsourceatom.sdk.SimpleReportService" />
```
Add IronSourceAtom to your main activity. For example:

```java
import io.ironsourceatom.sdk.IronSourceAtomFactory;
import io.ironsourceatom.sdk.IronSourceAtom;

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
        ironSourceAtomFactory.setAllowedNetworkTypes(IronSourceAtomFactory.NETWORK_MOBILE | IronSourceAtomFactory.NETWORK_WIFI);
        ironSourceAtomFactory.setAllowedOverRoaming(true);
    }

    public void sendReport(View v) {
        String stream = "your.stream.name";

        // Atom tracking url
        String url = "http://track.atom-data.io";
        String authKey = ""; // Pre-shared HMAC auth key

        // Configure sender to use methods putEvent() or putEvents()
        IronSourceAtom atom = ironSourceAtomFactory.newAtom(authKey); // SET AUTH KEY HERE
        atom.setEndPoint(url);

        JSONObject params = new JSONObject();
        try {
            params.put("event_name", "ANDROID_PUT_EVENT");
            params.put("id", "" + (int) (100 * Math.random()));
        } catch (JSONException e) {
            Log.d(TAG, "Failed to track your json");
        }
        Log.d("[putEvent]", params.toString());
        atom.putEvent(stream, params.toString());
        
        Gson gson = new Gson(); // Used for Array to json conversion.
        List<ExampleData> bulkList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            bulkList.add(new ExampleData((int) (Math.random() * 100), "ANDROID_PUT_EVENTS"));
        }
        Log.d("[putEvents]", gson.toJson(bulkList));
        atom.putEvents(stream, gson.toJson(bulkList));
    }
}
```

## Example
You can use our [example][example-url] for sending data to Atom:

![alt text][example]

## License
[MIT](LICENSE)

[example-url]: https://github.com/ironSource/atom-android/tree/master/ironsourceatom-samples
[example]: https://cloud.githubusercontent.com/assets/7361100/16713929/212a5496-46be-11e6-9ff7-0f5ed2c29844.png "example"
[license-image]: https://img.shields.io/badge/license-MIT-blue.svg?style=flat-square
[travis-image]: https://travis-ci.org/ironSource/atom-android.svg?branch=master
[travis-url]: https://travis-ci.org/ironSource/atom-android
[coveralls-image]: https://coveralls.io/repos/github/ironSource/atom-android/badge.svg?branch=master
[coveralls-url]: https://coveralls.io/github/ironSource/atom-android?branch=master
[docs-image]: https://img.shields.io/badge/docs-latest-blue.svg
[docs-url]: https://ironsource.github.io/atom-android/
[maven-image]: https://img.shields.io/badge/maven%20build-v1.1.0-green.svg
[maven-url]: https://github.com/ironSource/atom-android/tree/mvn-repo