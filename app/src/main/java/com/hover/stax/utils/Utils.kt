package com.hover.stax.utils

import android.Manifest
import android.app.Activity
import android.content.*
import android.net.ConnectivityManager
import android.net.Uri
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.google.firebase.messaging.FirebaseMessaging
import com.hover.stax.R
import com.hover.stax.permissions.PermissionUtils
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber
import java.text.DecimalFormat

object Utils {
    private const val SHARED_PREFS = "staxprefs"

    private fun getSharedPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(getPackage(context) + SHARED_PREFS, Context.MODE_PRIVATE)
    }

    @JvmStatic
    fun saveString(key: String?, value: String?, c: Context) {
        val editor = getSharedPrefs(c).edit()
        editor.putString(key, value)
        editor.commit()
    }

    @JvmStatic
    fun getString(key: String?, c: Context): String? {
        return getSharedPrefs(c).getString(key, "")
    }

    @JvmStatic
    fun getBoolean(key: String?, c: Context): Boolean {
        return getSharedPrefs(c).getBoolean(key, false)
    }

    @JvmStatic
    fun saveInt(key: String?, value: Int, c: Context) {
        val editor = getSharedPrefs(c).edit()
        editor.putInt(key, value)
        editor.apply()
    }

    @JvmStatic
    fun saveBoolean(key: String?, value: Boolean, c: Context) {
        val editor = getSharedPrefs(c).edit()
        editor.putBoolean(key, value)
        editor.apply()
    }

    fun getInt(key: String?, c: Context?): Int {
        return getSharedPrefs(c!!).getInt(key, 0)
    }

    fun getLong(key: String?, c: Context?): Long {
        return getSharedPrefs(c!!).getLong(key, 0)
    }

    fun saveLong(key: String?, value: Long, c: Context?) {
        val editor = getSharedPrefs(c!!).edit()
        editor.putLong(key, value)
        editor.apply()
    }

    @JvmStatic
    fun isFirebaseTopicInDefaultState(topic: String?, c: Context): Boolean {
        return getSharedPrefs(c).getBoolean(topic, true)
    }

    @JvmStatic
    fun alterFirebaseTopicState(topic: String?, c: Context) {
        val editor = getSharedPrefs(c).edit()
        editor.putBoolean(topic, false)
        editor.apply()
    }

    @JvmStatic
    fun stripHniString(hni: String): String {
        return hni.replace("[", "").replace("]", "").replace("\"", "")
    }

    @JvmStatic
    fun getPackage(c: Context): String {
        return try {
            c.applicationContext.packageName
        } catch (e: NullPointerException) {
            "fail"
        }
    }

    @JvmStatic
    fun getAppName(c: Context?): String {
        return if (c != null && c.applicationContext.applicationInfo != null)
            c.applicationContext.applicationInfo.loadLabel(c.packageManager).toString()
        else "Hover"
    }

    @JvmStatic
    fun formatAmount(number: String): String {
        return if (number == "0") "0,000" else try {
            formatAmount(getAmount(number))
        } catch (e: Exception) {
            number
        }
    }

    @JvmStatic
    fun formatAmount(number: Double): String {
        return try {
            val formatter = DecimalFormat("#,##0.00")
            formatter.maximumFractionDigits = 0
            formatter.format(number)
        } catch (e: Exception) {
            number.toString()
        }
    }

    @JvmStatic
    fun getAmount(amount: String): Double {
        return amount.replace(",".toRegex(), "").toDouble()
    }

    @JvmStatic
    fun usingDebugVariant(c: Context): Boolean {
        return getBuildConfigValue(c, "DEBUG") as Boolean
    }

    private fun getBuildConfigValue(context: Context, fieldName: String?): Any? {
        try {
            val clazz = Class.forName(getPackage(context) + ".BuildConfig")
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
            UIHelper.flashMessage(c, c.getString(R.string.copied))
            return true
        }
        return false
    }


    fun isInternetConnected(c: Context): Boolean {
        val cm = c.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = cm.activeNetworkInfo
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting
    }

    @JvmStatic
    fun setFirebaseMessagingTopic(topic: String?) {
        FirebaseMessaging.getInstance().subscribeToTopic(topic!!)
    }

    @JvmStatic
    fun removeFirebaseMessagingTopic(topic: String?) {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(topic!!)
    }





    @JvmStatic
    fun showSoftKeyboard(context: Context, view: View) {
        if (view.requestFocus()) {
            val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    @JvmStatic
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

    @JvmStatic
    fun openEmail(subject: String, context: Context) {
        val intent = Intent(Intent.ACTION_VIEW)
        val senderEmail = context.getString(R.string.stax_support_email)
        intent.data = Uri.parse("mailto:$senderEmail ?subject=$subject")
        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Timber.e("Activity not found")
            UIHelper.flashMessage(context, context.getString(R.string.email_client_not_found))
        }
    }

    @JvmStatic
    fun shareStax(activity: Activity) {
        AnalyticsUtil.logAnalyticsEvent(activity.getString(R.string.clicked_share), activity)
        val sharingIntent = Intent(Intent.ACTION_SEND)
        sharingIntent.type = "text/plain"
        sharingIntent.putExtra(Intent.EXTRA_SUBJECT, activity.getString(R.string.share_sub))
        sharingIntent.putExtra(Intent.EXTRA_TEXT, activity.getString(R.string.share_msg))
        activity.startActivity(Intent.createChooser(sharingIntent, activity.getString(R.string.share_explain)))
    }

    @JvmStatic
    fun openStaxPlaystorePage(activity: Activity) {
        val link = Uri.parse(activity.baseContext.getString(R.string.stax_market_playstore_link))
        val goToMarket = Intent(Intent.ACTION_VIEW, link)
        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_NEW_DOCUMENT or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
        try {
            activity.startActivity(goToMarket)
        } catch (e: ActivityNotFoundException) {
            activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(activity.baseContext.getString(R.string.stax_url_playstore_review_link))))
        }
    }

    @JvmStatic
    fun dial(shortCode: String, c: Context) {
        val data = JSONObject()
        try {
            data.put("shortcode", shortCode)
        } catch (ignored: JSONException) {
        }
        AnalyticsUtil.logAnalyticsEvent(c.getString(R.string.clicked_dial_shortcode), data, c)

        val dialIntent = Intent(Intent.ACTION_CALL, Uri.parse("tel:".plus(shortCode.replace("#", Uri.encode("#"))))).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        if (PermissionUtils.has(arrayOf(Manifest.permission.CALL_PHONE), c))
            c.startActivity(dialIntent)
        else
            UIHelper.flashMessage(c, c.getString(R.string.enable_call_permission))
    }
}