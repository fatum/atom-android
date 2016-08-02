# ironSource.atom SDK for Android
[![License][license-image]][license-url]
[![Docs][docs-image]][docs-url]
[![Build status][travis-image]][travis-url]
[![Coverage Status][coveralls-image]][coveralls-url]
[![Maven Status][maven-image]][maven-url]

Atom-Android is the official [ironSource.atom](http://www.ironsrc.com/data-flow-management) SDK for the Android.

- [Signup](https://atom.ironsrc.com/#/signup)
- [Documentation][docs-url]
- [Installation](#Installation)
- [Sending an event](#Tracker-usage)

###Installation

## Instalation for Gradle Project
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

## Installation for Maven Project
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

The SDK is divided into 2 separate services:

1. High level Tracker - contains a local db and tracks events based on certain parameters.
2. Low level - contains 2 methods: putEvent() and putEvents() to send 1 event or a batch respectively.

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
    // Your atom stream name, for example: "cluster.schema.table"
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
               tracker.track(STREAM, event);
           } catch (JSONException e) {
               ...
           }
           ...
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
...
import io.ironsourceatom.sdk.IronSourceAtomFactory;
import io.ironsourceatom.sdk.IronSourceAtom;

public class BaseMainActivity extends Activity {
    private IronSourceAtomFactory ironSourceAtomFactory;
    // Your atom stream name, for example: "cluster.schema.table"
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

### Example

You can use our [example][example-url] for sending data to Atom:

![alt text][example]

### License
[MIT][license-url]

[example-url]: https://github.com/ironSource/atom-android/tree/master/ironsourceatom-samples
[example]: https://cloud.githubusercontent.com/assets/7361100/16713929/212a5496-46be-11e6-9ff7-0f5ed2c29844.png "example"
[license-image]: https://img.shields.io/badge/license-MIT-blue.svg?style=flat-square
[license-url]: https://github.com/ironSource/atom-android/blob/master/LICENSE
[travis-image]: https://travis-ci.org/ironSource/atom-android.svg?branch=master
[travis-url]: https://travis-ci.org/ironSource/atom-android
[coveralls-image]: https://coveralls.io/repos/github/ironSource/atom-android/badge.svg?branch=master
[coveralls-url]: https://coveralls.io/github/ironSource/atom-android?branch=master
[docs-image]: https://img.shields.io/badge/docs-latest-blue.svg
[docs-url]: https://ironsource.github.io/atom-android/
[maven-image]: https://img.shields.io/badge/maven%20build-v1.1.0-green.svg
[maven-url]: https://github.com/ironSource/atom-android/tree/mvn-repo