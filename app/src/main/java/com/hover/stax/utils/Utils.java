package com.hover.stax.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.annotation.Nullable;

import com.hover.stax.ApplicationInstance;
import com.hover.stax.R;
import com.hover.stax.channels.Channel;
import com.hover.stax.models.StaxDate;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class Utils {

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


	public static String getPackage(Context c) {
		try {
			return c.getApplicationContext().getPackageName();
		} catch (NullPointerException e) {
			return "fail";
		}
	}

	public static List<Channel> getSimChannels(List<Channel> channels, List<String> simHniList) {
		List<Channel> simChannels = new ArrayList<>();
		for (int i = 0; i < channels.size(); i++) {
			String[] hniArr = channels.get(i).hniList.split(",");
			for (int l = 0; l < hniArr.length; l++) {
				if (simHniList.contains(Utils.stripHniString(hniArr[l])))
					if (!simChannels.contains(channels.get(i))) simChannels.add(channels.get(i));
			}
		}
		return simChannels;
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
}
