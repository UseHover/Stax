package com.hover.stax.utils

import android.content.Context
import android.os.Bundle
import com.amplitude.api.Amplitude
import com.amplitude.api.Identify
import com.appsflyer.AppsFlyerLib
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.hover.sdk.api.Hover
import com.hover.stax.BuildConfig
import com.uxcam.UXCam
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber

object AnalyticsUtil {
	@JvmStatic
	fun logErrorAndReportToFirebase(tag: String, message: String, e: Exception?) {
		Timber.e(e, message)
		if (BuildConfig.BUILD_TYPE == "release") {
			if (e != null) FirebaseCrashlytics.getInstance().recordException(e)
			else FirebaseCrashlytics.getInstance().log("$tag - $message")
		}
	}

	@JvmStatic
	fun logAnalyticsEvent(event: String, context: Context) {
		logAmplitude(event, null, context)
		logFirebase(event, null, context)
		logAppsFlyer(event, null, context)

		val firstWord = event.split(" ").first()
		if(firstWord == "Visited") {
			UXCam.tagScreenName(event)
		}
	}

	@JvmStatic
	fun logAnalyticsEvent(event: String, context: Context, excludeAmplitude: Boolean) {
		logFirebase(event, null, context)
		logAppsFlyer(event, null, context)
	}

	@JvmStatic
	fun logAnalyticsEvent(event: String, args: JSONObject, context: Context) {
		logAmplitude(event, args, context)
		logFirebase(event, args, context)
		logAppsFlyer(event, args, context)
	}

	private fun logAmplitude(event: String, args: JSONObject?, context: Context) {
		val amplitude = Amplitude.getInstance()
		val identify = Identify().set("deviceId", Hover.getDeviceId(context))
		amplitude.apply { identify(identify); logEvent(event, args) }
	}

	private fun logFirebase(event: String, args: JSONObject?, context: Context) {
		val deviceId = Hover.getDeviceId(context)
		var bundle: Bundle? = null
		if (args != null) {
			bundle = convertJSONObjectToBundle(args)
		}
		val firebaseAnalytics = FirebaseAnalytics.getInstance(context)
		firebaseAnalytics.apply {
			setUserId(deviceId); logEvent(strippedForFireAnalytics(event),
			bundle)
		}
	}

	private fun logAppsFlyer(event: String, args: JSONObject?, context: Context) {
		var map: Map<String, Any?>? = null
		if (args != null) {
			map = convertJSONObjectToHashMap(args)
		}

		AppsFlyerLib.getInstance().logEvent(context, event, map)
	}

	private fun strippedForFireAnalytics(firebaseEventLog: String): String {
		val newValue = firebaseEventLog.replace(" ", "_").lowercase()
		Timber.e("Logging event: $newValue")
		return newValue
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
}