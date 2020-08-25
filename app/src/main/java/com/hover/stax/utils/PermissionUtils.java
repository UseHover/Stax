package com.hover.stax.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;

public class PermissionUtils {

	public static boolean has(String[] permissions, Context context) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
			for (String permission : permissions) {
				if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
					return false;
				}
			}
		}
		return true;
	}

	public static boolean hasPhonePerm(Context c) {
		return Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
	       (c.checkSelfPermission(Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED &&
		        c.checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED);
	}

	public static void requestPhonePerms(Activity act, int requestCode) {
		ActivityCompat.requestPermissions(act, new String[]{Manifest.permission.CALL_PHONE, Manifest.permission.READ_PHONE_STATE}, requestCode);
	}

	public static boolean permissionsGranted(int[] grantResults) {
		for (int result: grantResults) {
			if (result != PackageManager.PERMISSION_GRANTED) { return false; }
		}
		return grantResults.length > 0;
	}
}
