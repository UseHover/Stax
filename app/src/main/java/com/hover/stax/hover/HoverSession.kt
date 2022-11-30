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
package com.hover.stax.hover

import android.app.Activity
import android.content.Context
import androidx.fragment.app.Fragment
import com.hover.sdk.actions.HoverAction
import com.hover.sdk.api.Hover
import com.hover.sdk.api.HoverParameters
import com.hover.stax.R
import com.hover.stax.contacts.PhoneHelper
import com.hover.stax.domain.model.ACCOUNT_ID
import com.hover.stax.domain.model.ACCOUNT_NAME
import com.hover.stax.domain.model.Account
import com.hover.stax.settings.TEST_MODE
import com.hover.stax.utils.AnalyticsUtil
import com.hover.stax.utils.Utils
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber

const val PERM_ACTIVITY = "com.hover.stax.permissions.PermissionsActivity"
private const val TIMER_LENGTH = 35000

class HoverSession private constructor(b: Builder) {

    private val frag: Fragment?
    private val account: Account
    private val action: HoverAction
    private val requestCode: Int
    private val finalScreenTime: Int

    private fun getBasicBuilder(b: Builder): HoverParameters.Builder = HoverParameters.Builder(b.activity)
        .apply {
            setEnvironment(if (Utils.getBoolean(TEST_MODE, b.activity)) HoverParameters.TEST_ENV else HoverParameters.PROD_ENV)
            extra(ACCOUNT_NAME, account.getAccountNameExtra())
            private_extra(ACCOUNT_ID, account.id.toString())
            request(b.action.public_id)
            setHeader(getMessage(b.action, b.activity))
            initialProcessingMessage("")
            showUserStepDescriptions(true)
            timeout(TIMER_LENGTH)
            finalMsgDisplayTime(finalScreenTime)
            style(R.style.StaxHoverTheme)
            sessionOverlayLayout(R.layout.stax_transacting_in_progress)
        }

    private fun addExtras(builder: HoverParameters.Builder, extras: JSONObject) {
        val keys: Iterator<*> = extras.keys()
        while (keys.hasNext()) {
            val key = keys.next() as String
            val normalizedVal = parseExtra(key, extras.optString(key))
            if (normalizedVal != null) builder.extra(key, normalizedVal)
        }
    }

    private fun parseExtra(key: String, value: String?): String? {
        if (value == null) return null
        return if (key == HoverAction.PHONE_KEY) {
            PhoneHelper.normalizeNumberByCountry(value, action.country_alpha2, action.to_country_alpha2)
        } else value
    }

    private fun getMessage(a: HoverAction, c: Context): String {
        return when (a.transaction_type) {
            HoverAction.BALANCE -> c.getString(R.string.balance_msg, a.from_institution_name)
            HoverAction.AIRTIME -> c.getString(R.string.airtime_msg)
            else -> c.getString(R.string.transfer_msg)
        }
    }

    private fun startHover(builder: HoverParameters.Builder, a: Activity) {
        Timber.v("starting hover")
        val i = builder.buildIntent()
        AnalyticsUtil.logAnalyticsEvent(a.getString(R.string.start_load_screen), a)
        if (frag != null) frag.startActivityForResult(i, requestCode) else a.startActivityForResult(i, requestCode)
    }

    class Builder(a: HoverAction?, c: Account, activity: Activity, code: Int) {
        val activity: Activity
        var fragment: Fragment? = null
        val account: Account
        val action: HoverAction
        val extras: JSONObject
        var requestCode: Int
        var finalScreenTime = 0

        constructor(a: HoverAction?, c: Account, act: Activity, requestCode: Int, frag: Fragment?) : this(a, c, act, requestCode) {
            fragment = frag
        }

        fun extra(key: String, value: String?): Builder {
            try {
                extras.put(key, value)
            } catch (e: JSONException) {
                Timber.e("Failed to add extra")
            }
            return this
        }

        fun extras(es: HashMap<String, String>): Builder {
            try {
                es.forEach { extras.put(it.key, it.value) }
            } catch (e: JSONException) {
                Timber.e("Failed to add extra")
            }
            return this
        }

        fun run(): HoverSession {
            return HoverSession(this)
        }

        init {
            requireNotNull(a) { "Action must not be null" }
            this.activity = activity
            account = c
            action = a
            extras = JSONObject()
            requestCode = code
        }
    }

    init {
        Hover.setPermissionActivity(PERM_ACTIVITY, b.activity)
        frag = b.fragment
        account = b.account
        action = b.action
        requestCode = b.requestCode
        finalScreenTime = b.finalScreenTime
        val builder = getBasicBuilder(b)
        addExtras(builder, b.extras)
        startHover(builder, b.activity)
    }
}