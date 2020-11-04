package com.hover.stax.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.hover.sdk.permissions.PermissionHelper;
import com.hover.sdk.utils.AnalyticsSingleton;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.text.DecimalFormat;

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

	public static void saveInt(String key, int value, Context c) {
		SharedPreferences.Editor editor = getSharedPrefs(c).edit();
		editor.putInt(key, value);
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
			formatter.setMaximumFractionDigits(2);
			return formatter.format(number);
		} catch (Exception e) {
			return String.valueOf(number);
		}
	}

	public static Double getAmount(String amount) {
		return Double.parseDouble(amount.replaceAll(",", ""));
	}

	public static String normalizePhoneNumber(String value, String country) {
		PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
		String number = value;
		try {
			Phonenumber.PhoneNumber phone = phoneUtil.parse(value, country);
			number = phoneUtil.formatNumberForMobileDialing(phone, country, false);
			Log.e(TAG, "Normalized number: " + number);
		} catch (NumberParseException e) {
			Log.e(TAG, "error formating number", e);
		}
		return number;
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

	public static boolean validateEmail(String string) {
		if(string == null) return  false;
		return string.matches("(?:[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)])");
	}
	public static byte[] bitmapToByteArray(Bitmap bitmap) {
		if (bitmap == null) return null;
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
		return stream.toByteArray();
	}

	@SuppressLint({"HardwareIds", "MissingPermission"})
	public static String getDeviceId(Context c) {
		try {
			if (new PermissionHelper(c).hasPhonePerm()) {
				String id = null;
				if (Build.VERSION.SDK_INT < 29) {
					try {
						id = ((TelephonyManager) c.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
					} catch (Exception ignored) {}
				}
				if (id == null) { id = Settings.Secure.getString(c.getContentResolver(), Settings.Secure.ANDROID_ID); }
				return id;
			}
		} catch (SecurityException e) {  AnalyticsSingleton.capture(c, e); }
		return c.getString(com.hover.sdk.R.string.hsdk_unknown_device_id);
	}

}
