package io.ironsourceatom.sdk;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Util class with helper methods
 */
class Utils {

	/**
	 * helper function that extract a buffer from the given inputStream
	 *
	 * @param in
	 * @return
	 * @throws IOException
	 */
	static byte[] getBytes(final InputStream in) throws
			IOException {
		final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		int bytesRead;
		byte[] data = new byte[8192];
		while ((bytesRead = in.read(data, 0, data.length)) != -1) {
			buffer.write(data, 0, bytesRead);
		}
		buffer.flush();
		return buffer.toByteArray();
	}

	/**
	 * IronSourceAtomFactory auth function
	 * Exception could be: NoSuchAlgorithmException and InvalidKeyException
	 *
	 * @param data
	 * @param key
	 * @return auth string
	 */
	static String auth(String data, String key) {
		try {
			SecretKeySpec secret_key = new SecretKeySpec(key.getBytes("UTF-8"), "HmacSHA256");
			Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
			sha256_HMAC.init(secret_key);
			StringBuilder sb = new StringBuilder();
			for (byte b : sha256_HMAC.doFinal(data.getBytes("UTF-8"))) {
				sb.append(String.format("%1$02x", b));
			}
			return sb.toString();
		} catch (NoSuchAlgorithmException | InvalidKeyException | UnsupportedEncodingException e) {
			return "";
		}
	}

	static boolean isBackgroundDataRestricted(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
			return cm.getRestrictBackgroundStatus() == ConnectivityManager.RESTRICT_BACKGROUND_STATUS_ENABLED;
		}
		else {
			NetworkInfo ni = cm.getActiveNetworkInfo();
			return ni != null && ni.getDetailedState() == NetworkInfo.DetailedState.BLOCKED;
		}
	}
}