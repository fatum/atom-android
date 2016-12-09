package io.ironsourceatom.sdk;

import android.content.Context;
import android.os.Build;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Factory to produce an instance of IronSourceAtom Class or IronSourceAtomTracker Class
 */
public class IronSourceAtomFactory {
    private static final String TAG = "IronSourceAtomFactory";
    private static final Object sInstanceLockObject = new Object();

    private static IronSourceAtomFactory sInstance;

    public static final int NETWORK_MOBILE = 1;
    public static final int NETWORK_WIFI = 2;

    private final Map<String, IronSourceAtomTracker> sAvailableTrackers = new HashMap<>();

    private IsaConfig config;
    private Context context;

    /**
     * Do not call directly.
     * You should use IronSourceAtomFactory.getInstance()
     */
    public IronSourceAtomFactory(Context context) {
        this.context = context;
        config = IsaConfig.getInstance(context);
    }

    public static IronSourceAtomFactory getInstance() {
        return sInstance;
    }

    public static IronSourceAtomFactory getInstance(Context context) {
        if (null == context) {
            throw new IllegalArgumentException("`context` should be valid Context object");
        }
        synchronized (sInstanceLockObject) {
            if (sInstance == null) {
                sInstance = new IronSourceAtomFactory(context.getApplicationContext());
            }
        }
        return sInstance;
    }

    /**
     * Create IronSourceAtomTracker with your YOUR_AUTH_KEY.
     * Example:
     * <code>
     * IronSourceAtomTracker tracker = IronSourceAtomFactory.newTracker("YOUR_AUTH_KEY");
     * </code>
     *
     * @param auth your IronSourceAtomFactory auth key
     * @return IronSourceAtomTracker
     */
    public IronSourceAtomTracker newTracker(String auth) {
        if (null == auth) {
            throw new IllegalArgumentException("`auth` should be valid String");
        }
        synchronized (sAvailableTrackers) {
            IronSourceAtomTracker tracker;
            if (sAvailableTrackers.containsKey(auth)) {
                tracker = sAvailableTrackers.get(auth);
            } else {
                tracker = new IronSourceAtomTracker(sInstance.context, auth);
                sAvailableTrackers.put(auth, tracker);
            }
            return tracker;
        }
    }

    /**
     * Enable the SDK error-tracker.
     */
    public void enableErrorReporting() {
        Logger.log(TAG, "Error reporting enabled", Logger.SDK_DEBUG);
        config.enableErrorReporting();
    }

    /**
     * Set whether the SDK can keep sending over a roaming connection.
     *
     * @param allowed
     */
    public void setAllowedOverRoaming(boolean allowed) {
        config.setAllowedOverRoaming(allowed);
    }

    /**
     * Restrict the types of networks over which this SDK can keep making HTTP requests.
     * By default, all network types are allowed
     *
     * @param flags
     */
    public void setAllowedNetworkTypes(int flags) {
        config.setAllowedNetworkTypes(flags);
    }

    /**
     * Set the SDK log level.
     *
     * @param logType
     */
    public void setLogType(IsaConfig.LOG_TYPE logType) {
        Logger.logLevel = logType;
    }

    /**
     * function set report bulk max size
     *
     * @param size - max size of report bulk (rows)
     */
    public void setBulkSize(int size) {
        config.setBulkSize(size);
    }

    /**
     * function set report max size in bytes
     *
     * @param bytes - max size of report (file size)
     */
    public void setMaximumRequestLimit(long bytes) {
        config.setMaximumRequestLimit(bytes);
    }

    /**
     * function set report flush intervals
     *
     * @param ms - time for flush in milliseconds
     */
    public void setFlushInterval(int ms) {
        config.setFlushInterval(ms);
    }

    /**
     * Setter for error stream
     *
     * @param errorStream
     */
    public void setErrorStream(String errorStream) {
        config.setSdkErrorStream(errorStream);
    }

    /**
     * Setter for error stream auth
     *
     * @param errorStreamAuth
     */
    public void setErrorStreamAuth(String errorStreamAuth) {
        config.setSdkErrorStreamAuthKey(errorStreamAuth);
    }

    /**
     * Track all SDK-errors/crashes when error-tracker enabled.
     *
     * @param errorString error info string
     */
    protected void trackError(String errorString) {
        if (config.isErrorReportingEnabled()) {
            String stream = config.getSdkErrorStream();
            String authKey = config.getSdkErrorStreamAuthKey();

            Logger.log(TAG, "TRACKING ERROR TO: " + stream + " / " + authKey, Logger.SDK_DEBUG);

            IronSourceAtomTracker sdkTracker = this.newTracker(authKey);

            try {
                JSONObject errorReport = new JSONObject();
                errorReport.put("details", errorString);
                errorReport.put("timestamp", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
                        .format(Calendar.getInstance().getTime()));
                errorReport.put("sdk_version", Consts.VER);
                errorReport.put("connection", NetworkManager.getInstance(context).getConnectedNetworkType());
                errorReport.put("platform", "Android");
                errorReport.put("os", String.valueOf(Build.VERSION.SDK_INT));
                errorReport.put("api_version", Build.VERSION.RELEASE);
                errorReport.put("manufacturer", Build.MANUFACTURER);
                errorReport.put("model", Build.MODEL);
                errorReport.put("locale", context.getResources().getConfiguration().locale.toString());
                sdkTracker.trackError(stream, errorReport);
            } catch (Exception e) {
                Logger.log(TAG, "Failed to track error: " + e, Logger.SDK_DEBUG);
            }
        }
    }

}
