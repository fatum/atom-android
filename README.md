# ironSource.atom SDK for Android
[![License][license-image]][license-url]
[![Docs][docs-image]][docs-url]
[![Coverage Status][coveralls-image]][coveralls-url]
[![Build status][travis-image]][travis-url]


Atom-Android is the official [ironSource.atom](http://www.ironsrc.com/data-flow-management) SDK for the Android.

###Installation

Currently, there is one way to integrate. soon, it will be available on jcenter and Github as well.

1. Add the SDK jar into libs directory.

2. Add dependency to app/build.gradle
```java
dependencies {
    compile fileTree(dir: 'libs', include: ['*.jar'])
}
```
###Tracker usage

 Add the following lines to AndroidManifest.xml
```java
        <service android:name="io.ironsourceatom.sdk.ReportService" />
```

Add IronSourceAtom to your main activity. For example:
```java
...
import io.ironsourceatom.sdk.HttpMethod;
import io.ironsourceatom.sdk.IronSourceAtom;
import io.ironsourceatom.sdk.IronSourceAtomEventSender;

public class BaseMainActivity extends Activity {
    private IronSourceAtom ironSourceAtom;
    private final String STREAM="YOUR_IRONSOURCEATOM_STREAM_NAME";
    @Override
       protected void onCreate(Bundle savedInstanceState) {
            ...
            // Configure IronSourceAtom
           ironSourceAtom = IronSourceAtom.getInstance(this);
           ironSourceAtom.setAllowedNetworkTypes(ironSourceAtom.NETWORK_MOBILE | ironSourceAtom.NETWORK_WIFI);
           ironSourceAtom.setAllowedOverRoaming(true);
           // Create and config ironSourceAtomTracker
           ironSourceAtomTracker tracker = ironSourceAtom.newTracker("YOUR_AUTH_KEY");
           try {
               JSONObject events = new JSONObject();
               events.put("action", "click on ...");
               events.put("user_id", user.id);
               tracker.track("STREAM", event);
           } catch (JSONException e) {
               ...
           }
           ...
        
    }
```

The tracker process:
You can use track() methods in order to track the evnets to Atom.
The tracker accumulates the event.
Once the flushInterval / bulkLength or bulkSize is reached you flush a batch of records to the inFlight queue.
Batches in the inFlight queue will be sent in parallel.
In case of failure each batch will have its own exponential back-off mechanism.

### Low level API usage
You can also use low level api to simple send single event with method putEvent() or array of events with method putEvents() as shown below.
This methods start new service and execute httpPost to the pipeline in it.

 Add the following lines to AndroidManifest.xml
```java
        <service android:name="io.ironsourceatom.sdk.SimpleReportService" />
```
Add IronSourceAtom to your main activity. For example:

```java
...
import io.ironsourceatom.sdk.IronSourceAtomFactory;
import io.ironsourceatom.sdk.IronSourceAtom;

public class BaseMainActivity extends Activity {
    private IronSourceAtomFactory ironSourceAtomFactory;
    private final String STREAM="YOUR_IRONSOURCEATOM_STREAM_NAME";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        .....

        // Create and config IronSourceAtom instance
        ironSourceAtomFactory = IronSourceAtomFactory.getInstance(this);
         String url = "https://track.atom-data.io/";
         IronSourceAtom atom = ironSourceAtomFactory.newAtom("YOUR_AUTH_KEY"");
        atom.setEndPoint(url);
         
         JSONObject params = new JSONObject();
         
         try {
               params.put("action", "Action 1");
               params.put("id", "1");
              } catch (JSONException e) {
                 Log.d("TAG", "Failed to put your json");
              }
           atom.putEvent(STREAM, params.toString());
                
    }
```
Make sure you have replaced "YOUR_AUTH_KEY with your IronSourceAtom auth key, and "YOUR_IRONSOURCEATOM_STREAM_NAME" to the desired destination (e.g: “cluster.schema.table”)

### Example

You can use our [example][example-url] for sending data to Atom:

![alt text][example]

### License
MIT

[example-url]: https://github.com/ironSource/atom-android/tree/master/ironsourceatom-samples
[example]: https://cloud.githubusercontent.com/assets/7361100/16713929/212a5496-46be-11e6-9ff7-0f5ed2c29844.png "example"
[license-image]: https://img.shields.io/badge/license-MIT-blue.svg?style=flat-square
[license-url]: https://github.com/ironSource/atom-android/blob/master/LICENSE
[travis-image]: https://travis-ci.org/ironSource/atom-android.svg?branch=master
[travis-url]: https://travis-ci.org/ironSource/atom-android
[coveralls-image]: https://coveralls.io/repos/github/ironSource/atom-android/badge.svg?branch=master
[coveralls-url]: https://coveralls.io/github/ironSource/atom-android?branch=master
[docs-image]: https://img.shields.io/badge/docs-latest-blue.svg
[docs-url]: https://ironsource.github.io/ironbeast-android/




