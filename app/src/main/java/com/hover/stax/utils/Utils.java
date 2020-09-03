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

import com.hover.stax.R;
import com.hover.stax.channels.Channel;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class Utils {

	public static String stripHniString(String hni) {
		return hni.replace("[", "").replace("]", "").replace("\"", "");
	}

	public static String formatDate(long timestamp) {
		String pattern = "HH:mm:ss (z) MMM dd, yyyy";
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern, Locale.getDefault());
		return simpleDateFormat.format(timestamp);
	}

	public static String formatDateV2(@Nullable long timestamp) {

		String pattern = "MMM dd";
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern, Locale.getDefault());
		return simpleDateFormat.format(timestamp);
	}

	public static String formatDateV3(long timestamp) {
		String pattern = "MMM dd, yyyy";
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern, Locale.getDefault());
		return simpleDateFormat.format(timestamp);
	}

	public static String nullToString(Object value) {
		if(value == null) return  "None";
		else return value.toString();
	}

	public static List<?> removeDuplicatesFromList(List<?> list) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
			list = list.stream().distinct().collect(Collectors.toList());
		} else {
			LinkedHashSet<Object> hashSet = new LinkedHashSet<>(list);
			list = new ArrayList<>(hashSet);
		}
		return list;
	}
	static public  Object nonNullDateRange(Object value) {
		if(value == null) return 0;
		else return value;
	}

	@SuppressLint({"HardwareIds", "MissingPermission"})
	public static String getDeviceId(Context c) {
		try {
			if (PermissionUtils.has(new String[]{ Manifest.permission.READ_PHONE_STATE, Manifest.permission.CALL_PHONE}, c)) {
				String id = null;
				if (Build.VERSION.SDK_INT < 29) {
					try {
						id = ((TelephonyManager) c.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
					} catch (Exception ignored) {}
				}
				if (id == null) { id = Settings.Secure.getString(c.getContentResolver(), Settings.Secure.ANDROID_ID); }
				return id;
			}
		} catch (SecurityException ignored) {  }
		return c.getString(R.string.hsdk_unknown_device_id);
	}


	public static String[] convertNormalJSONArrayToStringArray(JSONArray arr) throws JSONException {
		if(arr == null) return new String[]{};
		String[] list = new String[arr.length()];
		for(int i = 0; i < arr.length(); i++){
			list[i] = arr.getString(i);
		}
		return list;
	}

	public static void saveImageLocally(Bitmap b, String imageName, Context context) {
		FileOutputStream foStream;
		try {
			foStream = context.openFileOutput(imageName, Context.MODE_PRIVATE);
			b.compress(Bitmap.CompressFormat.PNG, 100, foStream);
			foStream.close();
		} catch (Exception e) {
			Log.d("saveImage", "Exception 2, Something went wrong!");
			e.printStackTrace();
		}
	}

	public static Bitmap loadImageBitmap(String imageName, Context context) {
		Bitmap bitmap = null;
		FileInputStream fiStream;
		try {
			fiStream    = context.openFileInput(imageName);
			bitmap      = BitmapFactory.decodeStream(fiStream);
			fiStream.close();
		} catch (Exception e) {
			Log.d("saveImage", "Exception 3, Something went wrong!");
			e.printStackTrace();
		}
		return bitmap;
	}

	public static Bitmap downloadImageBitmap(String sUrl) {
		Bitmap bitmap = null;
		try {
			InputStream inputStream = new URL(sUrl).openStream();   // Download Image from URL
			bitmap = BitmapFactory.decodeStream(inputStream);       // Decode Bitmap
			inputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bitmap;
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
				Log.d("CUS_", "data hni: "+ Utils.stripHniString(hniArr[l]));
				if (simHniList.contains(Utils.stripHniString(hniArr[l])))
					simChannels.add(channels.get(i));
			}
		}
		return simChannels;
	}

	public static String formatAmount(String number) {
			try{
				double amount = Double.parseDouble(number);
				DecimalFormat formatter = new DecimalFormat("#,###.00");
				return formatter.format(amount);
			}
			catch (Exception e) {
				return number;
			}
	}
}
