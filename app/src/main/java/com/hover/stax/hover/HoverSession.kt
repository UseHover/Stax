package com.hover.stax.hover

import android.app.Activity
import android.content.Context
import androidx.fragment.app.Fragment
import com.hover.sdk.actions.HoverAction
import com.hover.sdk.api.Hover
import com.hover.sdk.api.HoverParameters
import com.hover.stax.R
import com.hover.stax.channels.Channel
import com.hover.stax.contacts.PhoneHelper

import com.hover.stax.utils.AnalyticsUtil
import com.hover.stax.utils.Constants
import com.hover.stax.utils.Utils
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber


class HoverSession private constructor(b: Builder) {

    private val frag: Fragment?
    private val action: HoverAction
    private val requestCode: Int
    private val finalScreenTime: Int
    private val accountId: String?

    private fun getBasicBuilder(b: Builder): HoverParameters.Builder = HoverParameters.Builder(b.activity)
            .apply {
                setEnvironment(if (Utils.getBoolean(Constants.TEST_MODE, b.activity)) HoverParameters.TEST_ENV else HoverParameters.PROD_ENV)
                request(b.action.public_id)
                setHeader(getMessage(b.action, b.activity))
                initialProcessingMessage("")
                showUserStepDescriptions(true)
                finalMsgDisplayTime(finalScreenTime)
                style(R.style.StaxHoverTheme)
                sessionOverlayLayout(R.layout.stax_transacting_in_progress)
                private_extra(Constants.ACCOUNT_ID, accountId)
            }

    private fun addExtras(builder: HoverParameters.Builder, extras: JSONObject) {
        val requiredExtras = action.requiredParams
        val keys: Iterator<*> = extras.keys()
        while (keys.hasNext()) {
            val key = keys.next() as String
            val normalizedVal = parseExtra(key, extras.optString(key), requiredExtras)
            if (normalizedVal != null) builder.extra(key, normalizedVal)
        }
    }

    private fun parseExtra(key: String, value: String?, requiredExtras: List<String>): String? {
        if (value == null || !requiredExtras.contains(key)) {
            return null
        }
        return if (key == HoverAction.PHONE_KEY) {
            PhoneHelper.normalizeNumberByCountry(value, action.to_country_alpha2)
        } else value
    }

    private fun getMessage(a: HoverAction, c: Context): String {
        return when (a.transaction_type) {
            HoverAction.BALANCE -> c.getString(R.string.balance_msg, a.from_institution_name)
            HoverAction.AIRTIME -> c.getString(R.string.airtime_msg)
            HoverAction.FETCH_ACCOUNTS -> c.getString(R.string.fetch_accounts)
            else -> c.getString(R.string.transfer_msg)
        }
    }

    private fun startHover(builder: HoverParameters.Builder, a: Activity) {
        val i = builder.buildIntent()
        AnalyticsUtil.logAnalyticsEvent(a.getString(R.string.start_load_screen), a)
        if (frag != null) frag.startActivityForResult(i, requestCode) else a.startActivityForResult(i, requestCode)
    }

    class Builder(a: HoverAction?, c: Channel, activity: Activity, code: Int) {
        val activity: Activity
        var fragment: Fragment? = null
        val channel: Channel
        val action: HoverAction
        val extras: JSONObject
        var requestCode: Int
        var finalScreenTime = 4000
        var account: String? = null

        constructor(a: HoverAction?, c: Channel, act: Activity, requestCode: Int, frag: Fragment?) : this(a, c, act, requestCode) {
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

        fun finalScreenTime(ms: Int): Builder {
            finalScreenTime = ms
            return this
        }

        fun setAccountId(id: String) {
            account = id
        }

        fun run(): HoverSession {
            return HoverSession(this)
        }

        init {
            requireNotNull(a) { "Action must not be null" }
            this.activity = activity
            channel = c
            action = a
            extras = JSONObject()
            requestCode = code
        }
    }

    init {
        Hover.setPermissionActivity(Constants.PERM_ACTIVITY, b.activity)
        frag = b.fragment
        action = b.action
        requestCode = b.requestCode
        finalScreenTime = b.finalScreenTime
        accountId = b.account
        val builder = getBasicBuilder(b)
        addExtras(builder, b.extras)
        startHover(builder, b.activity)
    }
}