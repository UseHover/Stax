package com.hover.stax.transactions

import android.content.Context
import android.text.Html
import androidx.core.text.HtmlCompat
import com.hover.sdk.actions.HoverAction
import com.hover.sdk.transactions.Transaction
import com.hover.stax.R
import com.hover.stax.transactionDetails.UssdCallResponse
import com.hover.stax.utils.Utils
import timber.log.Timber

interface TransactionUiDelegate {
    val transaction: StaxTransaction

    fun getIcon(): Int {
        return when (transaction.status) {
            Transaction.FAILED -> R.drawable.ic_error
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

    fun humanStatus(c: Context): String {
        return when (transaction.status) {
            Transaction.FAILED -> c.getString(R.string.failed_label)
            Transaction.PENDING -> c.getString(if (transaction.isRecorded) R.string.checking_your_flow else R.string.pending_cardHead)
            else -> c.getString(R.string.successful_label)
        }
    }

    fun humanCategory(c: Context) : String {
        return if (transaction.isFailed) transaction.category.replace("-", " ") .replaceFirstChar { it.uppercaseChar() }
        else ""
    }

    fun title(c: Context): String {
        var str = if (transaction.isBalanceType && transaction.isSuccessful && !transaction.isRecorded)
            transaction.displayBalance
        else transaction.humanStatus(c)

        if (transaction.transaction_type == Transaction.FAILED)
            str = "$str: ${transaction.humanCategory(c)}"
        return str
    }

    fun shortStatusExplain(action: HoverAction?, institutionName: String, c: Context): String {
        if (transaction.isRecorded) return getRecordedStatusDetail(c)
        return when {
            transaction.status == Transaction.FAILED -> shortFailureMessage(action, c)
            transaction.status == Transaction.PENDING -> c.getString(R.string.pending_cardhead_with_isntType, institutionName)
            transaction.status == Transaction.SUCCEEDED && transaction.balance.isNullOrEmpty() -> c.getString(R.string.successful_label)
            else -> c.getString(R.string.new_balance, institutionName, transaction.balance)
        }
    }

    fun longStatus(action: HoverAction?, messages: UssdCallResponse?, sms: List<UssdCallResponse>?, isExpectingSMS: Boolean, c: Context): String {
        return if (transaction.isRecorded) getRecordedStatusDetail(c)
        else when (transaction.status) {
            Transaction.FAILED -> longFailureMessage(action, c)
            Transaction.PENDING -> c.getString( if(isExpectingSMS)  R.string.pending_cardbody else R.string.pending_no_sms_expected_cardbody)
            else -> lookupSuccessDescription(messages, sms, c)
        }
    }

    private fun getRecordedStatusDetail(c: Context): String {
        val msg = c.getString(when (transaction.status) {
            Transaction.FAILED -> R.string.bounty_transaction_failed
            Transaction.PENDING -> R.string.bounty_flow_pending_dialog_msg
            else -> R.string.flow_done_desc
        })

        return HtmlCompat.fromHtml(msg, HtmlCompat.FROM_HTML_MODE_LEGACY).toString()
    }

    private fun shortFailureMessage(a: HoverAction?, c: Context): String {
        return when (transaction.category) {
            StaxTransaction.BALANCE_ERROR -> c.getString(R.string.balance_error_short)
            StaxTransaction.PIN_ERROR -> c.getString(R.string.pin_error_short)
            StaxTransaction.INVALID_ENTRY_ERROR -> c.getString(R.string.invalid_entry_short)
            StaxTransaction.MMI_ERROR -> c.getString(R.string.mmi_error_short, getServiceName(a, c))
            StaxTransaction.UNREGISTERED_ERROR -> c.getString(R.string.unregistered_error_short)
            StaxTransaction.INCOMPLETE_ERROR -> c.getString(R.string.incomplete_error_short, getServiceName(a, c))
            StaxTransaction.NO_RESPONSE_ERROR -> c.getString(R.string.no_response_error_short, getServiceName(a, c))
            else -> c.getString(R.string.unspecified_error_short)
        }
    }

    private fun longFailureMessage(a: HoverAction?, c: Context): String {
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

    fun getRecipientLabel(): Int {
        return when (transaction.transaction_type) {
            HoverAction.BILL -> R.string.account_label
            HoverAction.RECEIVE -> R.string.sender_label
            else -> R.string.recipient_label
        }
    }

    private fun getServiceName(a: HoverAction?, c: Context): String {
        return a?.from_institution_name ?: c.getString(R.string.null_service_name_text)
    }

    fun getSignedAmount(a: Double?): String? {
        var str: String? = null
        if (a != null) {
            str = Utils.formatAmount(a.toString())
            if (transaction.transaction_type != HoverAction.RECEIVE) str = "-$str"
        }
        return str
    }

    val displayBalance: String
        get() = Utils.formatAmount(transaction.balance)
}
