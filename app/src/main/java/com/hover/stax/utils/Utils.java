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


	public static String stripHniString(String hni) {
		return hni.replace("[", "").replace("]", "").replace("\"", "");
	}

	@SuppressLint({"HardwareIds", "MissingPermission"})
	public static String getDeviceId(Context c) {
		try {
			if (PermissionUtils.has(new String[]{Manifest.permission.READ_PHONE_STATE, Manifest.permission.CALL_PHONE}, c)) {
				String id = null;
				if (Build.VERSION.SDK_INT < 29) {
					try {
						id = ((TelephonyManager) c.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
					} catch (Exception ignored) {
					}
				}
				if (id == null) {
					id = Settings.Secure.getString(c.getContentResolver(), Settings.Secure.ANDROID_ID);
				}
				return id;
			}
		} catch (SecurityException ignored) {
		}
		return c.getString(R.string.hsdk_unknown_device_id);
	}

	public static String[] convertNormalJSONArrayToStringArray(JSONArray arr) throws JSONException {
		if (arr == null) return new String[]{};
		String[] list = new String[arr.length()];
		for (int i = 0; i < arr.length(); i++) {
			list[i] = arr.getString(i);
		}
		return list;
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
