package io.ironsourceatom.sdk;

import android.content.Context;
import android.net.http.AndroidHttpClient;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;

import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;

/**
 * HttpClient is the default implementations to RemoteService.
 * Used for processing requests to endpoint
 * <p>
 * PENDING: Might be a good idea to allow supplying the implementation outside the SDK
 */
class DefaultHttpClient
		implements HttpClient {

	private static final String TAG = "HttpClient";

	private static final int DEFAULT_READ_TIMEOUT_MILLIS    = 15 * 1000; // 15s
	private static final int DEFAULT_CONNECT_TIMEOUT_MILLIS = 10 * 1000; // 10s

	/**
	 * Post String-data to the given url.
	 *
	 * @param data string with data to send
	 * @param url  target url
	 * @return RemoteService.Response that has code and body.
	 */
	public Response post(final String data, final String url) throws
			IOException {
		Response response = new Response();
		HttpURLConnection connection = null;
		DataOutputStream out = null;
		InputStream in = null;
		try {
			connection = createConnection(url);
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setDoOutput(true);

			out = new DataOutputStream(connection.getOutputStream());
			out.write(data.getBytes("UTF-8"));
			out.flush();
			out.close();
			out = null;

			in = connection.getInputStream();
			response.body = new String(Utils.getBytes(in), Charset.forName("UTF-8"));
			response.code = connection.getResponseCode();
			in.close();
			in = null;
		} catch (IOException e) {
			if (connection != null && (response.code = connection.getResponseCode()) >= HTTP_BAD_REQUEST) {
				Logger.log(TAG, "Failed post to Atom. StatusCode: " + response.code, Logger.SDK_DEBUG);
			}
			else {
				throw e;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (null != connection) {
				connection.disconnect();
			}
			if (null != out) {
				out.close();
			}
			if (null != in) {
				in.close();
			}
		}
		return response;
	}

	/**
	 * Post data using Gzip -
	 * PENDING: Currently not supported on the server side
	 *
	 * @param context
	 * @param data
	 * @param url
	 * @return
	 * @throws IOException
	 */
	@SuppressWarnings("deprecation")
	public Response postGzip(final Context context, final String data, final String url) throws
			IOException {
		Response response = new Response();

		try {
			AndroidHttpClient httpClient = AndroidHttpClient.newInstance("atomSDK/0");
			HttpPost httpPost = new HttpPost(url);
			HttpParams httpParams = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(httpParams, DEFAULT_CONNECT_TIMEOUT_MILLIS);
			HttpConnectionParams.setSoTimeout(httpParams, DEFAULT_READ_TIMEOUT_MILLIS);
			HttpConnectionParams.setTcpNoDelay(httpParams, true);
			httpPost.setParams(httpParams);
			AndroidHttpClient.modifyRequestToAcceptGzipResponse(httpPost);
			setCompressedEntity(context, data, httpPost);

			final HttpResponse httpResponse = httpClient.execute(httpPost);
			final StatusLine statusLine = httpResponse.getStatusLine();
			response.code = statusLine.getStatusCode();
			response.body = getIfCompressed(httpResponse);
		} catch (IOException e) {
			if (response.code >= HTTP_BAD_REQUEST) {
				Logger.log(TAG, "Failed post to Atom. StatusCode: " + response.code, Logger.SDK_DEBUG);
			}
			else {
				throw e;
			}
		}
		return response;
	}

	/**
	 * Compresses the content of the request parameters (as a string). Sets
	 * appropriate HTTP headers also so that the server can decode it properly.
	 *
	 * @param context Context
	 * @param content The string request params, ideally JSON string
	 * @param postReq The HttpPost request object
	 */
	private void setCompressedEntity(Context context, String content, HttpPost postReq) {
		try {
			byte[] data = content.getBytes("UTF-8");

			// if the length of the data exceeds the minimum gzip size then only
			// gzip it else it's not required at all
			if (content.length() > AndroidHttpClient.getMinGzipSize(context.getContentResolver())) {
				// set necessary headers
				postReq.setHeader("Content-Encoding", "gzip");
			}

			// Compressed entity itself checks for minimum gzip size
			// and if the content is shorter than that size then it
			// just returns a ByteArrayEntity
			postReq.setEntity(AndroidHttpClient.getCompressedEntity(data, context.getContentResolver()));

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private String getIfCompressed(HttpResponse response) {
		if (response == null) {
			return null;
		}

		try {
			InputStream is = AndroidHttpClient.getUngzippedContent(response.getEntity());
			return streamToString(is);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Converts an InputStream to String
	 *
	 * @param content
	 * @return
	 * @throws IOException
	 */
	private String streamToString(InputStream content) throws
			IOException {
		byte[] buffer = new byte[1024];
		int numRead;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		while ((numRead = content.read(buffer)) != -1) {
			baos.write(buffer, 0, numRead);
		}

		content.close();

		return new String(baos.toByteArray());
	}


	/**
	 * Returns new connection. referred to by given url.
	 */
	protected HttpURLConnection createConnection(String url) throws
			IOException {
		HttpURLConnection connection = (HttpURLConnection) (new URL(url)).openConnection();
		connection.setConnectTimeout(DEFAULT_CONNECT_TIMEOUT_MILLIS);
		connection.setReadTimeout(DEFAULT_READ_TIMEOUT_MILLIS);
		connection.setDoInput(true);
		return connection;
	}
}