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
package com.hover.stax.core

import android.content.Context
import android.os.Bundle
import com.appsflyer.AppsFlyerLib
import com.uxcam.UXCam
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber

object AnalyticsUtil {

    fun logUXCam(event: String, args: JSONObject?) {
        if (event.lowercase().contains("visited")) {
            UXCam.tagScreenName(event)
        }

        if (args != null) {
            UXCam.logEvent(event, args)
        } else {
            UXCam.logEvent(event)
        }
    }

    fun logAppsFlyer(event: String, args: JSONObject?, context: Context) {
        var map: Map<String, Any?>? = null
        if (args != null) {
            map = convertJSONObjectToHashMap(args)
        }

        AppsFlyerLib.getInstance().logEvent(context, event, map)
    }

    fun strippedForFireAnalytics(firebaseEventLog: String): String {
        val newValue = firebaseEventLog
            .replace(", ", "_")
            .replace(".", "_")
            .replace(" ", "_")

        Timber.v("Logging event: $newValue")
        return newValue.lowercase()
    }

    fun convertJSONObjectToBundle(args: JSONObject): Bundle {
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