package com.hover.stax.bounties

import android.content.Context
import com.hover.sdk.actions.HoverAction
import com.hover.sdk.transactions.Transaction
import com.hover.stax.R
import com.hover.stax.channels.Channel
import com.hover.stax.transactions.StaxTransaction
import com.yariksoffice.lingver.Lingver
import java.util.*


class Bounty(val action: HoverAction, val transactions: List<StaxTransaction>) {

    val transactionCount get(): Int = transactions.size

    fun lastTransactionIndex(): Int = if (transactionCount == 0) 0 else transactionCount - 1
    fun hasASuccessfulTransaction(): Boolean = transactions.any { it.status == Transaction.SUCCEEDED }
    fun isLastTransactionFailed(): Boolean = if (transactionCount == 0) false else transactions.last().status == Transaction.FAILED

    fun generateDescription(c: Context): String = when (action.transaction_type) {
        HoverAction.AIRTIME -> c.getString(R.string.descrip_bounty_airtime,
                if (action.isOnNetwork) c.getString(R.string.onnet_choice) else c.getString(R.string.descrip_bounty_offnet, action.to_institution_name))
        HoverAction.P2P -> c.getString(R.string.descrip_bounty_p2p, getP2pRecipientString(c))
        HoverAction.ME2ME -> c.getString(R.string.descrip_bounty_me2me, action.to_institution_name)
        HoverAction.BILL -> c.getString(R.string.descrip_bounty_bill, if (action.isOnNetwork) "" else c.getString(R.string.descrip_bounty_c2b_b, action.to_institution_name))
        HoverAction.MERCHANT -> c.getString(R.string.descrip_bounty_merchant, c.getString(R.string.bounty_merchant_any))
        else -> c.getString(R.string.check_balance)
    }

    fun getInstructions(c: Context): String = when (action.transaction_type) {
        HoverAction.AIRTIME -> c.getString(R.string.bounty_airtime_explain,
                if (action.isOnNetwork) c.getString(R.string.onnet_choice) else c.getString(R.string.descrip_bounty_offnet, action.to_institution_name))
        HoverAction.P2P -> c.getString(R.string.bounty_p2p_explain, getP2pRecipientString(c))
        HoverAction.ME2ME -> c.getString(R.string.bounty_me2me_explain, action.to_institution_name)
        HoverAction.BILL -> c.getString(R.string.bounty_bill_explain, if (action.isOnNetwork) c.getString(R.string.bounty_bill_any) else action.to_institution_name)
        HoverAction.MERCHANT -> c.getString(R.string.bounty_merchant_explain, c.getString(R.string.bounty_merchant_any))
        else -> c.getString(R.string.bounty_balance_explain)
    }

    private fun getP2pRecipientString(c: Context): String {
        return when {
            action.isCrossBorder -> c.getString(R.string.descrip_bounty_cross_country, getCountryName(action.to_country_alpha2), action.to_institution_name)
            action.isOnNetwork -> c.getString(R.string.descrip_bounty_offnet, action.from_institution_name)
            else -> c.getString(R.string.descrip_bounty_offnet, action.to_institution_name)
        }
    }

    private fun getCountryName(country_alpha2: String): String {
        return Locale(Lingver.getInstance().getLanguage(), country_alpha2).displayCountry
    }
}

data class ChannelBounties (val channel: Channel, val bounties: List<Bounty>)