package com.hover.stax.home

import com.hover.sdk.actions.HoverAction
import com.hover.sdk.api.HoverParameters
import com.hover.stax.R
import com.hover.stax.accounts.Account
import com.hover.stax.hover.HoverSession
import com.hover.stax.hover.HoverViewModel
import com.hover.stax.login.AbstractGoogleAuthActivity
import com.hover.stax.notifications.PushNotificationTopicsInterface
import com.hover.stax.utils.AnalyticsUtil
import com.hover.stax.utils.Constants
import com.hover.stax.utils.UIHelper
import org.json.JSONException
import org.json.JSONObject
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

abstract class AbstractHoverCallerActivity : AbstractGoogleAuthActivity(), PushNotificationTopicsInterface {

    private val viewModel: HoverViewModel by viewModel()

    private fun runAction(hsb: HoverSession.Builder) = try {
        hsb.run()
    } catch (e: Exception) {
        runOnUiThread { UIHelper.flashMessage(this, getString(R.string.error_running_action)) }
        createLog(hsb, "Failed Actions")
    }

    fun run(account: Account, type: String) {
        run(account, viewModel.getAction(account.channelId, type), null, account.id) // Constants.REQUEST_REQUEST
    }

    fun run(account: Account, action: HoverAction, extras: HashMap<String, String>?, index: Int) {
        val hsb = HoverSession.Builder(action, account, this@AbstractHoverCallerActivity,index)
        if (!extras.isNullOrEmpty()) hsb.extras(extras)
        runAction(hsb)
        createLog(hsb, getString(R.string.finish_transfer, action.transaction_type))
    }

    private fun createLog(hsb: HoverSession.Builder, event: String) {
        val data = JSONObject()
        try {
            data.put("actionId", hsb.action.id)
        } catch (ignored: JSONException) {
        }
        AnalyticsUtil.logAnalyticsEvent(event, data, this)
        Timber.e(event)
    }

    private fun getRequestCode(transactionType: String): Int {
        return if (transactionType == HoverAction.FETCH_ACCOUNTS) Constants.FETCH_ACCOUNT_REQUEST
        else Constants.TRANSFER_REQUEST
    }

    fun makeRegularCall(a: HoverAction, analytics: Int) {
        AnalyticsUtil.logAnalyticsEvent(getString(analytics), this)
        updatePushNotifGroupStatus(a)
        call(a.public_id)
    }

    private fun call(actionId: String) {
        val i = HoverParameters.Builder(this).request(actionId).setEnvironment(HoverParameters.MANUAL_ENV).buildIntent()
        startActivityForResult(i, Constants.BOUNTY_REQUEST)
    }

    private fun updatePushNotifGroupStatus() {
        joinTransactionGroup(this)
        leaveNoUsageGroup(this)
    }

    private fun updatePushNotifGroupStatus(a: HoverAction) {
        joinAllBountiesGroup(this)
        joinBountyCountryGroup(a.country_alpha2.uppercase(), this)
    }
}