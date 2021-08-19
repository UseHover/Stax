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

open class StaxTransactionStatusView(context: Context, attrs: AttributeSet ) : FrameLayout(context, attrs) {
    private val binding: TransactionStatusLayoutBinding
    private var isFlatView : Boolean = false

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
        if(isFlatView) binding.notificationCard.makeFlatView()
    }

    fun updateInfo(transaction: StaxTransaction) {
        when(transaction.status){
            Transaction.SUCCEEDED -> setSuccessView(transaction.isRecorded)
            Transaction.PENDING -> setPendingView(transaction.isRecorded)
            Transaction.FAILED -> setFailedView(transaction.isRecorded)
        }
        fillFromAttrs()
    }

    @SuppressLint("ResourceAsColor")
    private fun setSuccessView(isBounty: Boolean) {
        with(binding.notificationCard) {
            setBackButtonVisibility(VISIBLE)
            setIcon(R.drawable.ic_success)
            setBackgroundColor(R.color.muted_green)
            setTitle(R.string.confirmed_cardHead)
            binding.notificationDetail.text = Html.fromHtml(resources.getString(if(isBounty) R.string.flow_done_desc  else R.string.confirmed_desc))
        }
    }

    @SuppressLint("ResourceAsColor")
    private fun setPendingView(isBounty : Boolean) {
        with(binding.notificationCard) {
            setBackButtonVisibility(VISIBLE)
            setIcon(if(isBounty) R.drawable.ic_warning else R.drawable.ic_info)
            setBackgroundColor(if(isBounty)  R.color.pending_brown  else R.color.cardDarkBlue)
            setTitle(if (isBounty)R.string.checking_your_flow  else R.string.pending_cardHead)
            binding.notificationDetail.text = Html.fromHtml( resources.getString(if(isBounty) R.string.bounty_flow_pending_dialog_msg  else R.string.pending_cardbody))
        }
    }

    @SuppressLint("ResourceAsColor")
    private fun setFailedView(isBounty: Boolean) {
        with(binding.notificationCard) {
            setBackButtonVisibility(VISIBLE)
            setIcon(R.drawable.ic_info_red)
            setBackgroundColor(R.color.cardDarkRed)
            setTitle(R.string.unsuccessful_cardHead)
            binding.notificationDetail.text = Html.fromHtml(resources.getString(if (isBounty) R.string.bounty_transaction_failed  else R.string.unsuccessful_desc))
        }
    }

}