package com.example.testapp;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.os.Build;
import android.os.StrictMode;
import android.util.Log;

@TargetApi(Build.VERSION_CODES.GINGERBREAD)
@SuppressLint("NewApi")
public class HttpUtil implements IConnectServer{

	// Get the HttpGet Object through URL
	public HttpGet getHttpGet(String url) {
		// realize HttpGet
		HttpGet request = new HttpGet(url);
		return request;
	}

	// Get the HttpPost Object through URL
	public HttpPost getHttpPost(String url) {
		HttpPost request = new HttpPost(url);
		return request;
	}

	// Get the HttpResponse Object through HttpGet
	public HttpResponse getHttpResponse(HttpGet request)
			throws ClientProtocolException, IOException {
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
				.permitAll().build();
		StrictMode.setThreadPolicy(policy);
		HttpResponse response = new DefaultHttpClient().execute(request);
		Log.wtf("response code(Get)", response.getStatusLine().getStatusCode()
				+ "");
		return response;
	}

	// Get the HttpResponse Object through HttpPost
	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	@SuppressLint("NewApi")
	public HttpResponse getHttpResponse(HttpPost request)
			throws ClientProtocolException, IOException {
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
				.permitAll().build();
		StrictMode.setThreadPolicy(policy);

		HttpResponse response = new DefaultHttpClient().execute(request);
		Log.wtf("response code(Post)", response.getStatusLine().getStatusCode()
				+ "");
		// Log.d("response code", response.getStatusLine().getStatusCode() +
		// "");
		return response;
	}

	// Send Post Request through Url, Return the Result
	public String queryStringForPost(String url) {
		Log.d("queryStringForPost url=", url);
		HttpPost request = getHttpPost(url);
		String result = null;
		try {
			HttpResponse response = getHttpResponse(request);
			// If Request is Success
			if (response.getStatusLine().getStatusCode() == 200) {
				result = EntityUtils.toString(response.getEntity());
				Log.d("post==200", "testing");
				return result;
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			result = "Network Exception of ClientProtocolException";
			return result;
		} catch (IOException e) {
			e.printStackTrace();
			result = "Network Exception of IOException";
			return result;
		}

		Log.d("post==null", "testing");

		return null;
	}

	// Send Get Request through Url, Return the Result
	public String queryStringForGet(String url) {
		HttpGet request = getHttpGet(url);
		String result = null;
		Log.d("queryStringForGet url=", url);
		try {
			HttpResponse response = getHttpResponse(request);

			if (response.getStatusLine().getStatusCode() == 200) {
				result = EntityUtils.toString(response.getEntity());
				Log.d("Result = ", result);
				return result;
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			return result;
		} catch (IOException e) {
			e.printStackTrace();
			return result;
		}
		Log.d("Result = ", result);
		Log.d("==null", "testing");
		return null;
	}

	// Send Post Request through HttpPost, Return the Result
	public String queryStringForPost(HttpPost request) {
		String result = null;

		try {
			HttpResponse response = getHttpResponse(request);
			if (response.getStatusLine().getStatusCode() == 200) {
				result = EntityUtils.toString(response.getEntity());
				return result;
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			result = "Network Exception of ClientProtocolException";
			return result;
		} catch (IOException e) {
			e.printStackTrace();
			result = "Network Exception of IOException";
			return result;
		}
		return null;
	}
}
