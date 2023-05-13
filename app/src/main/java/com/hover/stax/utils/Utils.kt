/*
 * Copyright 2023 Stax
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
package com.hover.stax.utils

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.annotation.StringRes
import com.google.firebase.messaging.FirebaseMessaging
import com.hover.stax.R
import com.hover.stax.core.Utils
import com.hover.stax.permissions.PermissionUtils
import com.hover.stax.utils.AnalyticsUtil.logAnalyticsEvent
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber

object Utils {

    fun usingDebugVariant(c: Context): Boolean {
        return getBuildConfigValue(c, "DEBUG") as Boolean
    }

    private fun getBuildConfigValue(context: Context, fieldName: String): Any? {
        try {
            val clazz = Class.forName(Utils.getPackage(context) + ".BuildConfig")
            val field = clazz.getField(fieldName)
            return field[null]
        } catch (e: Exception) {
            Timber.d(e, "Error getting build config value")
        }
        return false
    }

    @JvmStatic
    fun copyToClipboard(content: String?, c: Context): Boolean {
        val clipboard = c.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        val clip = ClipData.newPlainText("Stax content", content)
        if (clipboard != null) {
            clipboard.setPrimaryClip(clip)
            UIHelper.flashAndReportMessage(c, c.getString(R.string.copied))
            return true
        }
        return false
    }

    fun setFirebaseMessagingTopic(topic: String?) {
        FirebaseMessaging.getInstance().subscribeToTopic(topic!!)
    }

    fun removeFirebaseMessagingTopic(topic: String?) {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(topic!!)
    }

    @JvmStatic
    fun showSoftKeyboard(context: Context, view: View) {
        if (view.requestFocus()) {
            val inputMethodManager =
                context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    fun openUrl(url: String?, ctx: Context) {
        val i = Intent(Intent.ACTION_VIEW)
        i.data = Uri.parse(url)

        try {
            ctx.startActivity(i)
        } catch (e: ActivityNotFoundException) {
            Timber.e("No activity found to handle intent")
        }
    }

    fun openUrl(urlRes: Int, ctx: Context) {
        openUrl(ctx.resources.getString(urlRes), ctx)
    }

    fun openEmail(@StringRes subject: Int, context: Context, body: String? = "") {
        openEmail(context.getString(subject), context, body)
    }

    fun openEmail(subject: String, context: Context, body: String? = "") {
        val intent = Intent(Intent.ACTION_VIEW)
        val senderEmail = context.getString(R.string.stax_support_email)
        intent.data = Uri.parse("mailto:$senderEmail ?subject=$subject&body=$body")
        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Timber.e("Activity not found")
            UIHelper.flashAndReportMessage(
                context,
                context.getString(R.string.email_client_not_found)
            )
        }
    }

    fun shareStax(activity: Activity, shareMessage: String? = null) {
        logAnalyticsEvent(activity.getString(R.string.clicked_share), activity)

        val sharingIntent = Intent(Intent.ACTION_SEND)
        sharingIntent.type = "text/plain"
        sharingIntent.putExtra(Intent.EXTRA_SUBJECT, activity.getString(R.string.share_sub))
        sharingIntent.putExtra(
            Intent.EXTRA_TEXT,
            shareMessage ?: activity.getString(R.string.share_msg)
        )
        activity.startActivity(
            Intent.createChooser(
                sharingIntent,
                activity.getString(R.string.share_explain)
            )
        )
    }

    fun openStaxPlaystorePage(activity: Activity) {
        val link = Uri.parse(activity.baseContext.getString(R.string.stax_market_playstore_link))
        val goToMarket = Intent(Intent.ACTION_VIEW, link)
        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_NEW_DOCUMENT or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
        try {
            activity.startActivity(goToMarket)
        } catch (e: ActivityNotFoundException) {
            activity.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(
                        activity.baseContext.getString(
                            R.string.stax_url_playstore_review_link
                        )
                    )
                )
            )
        }
    }

    @JvmStatic
    fun dial(shortCode: String, c: Context) {
        val data = JSONObject()
        try {
            data.put("shortcode", shortCode)
        } catch (ignored: JSONException) {
        }

        logAnalyticsEvent(c.getString(R.string.clicked_dial_shortcode), data, c)

        val dialIntent = Intent(
            Intent.ACTION_CALL,
            Uri.parse("tel:".plus(shortCode.replace("#", Uri.encode("#"))))
        ).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        if (PermissionUtils.has(arrayOf(Manifest.permission.CALL_PHONE), c))
            c.startActivity(dialIntent)
        else
            UIHelper.flashAndReportMessage(c, c.getString(R.string.enable_call_permission))
    }
}