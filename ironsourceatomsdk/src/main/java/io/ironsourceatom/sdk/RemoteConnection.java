package io.ironsourceatom.sdk;

import android.content.Context;

import java.io.IOException;

public interface RemoteConnection {

	Response post(final Context context, final String data, final String url) throws
			IOException;

	/**
	 * Response-like class
	 */
	class Response {

		public int    code;
		public String body;
	}
}
