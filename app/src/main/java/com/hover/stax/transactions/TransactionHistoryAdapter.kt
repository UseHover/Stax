package com.hover.stax.transactions

import com.hover.stax.utils.DateUtils.humanFriendlyDate
import com.hover.stax.transactions.TransactionStatus.getBackgroundColor
import com.hover.stax.transactions.TransactionStatus.getShortStatusDetail
import com.hover.stax.transactions.TransactionStatus.getIcon
import com.hover.stax.transactions.StaxTransaction
import com.hover.sdk.actions.HoverAction
import androidx.recyclerview.widget.RecyclerView
import com.hover.stax.transactions.TransactionHistoryAdapter.HistoryViewHolder
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import com.hover.stax.transactions.TransactionStatus
import androidx.core.text.HtmlCompat
import com.hover.sdk.transactions.Transaction
import com.hover.stax.databinding.TransactionListItemBinding
import java.util.*

class TransactionHistoryAdapter(private var transactions: List<StaxTransaction>?,
                                private var actions: List<HoverAction>,
                                private val selectListener: SelectListener) :
	RecyclerView.Adapter<HistoryViewHolder>() {
	fun updateData(ts: List<StaxTransaction>?, `as`: List<HoverAction>?) {
		if (ts == null || `as` == null) return
		transactions = ts
		actions = `as`
		notifyDataSetChanged()
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
		val binding =
			TransactionListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
		return HistoryViewHolder(binding)
	}

	override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
		val t = transactions!![position]
		holder.binding.liDescription.text = String.format("%s%s",
			t.description.substring(0, 1).uppercase(Locale.getDefault()),
			t.description.substring(1))
		holder.binding.liAmount.text = t.displayAmount
		holder.binding.liHeader.visibility =
			if (shouldShowDate(t, position)) View.VISIBLE else View.GONE
		holder.binding.liHeader.text = humanFriendlyDate(t.initiated_at)
		holder.itemView.setOnClickListener { view: View? -> selectListener.viewTransactionDetail(t.uuid) }
		setStatus(t, holder)
	}

	private fun setStatus(t: StaxTransaction, holder: HistoryViewHolder) {
		val ts = TransactionStatus(t)
		val a = findAction(t.action_id)
		holder.binding.liAmount.alpha =
			(if (t.status == Transaction.FAILED) 0.54 else 1.0).toFloat()
		holder.binding.transactionItemLayout.setBackgroundColor(holder.binding.root.context.resources.getColor(
			ts.getBackgroundColor()))
		holder.binding.liStatus.text =
			HtmlCompat.fromHtml(ts.getShortStatusDetail(a, holder.binding.root.context),
				HtmlCompat.FROM_HTML_MODE_LEGACY)
		holder.binding.liStatus.setCompoundDrawablesRelativeWithIntrinsicBounds(ts.getIcon(),
			0,
			0,
			0)
	}

	private fun findAction(public_id: String): HoverAction? {
		for (a in actions) {
			if (a.public_id == public_id) return a
		}
		return null
	}

	private fun shouldShowDate(t: StaxTransaction, position: Int): Boolean {
		return position == 0 || humanFriendlyDate(transactions!![position - 1].initiated_at) != humanFriendlyDate(
			t.initiated_at)
	}

	override fun getItemCount(): Int {
		return if (transactions != null) transactions!!.size else 0
	}

	override fun getItemId(position: Int): Long {
		return position.toLong()
	}

	interface SelectListener {
		fun viewTransactionDetail(uuid: String?)
	}

	class HistoryViewHolder(var binding: TransactionListItemBinding) :
		RecyclerView.ViewHolder(binding.root)
}