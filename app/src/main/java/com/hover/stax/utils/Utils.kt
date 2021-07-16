package com.hover.stax.utils

import android.app.Activity
import android.content.*
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.amplitude.api.Amplitude
import com.appsflyer.AppsFlyerLib
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.messaging.FirebaseMessaging
import com.hover.stax.BuildConfig
import com.hover.stax.R
import com.mixpanel.android.mpmetrics.MixpanelAPI
import org.json.JSONException
import org.json.JSONObject
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.text.DecimalFormat
import java.util.*
import kotlin.properties.Delegates


object Utils : KoinComponent {

    private val mixPanel: MixpanelAPI by inject()
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

    fun bitmapToByteArray(bitmap: Bitmap?): ByteArray? {
        if (bitmap == null) return null
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    }

    @JvmStatic
    fun copyToClipboard(content: String?, c: Context): Boolean {
        val clipboard = c.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        val clip = ClipData.newPlainText("Stax payment link", content)
        if (clipboard != null) {
            clipboard.setPrimaryClip(clip)
            return true
        }
        return false
    }

    @JvmStatic
    fun logErrorAndReportToFirebase(tag: String, message: String, e: Exception?) {
        Timber.e(e, message)
        if (BuildConfig.BUILD_TYPE == "release") {
            if (e != null) FirebaseCrashlytics.getInstance().recordException(e) else FirebaseCrashlytics.getInstance().log("$tag - $message")
        }
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
    fun logAnalyticsEvent(event: String, context: Context?) {
        Amplitude.getInstance().logEvent(event)
        FirebaseAnalytics.getInstance(context!!).logEvent(strippedForFireAnalytics(event), null)
        AppsFlyerLib.getInstance().logEvent(context, event, null)
        mixPanel.track(event)
    }

    @JvmStatic
    fun timeEvent(event: String){
        mixPanel.timeEvent(event)
    }

    @JvmStatic
    fun logAnalyticsEvent(event: String, args: JSONObject, context: Context) {
        val bundle = convertJSONObjectToBundle(args)
        val map = convertJSONObjectToHashMap(args)
        Amplitude.getInstance().logEvent(event, args)
        FirebaseAnalytics.getInstance(context).logEvent(strippedForFireAnalytics(event), bundle)
        AppsFlyerLib.getInstance().logEvent(context, event, map)
        mixPanel.track(event, args)
    }

    private fun strippedForFireAnalytics(firebaseEventLog: String): String {
        return firebaseEventLog.replace(" ", "_").lowercase()
    }

    private fun convertJSONObjectToBundle(args: JSONObject): Bundle {
        val bundle = Bundle()
        val iter = args.keys()
        while (iter.hasNext()) {
            val key = iter.next()
            var value: String? = null
            try {
                value = args[key].toString()
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            bundle.putString(strippedForFireAnalytics(key), value)
        }
        return bundle
    }

    private fun convertJSONObjectToHashMap(args: JSONObject): Map<String, Any?> {
        val map: MutableMap<String, Any?> = HashMap()
        val iter = args.keys()
        while (iter.hasNext()) {
            val key = iter.next()
            var value: String? = null
            try {
                value = args[key].toString()
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            map[key] = value
        }
        return map
    }

    @JvmStatic
    fun showSoftKeyboard(context: Context, view: View) {
        if (view.requestFocus()) {
            val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    fun hideSoftKeyboard(context: Context, view: View) {
        val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    @JvmStatic
    fun openUrl(url: String?, ctx: Context) {
        val i = Intent(Intent.ACTION_VIEW)
        i.data = Uri.parse(url)
        ctx.startActivity(i)
    }

    fun openUrl(urlRes: Int, ctx: Context) {
        openUrl(ctx.resources.getString(urlRes), ctx)
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
    fun isNetworkAvailable(context: Context?): Boolean {
        if (context == null) return false
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        connectivityManager?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val capabilities = it.getNetworkCapabilities(connectivityManager.activeNetwork)
                if (capabilities != null) {
                    return if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                        true
                    } else capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                } else {
                    try {
                        val activeNetworkInfo = it.activeNetworkInfo
                        if (activeNetworkInfo != null && activeNetworkInfo.isConnected) {
                            return true
                        }
                    } catch (e: Exception) {
                        Timber.e(e)
                    }
                }
            }
        }
        return false
    }

    var variant: String by Delegates.observable("", { _, _, newValue ->
        Timber.i("Variant: $newValue")

        val props = JSONObject()
        props.put("Variant", newValue)
        mixPanel.registerSuperPropertiesOnce(props)
    })
}