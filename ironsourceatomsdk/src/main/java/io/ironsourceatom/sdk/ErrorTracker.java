package io.ironsourceatom.sdk;

import android.content.Context;
import android.os.Build;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import static android.content.ContentValues.TAG;

/**
 * Created on 09/02/2017 10:29.
 */

class ErrorTracker {

	private Context mContext;
	
	ErrorTracker(Context context) {
		mContext = context;
	}
	/**
	 * Track all SDK-errors/crashes when error-tracker enabled.
	 *
	 * @param errorString error info string
	 */
	protected void trackError(String errorString) {
		if (IsaConfig.getInstance(mContext).isErrorReportingEnabled()) {
			String stream = IsaConfig.getInstance(mContext).getSdkErrorStream();
			String authKey = IsaConfig.getInstance(mContext).getSdkErrorStreamAuthKey();

			Logger.log(TAG, "TRACKING ERROR TO: " + stream + " / " + authKey, Logger.SDK_DEBUG);

			IronSourceAtomTracker sdkTracker = IronSourceAtomFactory.getInstance(mContext).newTracker(authKey);

			try {
				JSONObject errorReport = new JSONObject();
				errorReport.put("details", errorString);
				errorReport.put("timestamp", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).format(Calendar.getInstance()
				                                                                                                        .getTime()));
				errorReport.put("sdk_version", Consts.VER);
				errorReport.put("connection", NetworkManager.getInstance(mContext)
				                                            .getConnectedNetworkType());
				errorReport.put("platform", "Android");
				errorReport.put("os", String.valueOf(Build.VERSION.SDK_INT));
				errorReport.put("api_version", Build.VERSION.RELEASE);
				errorReport.put("manufacturer", Build.MANUFACTURER);
				errorReport.put("model", Build.MODEL);
				Locale locale;
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
					locale = mContext.getResources()
					                .getConfiguration()
					                .getLocales()
					                .get(0);
				}
				else {
					locale = mContext.getResources()
					                .getConfiguration().locale;
				}
				errorReport.put("locale", locale.toString());
				sdkTracker.trackError(stream, errorReport);
			} catch (Exception e) {
				Logger.log(TAG, "Failed to track error: " + e, Logger.SDK_DEBUG);
			}
		}
	}
}