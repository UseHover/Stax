package com.hover.stax.views

import android.annotation.SuppressLint
import android.content.Context
import android.text.Html
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.hover.sdk.transactions.Transaction
import com.hover.stax.R
import com.hover.stax.databinding.TransactionStatusLayoutBinding
import com.hover.stax.transactions.StaxTransaction

open class StaxTransactionStatusView(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {
    private val binding: TransactionStatusLayoutBinding
    private var isFlatView: Boolean = false

    init {
        getAttrs(context, attrs)
        binding = TransactionStatusLayoutBinding.inflate(LayoutInflater.from(context), this, true)
    }

    private fun getAttrs(context: Context, attrs: AttributeSet) {
        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.StaxTransactionStatusView, 0, 0)
        try {
            isFlatView = a.getBoolean(R.styleable.StaxTransactionStatusView_isFlatView, false)
        } finally {
            a.recycle()
        }
    }

    private fun fillFromAttrs() {
        if (isFlatView) binding.notificationCard.makeFlatView()
    }

    fun updateInfo(transaction: StaxTransaction) {
        binding.notificationCard.setBackButtonVisibility(VISIBLE)
        makeUpdate(transaction.status, transaction.isRecorded)
        fillFromAttrs()
    }

    private fun makeUpdate(status : String, isBounty: Boolean) {
        updateIcon(status, isBounty)
        updateBackgroundColor(status, isBounty)
        updateTitle(status, isBounty)
        updateNotificationDetail(status, isBounty)
    }

    private fun updateIcon(status : String, isBounty: Boolean) {
        with(binding.notificationCard) {
            when (status) {
                Transaction.SUCCEEDED -> setIcon(R.drawable.ic_success)
                Transaction.PENDING -> setIcon(if (isBounty) R.drawable.ic_warning else R.drawable.ic_info)
                Transaction.FAILED -> setIcon(R.drawable.ic_info_red)
            }
        }
    }

    @SuppressLint("ResourceAsColor")
    private fun updateBackgroundColor(status : String, isBounty: Boolean) {
        with(binding.notificationCard) {
            when (status) {
                Transaction.SUCCEEDED -> setBackgroundColor(R.color.muted_green)
                Transaction.PENDING -> setBackgroundColor(if (isBounty) R.color.pending_brown else R.color.cardDarkBlue)
                Transaction.FAILED -> setBackgroundColor(R.color.cardDarkRed)
            }
        }
    }

    private fun updateTitle(status : String, isBounty: Boolean) {
        with(binding.notificationCard) {
            when (status) {
                Transaction.SUCCEEDED -> setTitle(R.string.confirmed_cardHead)
                Transaction.PENDING ->  setTitle(if (isBounty) R.string.checking_your_flow else R.string.pending_cardHead)
                Transaction.FAILED -> setTitle(R.string.unsuccessful_cardHead)
            }
        }
    }

    private fun updateNotificationDetail(status : String, isBounty: Boolean) {
        with(binding.notificationCard) {
            when (status) {
                Transaction.SUCCEEDED ->  binding.notificationDetail.text = Html.fromHtml(resources.getString(if (isBounty) R.string.flow_done_desc else R.string.confirmed_desc))
                Transaction.PENDING ->  binding.notificationDetail.text = Html.fromHtml(resources.getString(if (isBounty) R.string.bounty_flow_pending_dialog_msg else R.string.pending_cardbody))
                Transaction.FAILED -> binding.notificationDetail.text = Html.fromHtml(resources.getString(if (isBounty) R.string.bounty_transaction_failed else R.string.unsuccessful_desc))
            }
        }


    }

}