package com.hover.stax.languages

import android.content.Context
import com.hover.stax.R

import com.hover.stax.utils.AnalyticsUtil
import com.yariksoffice.lingver.Lingver
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber
import java.util.*

class Lang(val code: String) {

    var name: String? = null

    init {
        val locale = Locale(code)
        name = locale.getDisplayLanguage(locale).apply{
            substring(0, 1).uppercase().plus(substring(1))
        }
    }

    fun isSelected(): Boolean = code == Lingver.getInstance().getLanguage()

    override fun toString(): String = name!!

    override fun equals(other: Any?): Boolean {
        if(other !is Lang) return false

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

            AnalyticsUtil.logAnalyticsEvent(c.getString(R.string.selected_language), data, c)
        }
    }
}