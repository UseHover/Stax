package com.hover.stax.home

import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.hover.sdk.actions.HoverAction
import com.hover.sdk.api.HoverParameters
import com.hover.stax.R
import com.hover.stax.accounts.Account
import com.hover.stax.actions.ActionSelectViewModel
import com.hover.stax.balances.BalancesViewModel
import com.hover.stax.channels.Channel
import com.hover.stax.channels.ChannelsViewModel
import com.hover.stax.hover.HoverSession
import com.hover.stax.login.AbstractGoogleAuthActivity
import com.hover.stax.notifications.PushNotificationTopicsInterface
import com.hover.stax.paybill.PaybillViewModel
import com.hover.stax.transactions.StaxTransaction
import com.hover.stax.transactions.TransactionHistoryViewModel
import com.hover.stax.transfers.TransactionType
import com.hover.stax.transfers.TransferViewModel
import com.hover.stax.utils.AnalyticsUtil
import com.hover.stax.utils.Constants
import com.hover.stax.utils.UIHelper
import com.hover.stax.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

abstract class AbstractSDKCaller : AbstractGoogleAuthActivity(), PushNotificationTopicsInterface {

    private val balancesViewModel: BalancesViewModel by viewModel()
    private val historyViewModel: TransactionHistoryViewModel by viewModel()
    private val actionSelectViewModel: ActionSelectViewModel by viewModel()
    private val channelsViewModel: ChannelsViewModel by viewModel()
    private val transferViewModel: TransferViewModel by viewModel()
    private val paybillViewModel: PaybillViewModel by viewModel()

    fun reBuildHoverSession(transaction: StaxTransaction) {
        lifecycleScope.launch(Dispatchers.IO) {
            val actionAndChannelPair = historyViewModel.getActionAndChannel(transaction.action_id, transaction.channel_id)
            val accountNumber = historyViewModel.getAccountNumber(transaction.counterparty_id)

            val hsb = HoverSession.Builder(actionAndChannelPair.first, actionAndChannelPair.second, this@AbstractSDKCaller, Constants.TRANSFERRED_INT)
                    .extra(HoverAction.AMOUNT_KEY, Utils.formatAmount(transaction.amount.toString()))
                    .extra(HoverAction.ACCOUNT_KEY, accountNumber)
                    .extra(HoverAction.PHONE_KEY, accountNumber)

            runAction(hsb)
        }
    }

    fun run(actionPair: Pair<Account?, HoverAction>, index: Int) {
        if (balancesViewModel.getChannel(actionPair.second.channel_id) != null) {
            val hsb = HoverSession.Builder(actionPair.second, balancesViewModel.getChannel(actionPair.second.channel_id)!!, this@AbstractSDKCaller, index)
                    .extra(Constants.ACCOUNT_NAME, actionPair.first?.name)
            actionPair.first?.let { hsb.setAccountId(it.id.toString()) }

            if (index + 1 < balancesViewModel.accounts.value!!.size) hsb.finalScreenTime(0)

            runAction(hsb)
        } else {
//            the only way to get the reference to the observer is to move this out onto it's own block.
            val selectedChannelsObserver = object : Observer<List<Channel>> {
                override fun onChanged(t: List<Channel>?) {
                    if (t != null && balancesViewModel.getChannel(t, actionPair.second.channel_id) != null) {
                        run(actionPair, 0)
                        balancesViewModel.selectedChannels.removeObserver(this)
                    }
                }
            }

            balancesViewModel.selectedChannels.observe(this, selectedChannelsObserver)
        }
    }

    private fun runAction(hsb: HoverSession.Builder) = try {
        hsb.run()
    } catch (e: Exception) {
        runOnUiThread { UIHelper.flashMessage(this, getString(R.string.error_running_action)) }

        val data = JSONObject()
        try {
            data.put("actionId", hsb.action.id)
        } catch (ignored: JSONException) {
        }

        AnalyticsUtil.logAnalyticsEvent("Failed Actions", data, this)
        Timber.e(e)
    }

    fun makeCall(action: HoverAction, channel: Channel) {
        val hsb = HoverSession.Builder(action, channel, this, Constants.REQUEST_REQUEST)
        runAction(hsb)
    }

    private fun makeCall(action: HoverAction, channel: Channel? = null, selectedAccount: Account? = null) {
        val hsb = HoverSession.Builder(action, channel
                ?: channelsViewModel.activeChannel.value!!, this, getRequestCode(action.transaction_type))

        if (action.transaction_type != HoverAction.FETCH_ACCOUNTS) {
            hsb.extra(HoverAction.AMOUNT_KEY, transferViewModel.amount.value)
                    .extra(HoverAction.NOTE_KEY, transferViewModel.note.value)
                    .extra(Constants.ACCOUNT_NAME, selectedAccount?.name)

            actionSelectViewModel.nonStandardVariables.value?.forEach {
                hsb.extra(it.key, it.value)
            }

            selectedAccount?.run { hsb.setAccountId(id.toString()) }
            transferViewModel.contact.value?.let {
                addRecipientInfo(hsb) }
        }

        runAction(hsb)
    }

    private fun addRecipientInfo(hsb: HoverSession.Builder) {
        hsb.extra(HoverAction.ACCOUNT_KEY, transferViewModel.contact.value!!.accountNumber)
            .extra(HoverAction.PHONE_KEY, transferViewModel.contact.value!!.accountNumber)
    }

    fun makeHoverCall(action: HoverAction, account: Account) {
        AnalyticsUtil.logAnalyticsEvent(getString(R.string.finish_transfer, TransactionType.type), this)
        updatePushNotifGroupStatus()

        transferViewModel.checkSchedule()

        makeCall(action, selectedAccount = account)
    }

    private fun getRequestCode(transactionType: String): Int {
        return if (transactionType == HoverAction.FETCH_ACCOUNTS) Constants.FETCH_ACCOUNT_REQUEST
        else Constants.TRANSFER_REQUEST
    }

    fun submitPaymentRequest(action: HoverAction, channel: Channel, account: Account) {
        val hsb = HoverSession.Builder(action, channel, this, Constants.PAYBILL_REQUEST)
                .extra(HoverAction.AMOUNT_KEY, paybillViewModel.amount.value)
                .extra("businessNo", paybillViewModel.businessNumber.value)
                .extra(Constants.ACCOUNT_NAME, account.name)
                .extra(HoverAction.ACCOUNT_KEY, paybillViewModel.accountNumber.value)
        hsb.setAccountId(account.id.toString())

        runAction(hsb)

        val data = JSONObject()
        try {
            data.put("businessNo", paybillViewModel.businessNumber.value)
        } catch (e: Exception) {
            Timber.e(e)
        }

        AnalyticsUtil.logAnalyticsEvent(getString(R.string.finish_transfer, TransactionType.type), data, this)
    }


    fun makeCall(a: HoverAction) {
        AnalyticsUtil.logAnalyticsEvent(getString(R.string.clicked_run_bounty_session), this)
        updatePushNotifGroupStatus(a)
        call(a.public_id)
    }

    fun retryCall(actionId: String) {
        AnalyticsUtil.logAnalyticsEvent(getString(R.string.clicked_retry_bounty_session), this)
        call(actionId)
    }

    private fun call(actionId: String) {
        val i = HoverParameters.Builder(this).request(actionId).setEnvironment(HoverParameters.MANUAL_ENV).buildIntent()
        startActivityForResult(i, BOUNTY_REQUEST)
    }

    private fun updatePushNotifGroupStatus() {
        joinTransactionGroup(this)
        leaveNoUsageGroup(this)
    }

    private fun updatePushNotifGroupStatus(a: HoverAction) {
        joinAllBountiesGroup(this)
        joinBountyCountryGroup(a.country_alpha2.uppercase(), this)
    }

    companion object {
        private const val BOUNTY_REQUEST = 3000
    }
}