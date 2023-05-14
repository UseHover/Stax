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

import android.content.Context
import android.os.Bundle
import com.amplitude.api.Amplitude
import com.amplitude.api.Identify
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.hover.sdk.api.Hover
import com.hover.stax.core.AnalyticsUtil
import com.hover.stax.core.BuildConfig
import com.hover.stax.core.R
import com.uxcam.UXCam
import org.json.JSONObject
import timber.log.Timber

object AnalyticsUtil {

    @JvmStatic
    fun logErrorAndReportToFirebase(tag: String, message: String?, e: Exception?) {
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
        AnalyticsUtil.logAppsFlyer(event, null, context)
        AnalyticsUtil.logUXCam(event, null)
    }

    @JvmStatic
    fun logAnalyticsEvent(event: String, context: Context, excludeAmplitude: Boolean) {
        logFirebase(event, null, context)
        AnalyticsUtil.logAppsFlyer(event, null, context)
        AnalyticsUtil.logUXCam(event, null)
    }

    @JvmStatic
    fun logAnalyticsEvent(event: String, args: JSONObject, context: Context) {
        logAmplitude(event, args, context)
        logFirebase(event, args, context)
        AnalyticsUtil.logAppsFlyer(event, args, context)
        AnalyticsUtil.logUXCam(event, args)
    }

    private fun logAmplitude(event: String, args: JSONObject?, context: Context) {
        val amplitude = Amplitude.getInstance()
        val identify = Identify().set("deviceId", Hover.getDeviceId(context))
        amplitude.apply { identify(identify); logEvent(event, args) }
    }

    private fun logFirebase(event: String, args: JSONObject?, context: Context) {
        val deviceId = Hover.getDeviceId(context)
        val bundle: Bundle = if (args != null) {
            AnalyticsUtil.convertJSONObjectToBundle(args)
        } else {
            Bundle()
        }

        bundle.putString(
            context.getString(R.string.uxcam_session_url),
            UXCam.urlForCurrentSession()
        )

        val firebaseAnalytics = FirebaseAnalytics.getInstance(context)
        firebaseAnalytics.apply {
            setUserId(deviceId); logEvent(
                AnalyticsUtil.strippedForFireAnalytics(event),
                bundle
            )
        }
    }
}