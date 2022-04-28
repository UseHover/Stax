package com.hover.stax.home

import android.content.Context
import android.content.Intent
import com.hover.sdk.actions.HoverAction
import com.hover.sdk.api.HoverParameters
import com.hover.stax.accounts.Account
import com.hover.stax.channels.Channel
import com.hover.stax.contacts.StaxContact
import com.hover.stax.hover.HoverSession
import com.hover.stax.login.AbstractGoogleAuthActivity
import com.hover.stax.notifications.PushNotificationTopicsInterface
import com.hover.stax.paybill.Paybill
import com.hover.stax.transactions.StaxTransaction
import com.hover.stax.transfers.TransferViewModel
import com.hover.stax.utils.*
import java.util.LinkedHashMap

object SDKBuilder: PushNotificationTopicsInterface {

    fun createIntent(transaction: StaxTransaction, actionAndChannelPair: Pair<HoverAction, Channel>, c: Context) : Intent {
        val hsb = HoverSession.Builder(actionAndChannelPair.first, actionAndChannelPair.second, c)
            .extra(HoverAction.AMOUNT_KEY, Utils.formatAmount(transaction.amount.toString()))
        return hsb.getIntent()
    }

    fun createIntent(action: HoverAction, channel: Channel, c: Context) : Intent {
        val hsb = HoverSession.Builder(action, channel, c)
        return hsb.getIntent()
    }

    fun createIntent(action: HoverAction, channel: Channel, account: Account, paybill: Paybill, c: Context) : Intent {
        val hsb = HoverSession.Builder(action, channel, c)
            .extra(HoverAction.AMOUNT_KEY, paybill.recurringAmount.toString())
            .extra("businessNo", paybill.businessNo)
            .extra(Constants.ACCOUNT_NAME, account.name)
            .extra(HoverAction.ACCOUNT_KEY, paybill.accountNo)
        hsb.setAccountId(account.id.toString())
        return hsb.getIntent()
    }

    fun createIntent(a: HoverAction, c: Context) : Intent{
        updatePushNotifGroupStatus(a, c)
        return createIntent(a.public_id, c)
    }

    fun createIntent(actionId: String, c: Context) : Intent {
        return HoverParameters.Builder(c).request(actionId).setEnvironment(HoverParameters.MANUAL_ENV).buildIntent();
    }

    fun createIntent(action: HoverAction,
                             channel: Channel,
                             selectedAccount: Account? = null,
                             nonStandardVariables: LinkedHashMap<String, String>?,
                             transferViewModel: TransferViewModel, c: Context) : Intent {
        val hsb = HoverSession.Builder(action, channel, c)

        if (action.transaction_type != HoverAction.FETCH_ACCOUNTS) {
            hsb.extra(HoverAction.AMOUNT_KEY, transferViewModel.amount.value)
                .extra(HoverAction.NOTE_KEY, transferViewModel.note.value)
                .extra(Constants.ACCOUNT_NAME, selectedAccount?.name)

            nonStandardVariables?.forEach { hsb.extra(it.key, it.value) }

            selectedAccount?.run { hsb.setAccountId(id.toString()) }
            transferViewModel.contact.value?.let { addRecipientInfo(hsb, it) }
        }

        return hsb.getIntent()
    }

    private fun addRecipientInfo(hsb: HoverSession.Builder, contact: StaxContact) {
        hsb.extra(HoverAction.ACCOUNT_KEY, contact.accountNumber)
            .extra(HoverAction.PHONE_KEY,  contact.accountNumber)
    }

    private fun updatePushNotifGroupStatus(a: HoverAction, c: Context) {
        joinAllBountiesGroup(c)
        joinBountyCountryGroup(a.country_alpha2.uppercase(), c)
    }
}