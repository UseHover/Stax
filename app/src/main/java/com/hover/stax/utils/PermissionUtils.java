package com.hover.stax.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;

import com.hover.stax.ApplicationInstance;

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
	
	public static boolean permissionsGranted(int[] grantResults) {
		for (int result : grantResults) {
			if (result != PackageManager.PERMISSION_GRANTED) {
				return false;
			}
		}
		return grantResults.length > 0;
	}

	public static boolean hasContactPermission() {
		return PermissionUtils.has(new String[]{Manifest.permission.READ_CONTACTS}, ApplicationInstance.getContext());
	}

	public static boolean hasSendSMSPermission() {
		return PermissionUtils.has(new String[]{Manifest.permission.SEND_SMS}, ApplicationInstance.getContext());
	}
}
