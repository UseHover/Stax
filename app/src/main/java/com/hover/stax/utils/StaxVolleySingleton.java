package com.hover.stax.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.hover.sdk.BuildConfig;
import com.hover.sdk.R;
import com.hover.sdk.security.KeyHelper;
import com.hover.sdk.utils.AnalyticsSingleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.UnsupportedEncodingException;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

final public class StaxVolleySingleton {
	private final static String TAG = "VolleySingleton";
	@SuppressLint("StaticFieldLeak") // Singleton instance is recommended by android docs
	private static StaxVolleySingleton instance;
	private RequestQueue requestQueue;
	private final Context ctx;
	private final static long TIMEOUT_S = 300; // This is super long (5 min), but they may be downloading up to a couple MB on a very slow connection!

	private StaxVolleySingleton(Context context) {
		ctx = context;
		requestQueue = getRequestQueue();
	}

	public static synchronized StaxVolleySingleton getInstance(Context context) {
		if (instance == null)
			instance = new StaxVolleySingleton(context);
		return instance;
	}

	private RequestQueue getRequestQueue() {
		if (requestQueue == null)
			requestQueue = Volley.newRequestQueue(ctx.getApplicationContext(), new CustomHurlStack());
		return requestQueue;
	}

	private <T> void addToRequestQueue(Request<T> req) {
		getRequestQueue().add(req);
	}

	public static JSONObject uploadNow(Context c, int requestType, String url, JSONObject json) throws JSONException, InterruptedException, ExecutionException, TimeoutException {
		RequestFuture<JSONObject> future = RequestFuture.newFuture();
		StaxVolleySingleton.getInstance(c).addToRequestQueue(new JsonObjectRequest(requestType, url, json, future, future));
		return future.get(TIMEOUT_S, TimeUnit.SECONDS);
	}

	class CustomHurlStack extends HurlStack {
		@Override
		protected HttpURLConnection createConnection(URL url) throws IOException {
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestProperty("Authorization", "Token token=" + getApiKey());
			connection.setRequestProperty("X-SDK-VERSION", BuildConfig.VERSION_NAME);
			connection.setRequestProperty("X-APP-PACKAGE-NAME", Utils.getPackage(ctx));
			return connection;
		}
	}
	public String getApiKey() throws IOException {
		try {
			String key;
			if (KeyHelper.getApiKey(ctx) == null) {
				ApplicationInfo ai = ctx.getPackageManager().getApplicationInfo(Utils.getPackage(ctx), PackageManager.GET_META_DATA);
				key = ai.metaData.getString("com.hover.ApiKey");
			} else
				key = KeyHelper.getApiKey(ctx);
			Log.v(TAG, "apikey found: " + key);
			return key;
		} catch (PackageManager.NameNotFoundException e) { throw new IOException(ctx.getString(R.string.hsdk_log_no_package_name) + e); }
		catch (NullPointerException e) { throw new IOException(ctx.getString(R.string.hsdk_log_no_api_key) + e); }
	}

	public static boolean isConnected(Context c) {
		ConnectivityManager cm = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
	}

	public static void exceptionHandler(Context c, Exception e) { // May wish to report instances of com.android.volley.NoConnectionError: java.io.EOFException
		if (e instanceof ExecutionException && (e.getMessage().contains("ServerError") || e.getMessage().contains("AuthFailureError")))
			AnalyticsSingleton.capture(c, e);
	}
}
