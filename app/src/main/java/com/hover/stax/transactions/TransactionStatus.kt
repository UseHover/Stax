package com.hover.stax.transactions

import android.content.Context
import androidx.compose.ui.text.capitalize
import com.hover.sdk.actions.HoverAction
import com.hover.sdk.transactions.Transaction
import com.hover.stax.R

class TransactionStatus(val transaction: StaxTransaction) {

    fun getIcon(): Int {
        return when (transaction.status) {
            Transaction.FAILED -> R.drawable.ic_info_red
            Transaction.PENDING -> R.drawable.ic_warning
            else -> R.drawable.ic_success
        }
    }

    fun getBackgroundColor(): Int {
        return when (transaction.status) {
            Transaction.FAILED -> R.color.cardDarkRed
            Transaction.PENDING -> if (transaction.isRecorded) R.color.pending_brown else R.color.cardDarkBlue
            else -> R.color.muted_green
        }
    }

    fun getTitle(c: Context): String {
        return when (transaction.status) {
            Transaction.FAILED -> c.getString(R.string.failed_label)
            Transaction.PENDING -> c.getString(if (transaction.isRecorded) R.string.checking_your_flow else R.string.pending_cardHead)
            else -> if(transaction.isBalanceType) transaction.displayBalance else c.getString(R.string.successful_label)
        }
    }

    fun getPlainTitle(c: Context) : String {
        return when (transaction.status) {
            Transaction.FAILED -> c.getString(R.string.failed_label)
            Transaction.PENDING -> c.getString(if (transaction.isRecorded) R.string.checking_your_flow else R.string.pending_cardHead)
            else -> c.getString(R.string.successful_label)
        }
    }

    fun getReason() : String {
        return if(transaction.isFailed) transaction.category.replace("-", "") else ""

    }

    fun getDisplayType(c: Context, a: HoverAction): String {
        return when (transaction.transaction_type) {
            HoverAction.BALANCE -> c.getString(R.string.check_balance)
            HoverAction.AIRTIME -> c.getString(R.string.buy_airtime)
            HoverAction.P2P -> c.getString(R.string.display_transfer_money, a.to_institution_name)
            HoverAction.ME2ME -> c.getString(R.string.display_transfer_money, a.to_institution_name)
            HoverAction.C2B -> c.getString(R.string.display_bill_payment)
            HoverAction.RECEIVE -> c.getString(R.string.display_money_received)
            else -> "Other"
        }.replaceFirstChar { it.uppercase() }
    }


    fun getShortStatusDetail(action: HoverAction?, c: Context): String {
        return if (transaction.isRecorded) getRecordedStatusDetail(c)
        else when (transaction.status) {
            Transaction.FAILED -> lookupFailureMessage(action, c)
            Transaction.PENDING -> c.getString(R.string.pending_cardhead)
            else -> getStringOrEmdash(transaction.balance, R.string.new_balance, c)
        }
    }

    fun getStatusDetail(action: HoverAction?, messages: UssdCallResponse?, sms: List<UssdCallResponse>?, c: Context): String {
        return if (transaction.isRecorded) getRecordedStatusDetail(c)
        else when (transaction.status) {
            Transaction.FAILED -> lookupFailureDescription(action, c)
            Transaction.PENDING -> c.getString(R.string.pending_cardbody)
            else -> lookupSuccessDescription(messages, sms, c)
        }
    }

    private fun getRecordedStatusDetail(c: Context): String {
        return c.getString(when (transaction.status) {
            Transaction.FAILED -> R.string.bounty_transaction_failed
            Transaction.PENDING -> R.string.bounty_flow_pending_dialog_msg
            else -> R.string.flow_done_desc
        })
    }

    private fun lookupFailureMessage(a: HoverAction?, c: Context): String {
        return when (transaction.category) {
            StaxTransaction.BALANCE_ERROR -> c.getString(R.string.balance_error, getServiceName(a, c))
            StaxTransaction.PIN_ERROR -> c.getString(R.string.pin_error, getServiceName(a, c))
            StaxTransaction.INVALID_ENTRY_ERROR -> c.getString(R.string.invalid_entry)
            StaxTransaction.MMI_ERROR -> c.getString(R.string.mmi_error)
            StaxTransaction.UNREGISTERED_ERROR -> c.getString(R.string.unregistered_error, getServiceName(a, c))
            StaxTransaction.INCOMPLETE_ERROR -> c.getString(R.string.incomplete_error, getServiceName(a, c))
            StaxTransaction.NO_RESPONSE_ERROR -> c.getString(R.string.no_response_error, getServiceName(a, c))
            else -> c.getString(R.string.unspecified_error)
        }
    }

    private fun lookupFailureDescription(a: HoverAction?, c: Context): String {
        return when (transaction.category) {
            StaxTransaction.BALANCE_ERROR -> c.getString(R.string.balance_error, getServiceName(a, c))
            StaxTransaction.PIN_ERROR -> c.getString(R.string.pin_error, getServiceName(a, c))
            StaxTransaction.INVALID_ENTRY_ERROR -> c.getString(R.string.invalid_entry_desc)
            StaxTransaction.MMI_ERROR -> c.getString(R.string.mmi_error_desc)
            StaxTransaction.UNREGISTERED_ERROR -> c.getString(R.string.unregistered_error, getServiceName(a, c))
            StaxTransaction.INCOMPLETE_ERROR -> c.getString(R.string.incomplete_desc, getServiceName(a, c))
            StaxTransaction.NO_RESPONSE_ERROR -> c.getString(R.string.no_response_desc, getServiceName(a, c))
            else -> c.getString(R.string.unspecified_error_desc)
        }
    }

    private fun lookupSuccessDescription(last_message: UssdCallResponse?, sms: List<UssdCallResponse>?, c: Context): String {
        return if (!sms.isNullOrEmpty())
            sms.sortedByDescending { it.responseMessage.length }.map { it.responseMessage }.toString()
        else if (!(last_message?.responseMessage.isNullOrEmpty()))
            last_message!!.responseMessage
        else
            c.getString(R.string.loading)
    }

    private fun getStringOrEmdash(value: String?, stringRes: Int, c: Context): String {
        return if (value == null) "\\u2014"
        else c.getString(stringRes, value)
    }

    private fun getServiceName(a: HoverAction?, c: Context): String {
        return a?.from_institution_name ?: c.getString(R.string.null_service_name_text)
    }
}
