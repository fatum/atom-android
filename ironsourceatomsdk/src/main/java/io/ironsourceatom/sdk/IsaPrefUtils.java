package io.ironsourceatom.sdk;

import android.content.Context;
import android.content.SharedPreferences;

class IsaPrefUtils {

	private static final String SHARED_PREF_FILENAME = "ironsourceatom.prefs";
	private static IsaPrefUtils sInstance;

	private Context mContext;

	private IsaPrefUtils(Context context) {
		mContext = context;
	}

	public static synchronized IsaPrefUtils getInstance(Context context) {
		if (sInstance == null) {
			sInstance = new IsaPrefUtils(context);
		}

		return sInstance;
	}

	public String load(String key, String defVal) {
		SharedPreferences pr = mContext.getSharedPreferences(SHARED_PREF_FILENAME, Context.MODE_PRIVATE);
		if (null != pr) {
			return pr.getString(key, defVal);
		}
		return defVal;
	}

	public boolean load(String key, boolean defVal) {
		return Boolean.parseBoolean(load(key, String.valueOf(defVal)));
	}

	public int load(String key, int defVal) {
		try {
			return Integer.parseInt(load(key, String.valueOf(defVal)));
		} catch (NumberFormatException e) {
			return defVal;
		}
	}

	public long load(String key, long defVal) {
		try {
			return Long.parseLong(load(key, String.valueOf(defVal)));
		} catch (NumberFormatException e) {
			return defVal;
		}
	}

	public String load(String key) {
		return load(key, "");
	}

	public <T> void save(String key, T value) {
		SharedPreferences pr = mContext.getSharedPreferences(SHARED_PREF_FILENAME, Context.MODE_PRIVATE);
		if (null != pr) {
			SharedPreferences.Editor editor = pr.edit();
			editor.putString(key, value.toString());
			editor.apply();
		}
	}

	public boolean delete(String key) {
		SharedPreferences pr = mContext.getSharedPreferences(SHARED_PREF_FILENAME, Context.MODE_PRIVATE);
		if (null != pr) {
			return pr.edit()
			         .remove(key)
			         .commit();
		}
		return false;
	}

}