package com.hover.stax.transactions;

import com.hover.sdk.transactions.Transaction
import com.hover.stax.R

class TransactionStatus(val transaction: StaxTransaction) {

    fun getIcon(): Int {
        return when (transaction.status) {
            Transaction.FAILED -> R.drawable.ic_info_red
            Transaction.PENDING -> if (transaction.isRecorded) R.drawable.ic_warning else R.drawable.ic_info
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

    fun getTitle(): Int {
        return when (transaction.status) {
            Transaction.FAILED -> R.string.unsuccessful_cardHead
            Transaction.PENDING -> if (transaction.isRecorded) R.string.checking_your_flow else R.string.pending_cardHead
            else -> R.string.confirmed_cardHead
        }
    }

    fun getDetail(): Int {
        return when (transaction.status) {
            Transaction.FAILED -> if (transaction.isRecorded) R.string.bounty_transaction_failed else {
                if(transaction.category !=null && transaction.category == StaxTransaction.CATEGORY_INCOMPLETE_SESSION)
                    R.string.unsuccessful_incomplete_desc
                else
                    R.string.unsuccessful_desc
            }
            Transaction.PENDING -> if (transaction.isRecorded) R.string.bounty_flow_pending_dialog_msg else R.string.pending_cardbody
            else -> if (transaction.isRecorded) R.string.flow_done_desc else R.string.confirmed_desc
        }
    }
}
