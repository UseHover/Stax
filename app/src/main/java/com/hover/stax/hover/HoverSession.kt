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
import android.content.Intent
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
    private val finalScreenTime: Int

    private val builder: HoverParameters.Builder

    private fun getBasicBuilder(b: Builder): HoverParameters.Builder = HoverParameters.Builder(b.activity)
        .apply {
            setEnvironment(if (Utils.getBoolean(TEST_MODE, b.activity)) HoverParameters.TEST_ENV else HoverParameters.PROD_ENV)
            extra(ACCOUNT_NAME, account.getAccountNameExtra())
            private_extra(ACCOUNT_ID, account.id.toString())
            request(b.action.public_id)
            setHeader(getMessage(b))
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

    private fun getMessage(b: Builder): String {
        return if (b.message != null) { b.message!! }
            else {
                when (b.action.transaction_type) {
                    HoverAction.BALANCE -> b.activity.getString(R.string.balance_msg, b.action.from_institution_name)
                    HoverAction.AIRTIME -> b.activity.getString(R.string.airtime_msg)
                    else -> b.activity.getString(R.string.transfer_msg)
                }
            }
    }

    private fun stopEarly(builder: HoverParameters.Builder, varName: String?) {
        if (varName != null && action.output_params.has(varName))
            builder.stopAt(action.output_params.getInt(varName))
    }

    private fun getIntent(): Intent {
        return builder.buildIntent()
    }

    class Builder(a: HoverAction?, c: Account, activity: Activity) {
        val activity: Activity
        var fragment: Fragment? = null
        val account: Account
        var message: String? = null
        val action: HoverAction
        val extras: JSONObject
        var finalScreenTime = 0
        var stopVar: String? = null

        constructor(action: HoverAction,
                    c: Account,
                    extras: HashMap<String, String>?,
                    act: Activity) : this(action, c, act) {
            if (!extras.isNullOrEmpty()) { extras(extras) }
        }

        constructor(a: HoverAction?, c: Account, act: Activity, frag: Fragment?) : this(a, c, act) {
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

        fun stopAt(varName: String): Builder {
            stopVar = varName
            return this
        }

        fun finalScreenTime(ms: Int): Builder {
            finalScreenTime = ms
            return this
        }

        fun message(msg: String): Builder {
            message = msg
            return this
        }

        fun build(): Intent {
            return HoverSession(this).getIntent()
        }

        init {
            requireNotNull(a) { "Action must not be null" }
            this.activity = activity
            account = c
            action = a
            extras = JSONObject()
        }
    }

    init {
        Hover.setPermissionActivity(PERM_ACTIVITY, b.activity)
        frag = b.fragment
        account = b.account
        action = b.action
        finalScreenTime = b.finalScreenTime

        builder = getBasicBuilder(b)
        addExtras(builder, b.extras)
        stopEarly(builder, b.stopVar)
    }
}