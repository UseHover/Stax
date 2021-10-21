package com.hover.stax.bounties

import android.content.Context
import com.hover.sdk.actions.HoverAction
import com.hover.sdk.transactions.Transaction
import com.hover.stax.R
import com.hover.stax.transactions.StaxTransaction


class Bounty(val action: HoverAction, val transactions: List<StaxTransaction>) {

    val transactionCount get(): Int = transactions.size

    fun lastTransactionIndex(): Int = if (transactionCount == 0) 0 else transactionCount - 1

    fun isLastTransactionFailed(): Boolean = if (transactionCount == 0) false else transactions.last().status == Transaction.FAILED

    fun generateDescription(c: Context): String = when (action.transaction_type) {
        HoverAction.AIRTIME -> c.getString(R.string.descrip_bounty_airtime,
                if (action.isOnNetwork) c.getString(R.string.onnet_choice) else c.getString(R.string.descrip_bounty_offnet, action.to_institution_name))
        HoverAction.P2P -> c.getString(R.string.descrip_bounty_p2p, c.getString(R.string.descrip_bounty_offnet,
                if (action.isOnNetwork) action.from_institution_name else action.to_institution_name))
        HoverAction.ME2ME -> c.getString(R.string.descrip_bounty_me2me, action.to_institution_name)
        HoverAction.C2B -> c.getString(R.string.descrip_bounty_c2b, if (action.isOnNetwork) "" else c.getString(R.string.descrip_bounty_c2b_b, action.to_institution_name))
        else -> c.getString(R.string.check_balance)
    }

    fun getInstructions(c: Context): String = when (action.transaction_type) {
        HoverAction.AIRTIME -> c.getString(R.string.bounty_airtime_explain,
                if (action.isOnNetwork) c.getString(R.string.onnet_choice) else c.getString(R.string.descrip_bounty_offnet, action.to_institution_name))
        HoverAction.P2P -> c.getString(R.string.bounty_p2p_explain, c.getString(R.string.descrip_bounty_offnet,
                if (action.isOnNetwork) action.from_institution_name else action.to_institution_name))
        HoverAction.ME2ME -> c.getString(R.string.bounty_me2me_explain, action.to_institution_name)
        HoverAction.C2B -> c.getString(R.string.bounty_c2b_explain, if (action.isOnNetwork) c.getString(R.string.bounty_c2b_any) else action.to_institution_name)
        else -> c.getString(R.string.bounty_balance_explain)
    }
}