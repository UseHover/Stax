package com.hover.stax.utils;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;

import com.amplitude.api.Amplitude;
import com.appsflyer.AppsFlyerLib;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.messaging.FirebaseMessaging;
import com.hover.stax.BuildConfig;
import com.hover.stax.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import timber.log.Timber;

import static android.content.Context.CLIPBOARD_SERVICE;

public class Utils {
	private final static String TAG = "Utils";

	private static final String SHARED_PREFS = "staxprefs";

	public static SharedPreferences getSharedPrefs(Context context) {
		return context.getSharedPreferences(getPackage(context) + SHARED_PREFS, Context.MODE_PRIVATE);
	}

	public static void saveString(String key, String value, Context c) {
		SharedPreferences.Editor editor = getSharedPrefs(c).edit();
		editor.putString(key, value);
		editor.commit();
	}

	public static String getString(String key, Context c) { return getSharedPrefs(c).getString(key, ""); }
	public static boolean getBoolean(String key, Context c) { return getSharedPrefs(c).getBoolean(key, false); }

	public static void saveInt(String key, int value, Context c) {
		SharedPreferences.Editor editor = getSharedPrefs(c).edit();
		editor.putInt(key, value);
		editor.apply();
	}

	public static void saveBoolean(String key, boolean value, Context c) {
		SharedPreferences.Editor editor = getSharedPrefs(c).edit();
		editor.putBoolean(key, value);
		editor.apply();
	}

	public static boolean isFirebaseTopicInDefaultState(String topic, Context c) {
		return getSharedPrefs(c).getBoolean(topic, true);
	}

	public static void alterFirebaseTopicState(String topic, Context c) {
		SharedPreferences.Editor editor = getSharedPrefs(c).edit();
		editor.putBoolean(topic, false);
		editor.apply();
	}

	public static String stripHniString(String hni) {
		return hni.replace("[", "").replace("]", "").replace("\"", "");
	}

	public static String getPackage(Context c) {
		try {
			return c.getApplicationContext().getPackageName();
		} catch (NullPointerException e) {
			return "fail";
		}
	}

	public static String getAppName(Context c) {
		return (c != null && c.getApplicationContext().getApplicationInfo() != null) ? c.getApplicationContext().getApplicationInfo().loadLabel(c.getPackageManager()).toString() : "Hover";
	}

	public static String formatAmount(String number) {
		try {
			return formatAmount(getAmount(number));
		} catch (Exception e) {
			return number;
		}
	}

	public static String formatAmount(Double number) {
		try {
			DecimalFormat formatter = new DecimalFormat("#,##0.00");
			formatter.setMaximumFractionDigits(0);
			return formatter.format(number);
		} catch (Exception e) {
			return String.valueOf(number);
		}
	}

	public static Double getAmount(String amount) {
		return Double.parseDouble(amount.replaceAll(",", ""));
	}

	public static boolean usingDebugVariant(Context c) {
		return (Boolean) getBuildConfigValue(c, "DEBUG");
	}

	@SuppressWarnings("SameParameterValue")
	public static Object getBuildConfigValue(Context context, String fieldName) {
		try {
			Class<?> clazz = Class.forName(getPackage(context) + ".BuildConfig");
			Field field = clazz.getField(fieldName);
			return field.get(null);
		} catch (Exception e) {
			Log.d(TAG, "Error getting build config value", e);
		}
		return false;
	}

	public static byte[] bitmapToByteArray(Bitmap bitmap) {
		if (bitmap == null) return null;
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
		return stream.toByteArray();
	}

	public static boolean copyToClipboard(String content, Context c) {
		ClipboardManager clipboard = (ClipboardManager) c.getSystemService(CLIPBOARD_SERVICE);
		ClipData clip = ClipData.newPlainText("Stax payment link", content);
		if(clipboard!=null) {
			clipboard.setPrimaryClip(clip);
			return true;
		}
		return false;
	}
	public static void logErrorAndReportToFirebase(String tag, String message, Exception e) {
		Timber.e(e, message);
		if(BuildConfig.BUILD_TYPE.equals("release")) FirebaseCrashlytics.getInstance().recordException(e);
	}

	public static boolean isInternetConnected(Context c) {
		ConnectivityManager cm = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
	}
	public static void setFirebaseMessagingTopic(String topic){
		FirebaseMessaging.getInstance().subscribeToTopic(topic);
	}

	public static void removeFirebaseMessagingTopic(String topic){
		FirebaseMessaging.getInstance().unsubscribeFromTopic(topic);
	}
	public static void logAnalyticsEvent(String event, Context context) {
		Amplitude.getInstance().logEvent(event);
		FirebaseAnalytics.getInstance(context).logEvent(strippedForFireAnalytics(event), null);
		AppsFlyerLib.getInstance().logEvent(context, event, null);
	}
	public static void logAnalyticsEvent(@NonNull  String event, @NonNull JSONObject args, @NonNull Context context) {
		Bundle bundle = convertJSONObjectToBundle(args);
		Map<String, Object> map = convertJSONObjectToHashMap(args);
		Amplitude.getInstance().logEvent(event, args);
		FirebaseAnalytics.getInstance(context).logEvent(strippedForFireAnalytics(event), bundle);
		AppsFlyerLib.getInstance().logEvent(context, event, map);
	}
	private static String strippedForFireAnalytics(String firebaseEventLog) {
		return firebaseEventLog.replace(" ","_").toLowerCase();
	}
	private static Bundle convertJSONObjectToBundle(JSONObject args) {
		Bundle bundle = new Bundle();
		Iterator<String> iter = args.keys();
		while(iter.hasNext()){
			String key = iter.next();
			String value = null;
			try {
				value = args.get(key).toString();
			} catch (JSONException e) {
				e.printStackTrace();
			}
			bundle.putString(strippedForFireAnalytics(key),value);
		}
		return bundle;
	}
	private static Map<String, Object> convertJSONObjectToHashMap(JSONObject args) {
		Map<String, Object> map = new HashMap<>();
		Iterator<String> iter = args.keys();
		while(iter.hasNext()){
			String key = iter.next();
			String value = null;
			try {
				value = args.get(key).toString();
			} catch (JSONException e) {
				e.printStackTrace();
			}
			map.put(key,value);
		}
		return map;
	}

	public static void showSoftKeyboard(Context context, View view) {
		if(view.requestFocus()){
			InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
			inputMethodManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
		}
	}

	public static void hideSoftKeyboard(Context context, View view){
		InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
		inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
	}

	public static void openUrl(String url, Context ctx) {
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setData(Uri.parse(url));
		ctx.startActivity(i);
	}
	public static void openStaxPlaystorePage(Activity activity) {
		Uri link = Uri.parse(activity.getBaseContext().getString(R.string.stax_market_playstore_link));
		Intent goToMarket = new Intent(Intent.ACTION_VIEW, link);
		goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_NEW_DOCUMENT | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
		try {
			activity.startActivity(goToMarket);
		} catch (ActivityNotFoundException e) {
			activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(activity.getBaseContext().getString(R.string.stax_url_playstore_review_link))));
		}
	}

	public static boolean isNetworkAvailable( Context context) {
		if (context == null) return false;
		ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

		if (connectivityManager != null) {

			if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
				NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
				if (capabilities != null) {
					if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
						return true;
					} else return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI);
				} else {
					try {
						NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
						if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
							return true;
						}
					} catch (Exception e) {
						Timber.e(e);
					}
				}
			}
			return false;
		} else return false;
	}

}
