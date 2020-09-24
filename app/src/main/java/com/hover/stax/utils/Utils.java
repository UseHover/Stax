package com.hover.stax.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.hover.stax.R;

import org.json.JSONArray;
import org.json.JSONException;

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
		editor.commit();
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
			double amount = Double.parseDouble(number);
			DecimalFormat formatter = new DecimalFormat("#,###.00");
			return formatter.format(amount);
		} catch (Exception e) {
			return number;
		}
	}

	public static String formatAmountV2(String amount) {
		DecimalFormat df = new DecimalFormat("0.00");
		df.setMaximumFractionDigits(2);
		return df.format(Integer.valueOf(amount));
	}
	public static String formatAmountV2(double amount) {
		DecimalFormat df = new DecimalFormat("0.00");
		df.setMaximumFractionDigits(2);
		return df.format(amount);
	}

	public static Double getAmount(String amount) {return Double.valueOf(formatAmountV2(amount));}

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
}
