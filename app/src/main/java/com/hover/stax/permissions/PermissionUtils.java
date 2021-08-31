package com.hover.stax.permissions;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.view.View;

import androidx.core.app.ActivityCompat;

import com.hover.sdk.permissions.PermissionHelper;
import com.hover.stax.R;
import com.hover.stax.utils.Utils;
import com.hover.stax.views.StaxDialog;

public class PermissionUtils {

    public static void requestPerms(int requestCode, Activity a) {
        PermissionHelper ph = new PermissionHelper(a);
        if (!ph.hasPhonePerm() && !ph.hasPhonePerm())
            Utils.logAnalyticsEvent(a.getString(R.string.perms_basic_requested), a);
        else if (!ph.hasPhonePerm())
            Utils.logAnalyticsEvent(a.getString(R.string.perms_phone_requested), a);
        else if (!ph.hasSmsPerm())
            Utils.logAnalyticsEvent(a.getString(R.string.perms_sms_requested), a);
        ph.requestBasicPerms(a, requestCode);
    }


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

    public static void logPermissionsGranted(int[] grantResults, Activity a) {
        PermissionHelper ph = new PermissionHelper(a);
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                logDenyResult(ph, a);
            }
        }

        Utils.logAnalyticsEvent(a.getString(R.string.perms_basic_granted), a);
    }

    private static void logDenyResult(PermissionHelper ph, Activity a) {
        if (!ph.hasPhonePerm() && !ph.hasPhonePerm())
            Utils.logAnalyticsEvent(a.getString(R.string.perms_basic_denied), a);
        else if (!ph.hasPhonePerm())
            Utils.logAnalyticsEvent(a.getString(R.string.perms_phone_denied), a);
        else if (!ph.hasSmsPerm())
            Utils.logAnalyticsEvent(a.getString(R.string.perms_sms_denied), a);
    }

    public static boolean hasContactPermission(Context c) {
        return Build.VERSION.SDK_INT < 23 || PermissionUtils.has(new String[]{Manifest.permission.READ_CONTACTS}, c);
    }

    public static boolean hasWritePermission(Context c) {
        return Build.VERSION.SDK_INT < 23 || PermissionUtils.has(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, c);
    }

    public static boolean hasSmsPermission(Context c) {
        return Build.VERSION.SDK_INT < 23 || PermissionUtils.has(new String[]{Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS}, c);
    }

    public static void showInformativeBasicPermissionDialog(int permissionMessage, View.OnClickListener posListener, View.OnClickListener negListener, Activity activity) {
        Utils.logAnalyticsEvent(activity.getString(R.string.perms_basic_dialog), activity);
        if (permissionMessage > 0) {
            new StaxDialog(activity)
                    .setDialogTitle(R.string.permissions_title)
                    .setDialogMessage(permissionMessage)
                    .setPosButton(R.string.btn_ok, posListener)
                    .setNegButton(R.string.btn_cancel, negListener)
                    .showIt();
        } else {
            new StaxDialog(activity, R.layout.basic_perm_dialog)
                    .setPosButton(R.string.btn_ok, posListener)
                    .setNegButton(R.string.btn_cancel, negListener)
                    .showIt();
        }
    }
}