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
import com.hover.stax.transactions.TransactionStatus

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
    fun setStateInfo(status: TransactionStatus?) {
        if (status != null) {
            updateState(status.getIcon(), status.getBackgrounColor(), status.getTitle(), status.getDetail())
        }
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
}