package io.ironsourceatom.sdk;

import android.content.Context;

import java.util.HashMap;
import java.util.Map;

/**
 * Factory to produce an instance of IronSourceAtom Class or IronSourceAtomTracker Class
 */
public class IronSourceAtomFactory {

	private static final String TAG = "IronSourceAtomFactory";

	private static IronSourceAtomFactory sInstance;

	static final String VERSION_NAME = BuildConfig.VERSION_NAME;

	public static final int NETWORK_MOBILE = 1;
	public static final int NETWORK_WIFI   = 2;

	private final Map<String, IronSourceAtomTracker> sAvailableTrackers = new HashMap<>();

	private Context context;

	private IsaConfig config;

	private HttpClient mHttpClient;

	private ErrorTracker mErrorTracker;

	public static synchronized IronSourceAtomFactory getInstance(Context context) {
		if (context == null) {
			throw new IllegalArgumentException("`context` should be valid Context object");
		}

		if (sInstance == null) {
			sInstance = new IronSourceAtomFactory(context.getApplicationContext());
		}

		return sInstance;
	}

	IronSourceAtomFactory(Context context) {
		this.context = context;
		config = IsaConfig.getInstance(context);
		mHttpClient = new DefaultHttpClient();
		mErrorTracker = new ErrorTracker(context);
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
			}
			else {
				tracker = new IronSourceAtomTracker(context, auth);
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
	 * Set Logger print stack trace
	 *
	 * @param isPrintStackTrace enable print stack trace
	 */
	public void setLogPrintStackTrace(boolean isPrintStackTrace) {
		Logger.setPrintErrorStackTrace(isPrintStackTrace);
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
	 * function set report flushTable intervals
	 *
	 * @param ms - time for flushTable in milliseconds
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
	 * Provides an external custom implementation of the HTTP communication layer
	 *
	 * @param httpClient
	 */
	public void setHttpClient(HttpClient httpClient) {
		mHttpClient = httpClient;
	}

	public HttpClient getHttpClient() {
		return mHttpClient;
	}

	ErrorTracker getErrorTracker() {
		return mErrorTracker;
	}
}