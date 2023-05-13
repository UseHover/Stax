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
import android.content.SharedPreferences
import java.text.DecimalFormat

object Utils {

    private const val SHARED_PREFS = "staxprefs"
    private const val SDK_PREFS = "_hoversdk"

    private fun getSharedPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(
            getPackage(context) + SHARED_PREFS,
            Context.MODE_PRIVATE
        )
    }

    fun getSdkPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(getPackage(context) + SDK_PREFS, Context.MODE_PRIVATE)
    }

    fun saveString(key: String?, value: String?, c: Context) {
        val editor = getSharedPrefs(c).edit()
        editor.putString(key, value)
        editor.apply()
    }

    fun getString(key: String?, c: Context): String? {
        return getSharedPrefs(c).getString(key, "")
    }

    fun removeString(key: String, context: Context) {
        getSharedPrefs(context).edit().apply {
            remove(key)
            apply()
        }
    }

    fun getBoolean(key: String?, c: Context, returnTrueDefault: Boolean = false): Boolean {
        return getSharedPrefs(c).getBoolean(key, returnTrueDefault)
    }

    fun saveInt(key: String?, value: Int, c: Context) {
        val editor = getSharedPrefs(c).edit()
        editor.putInt(key, value)
        editor.apply()
    }

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

    fun isFirebaseTopicInDefaultState(topic: String?, c: Context): Boolean {
        return getSharedPrefs(c).getBoolean(topic, true)
    }

    fun alterFirebaseTopicState(topic: String?, c: Context) {
        val editor = getSharedPrefs(c).edit()
        editor.putBoolean(topic, false)
        editor.apply()
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
    fun formatAmount(number: String?): String {
        return when (number) {
            "0" -> "00"
            null -> "--"
            else -> try {
                formatAmount(amountToDouble(number))
            } catch (e: Exception) {
                number
            }
        }
    }

    @JvmStatic
    fun formatAmount(number: Double?): String {
        return try {
            val formatter = DecimalFormat("#,##0.00")
            formatter.maximumFractionDigits = 2
            formatter.format(number)
        } catch (e: Exception) {
            number.toString()
        }
    }

    @JvmStatic
    fun formatAmountForUSSD(number: Double?): String {
        return try {
            val formatter = DecimalFormat("###0.##")
            formatter.maximumFractionDigits = 2
            formatter.minimumFractionDigits = 0
            formatter.format(number)
        } catch (e: Exception) {
            number.toString()
        }
    }

    @JvmStatic
    fun amountToDouble(amount: String?): Double? {
        try {
            return amount?.replace(",".toRegex(), "")?.toDouble()
        } catch (e: NumberFormatException) {
            return null
        }
    }

    @JvmStatic
    fun formatPercent(number: Int): String {
        return try {
            val formatter = DecimalFormat("##0")
            formatter.maximumFractionDigits = 0
            formatter.format(number)
        } catch (e: Exception) {
            number.toString()
        }
    }
}