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

    @SuppressLint("ResourceAsColor")
    fun setStateInfo(transaction: StaxTransaction) {
        updateState(getIcon(transaction), getBackgrounColor(transaction), getTitle(transaction), getDetail(transaction))
    }

    private fun updateState(icon: Int, backgroundColor: Int, title: Int, detail: Int) {
        with(binding.notificationCard) {
            setBackButtonVisibility(VISIBLE);
            setIcon(icon);
            setBackgroundColor(backgroundColor);
            setTitle(title);
            if (isFlatView) makeFlatView()
            binding.notificationDetail.text = Html.fromHtml(resources.getString(detail));
        }
    }

    private fun getIcon(transaction: StaxTransaction): Int {
        return when (transaction.status) {
            Transaction.FAILED -> R.drawable.ic_info_red
            Transaction.PENDING -> if (transaction.isRecorded) R.drawable.ic_warning else R.drawable.ic_info
            else -> R.drawable.ic_success
        }
    }

    private fun getBackgrounColor(transaction: StaxTransaction): Int {
        return when (transaction.status) {
            Transaction.FAILED -> R.color.cardDarkRed
            Transaction.PENDING -> if (transaction.isRecorded) R.color.pending_brown else R.color.cardDarkBlue
            else -> R.color.muted_green
        }
    }

    private fun getTitle(transaction: StaxTransaction): Int {
        return when (transaction.status) {
            Transaction.FAILED -> R.string.unsuccessful_cardHead
            Transaction.PENDING ->  if (transaction.isRecorded) R.string.checking_your_flow else R.string.pending_cardHead
            else -> R.string.confirmed_cardHead
        }
    }

    private fun getDetail(transaction: StaxTransaction): Int {
        return when (transaction.status) {
            Transaction.FAILED -> if (transaction.isRecorded) R.string.bounty_transaction_failed else R.string.unsuccessful_desc
            Transaction.PENDING ->  if (transaction.isRecorded) R.string.bounty_flow_pending_dialog_msg else R.string.pending_cardbody
            else -> if (transaction.isRecorded) R.string.flow_done_desc else R.string.confirmed_desc
        }
    }

}