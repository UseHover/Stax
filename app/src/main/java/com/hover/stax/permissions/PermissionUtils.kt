/*
 * Copyright 2022 Stax
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hover.stax.permissions

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.view.View
import androidx.core.app.ActivityCompat
import com.hover.sdk.permissions.PermissionHelper
import com.hover.stax.R
import com.hover.stax.core.AnalyticsUtil.logAnalyticsEvent
import com.hover.stax.views.StaxDialog
import timber.log.Timber

object PermissionUtils {

    fun requestPerms(requestCode: Int, a: Activity) {
        val ph = PermissionHelper(a)

        when {
            !ph.hasPhonePerm() && !ph.hasSmsPerm() -> logAnalyticsEvent(a.getString(R.string.perms_basic_requested), a)
            !ph.hasPhonePerm() -> logAnalyticsEvent(a.getString(R.string.perms_phone_requested), a)
            !ph.hasSmsPerm() -> logAnalyticsEvent(a.getString(R.string.perms_sms_requested), a)
        }

        Timber.e("Requesting basic permissions")
        ph.requestBasicPerms(a, requestCode)
    }

    fun has(permissions: Array<String>?, context: Context?): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (permission in permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false
                }
            }
        }
        return true
    }

    fun logPermissionsGranted(grantResults: IntArray, a: Activity) {
        val ph = PermissionHelper(a)
        for (result in grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                logDenyResult(ph, a)
            }
        }
        logAnalyticsEvent(a.getString(R.string.perms_basic_granted), a)
    }

    private fun logDenyResult(ph: PermissionHelper, a: Activity) {
        when {
            !ph.hasPhonePerm() && !ph.hasSmsPerm() -> logAnalyticsEvent(a.getString(R.string.perms_basic_denied), a)
            !ph.hasPhonePerm() -> logAnalyticsEvent(a.getString(R.string.perms_phone_denied), a)
            !ph.hasSmsPerm() -> logAnalyticsEvent(a.getString(R.string.perms_sms_denied), a)
        }
    }

    fun hasContactPermission(c: Context?): Boolean {
        return Build.VERSION.SDK_INT < 23 || has(arrayOf(Manifest.permission.READ_CONTACTS), c)
    }

    fun hasWritePermission(c: Context?): Boolean {
        return Build.VERSION.SDK_INT < 23 || has(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), c)
    }

    fun hasSmsPermission(c: Context?): Boolean {
        return Build.VERSION.SDK_INT < 23 || has(arrayOf(Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS), c)
    }

    fun hasPhonePermission(c: Context?): Boolean {
        return Build.VERSION.SDK_INT < 23 || has(arrayOf(Manifest.permission.READ_PHONE_STATE), c)
    }

    fun showInformativeBasicPermissionDialog(
        permissionMessage: Int,
        posListener: View.OnClickListener?,
        negListener: View.OnClickListener?,
        activity: Activity
    ) {
        logAnalyticsEvent(activity.getString(R.string.perms_basic_dialog), activity)
        if (permissionMessage > 0) {
            StaxDialog(activity)
                .setDialogTitle(R.string.permissions_title)
                .setDialogMessage(permissionMessage)
                .setPosButton(R.string.btn_ok, posListener)
                .setNegButton(R.string.btn_cancel, negListener)
                .showIt()
        } else {
            StaxDialog(activity, R.layout.dialog_basic_perms)
                .setPosButton(R.string.btn_continue, posListener)
                .setNegButton(R.string.btn_not_now, negListener)
                .showIt()
        }
    }
}