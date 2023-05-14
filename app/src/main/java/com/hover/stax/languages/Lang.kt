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
package com.hover.stax.languages

import android.content.Context
import com.hover.stax.R
import com.hover.stax.core.AnalyticsUtil
import com.yariksoffice.lingver.Lingver
import java.util.*
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber

class Lang(val code: String) {

    var name: String? = null

    init {
        val locale = Locale(code)
        name = locale.getDisplayLanguage(locale).apply {
            substring(0, 1).uppercase().plus(substring(1))
        }
    }

    fun isSelected(): Boolean = code == Lingver.getInstance().getLanguage()

    override fun toString(): String = name!!

    override fun equals(other: Any?): Boolean {
        if (other !is Lang) return false

        return code == other.code
    }

    companion object {
        fun logChange(code: String, c: Context) {
            val data = JSONObject()
            try {
                data.put("language", code)
            } catch (e: JSONException) {
                Timber.e(e.localizedMessage)
            }

            com.hover.stax.utils.AnalyticsUtil.logAnalyticsEvent(c.getString(R.string.selected_language), data, c)
        }
    }
}