package com.hover.stax.transactions

import com.hover.stax.utils.DateUtils.humanFriendlyDate
import com.hover.stax.transactions.StaxTransaction
import com.hover.sdk.actions.HoverAction
import androidx.recyclerview.widget.RecyclerView
import com.hover.stax.transactions.TransactionHistoryAdapter.HistoryViewHolder
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import com.hover.sdk.transactions.Transaction
import com.hover.stax.databinding.TransactionListItemBinding

class TransactionHistoryAdapter(
	private var transactions: List<StaxTransaction>?, private var actions: List<HoverAction>?, private val selectListener: SelectListener
		) : RecyclerView.Adapter<HistoryViewHolder>() {

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
		holder.binding.liTitle.text = String.format(
			"%s%s",
			t.description.substring(0, 1).uppercase(),
			t.description.substring(1)
		)
		holder.binding.liAmount.text = t.getSignedAmount(t.amount)
		holder.binding.liHeader.visibility =
			if (shouldShowDate(t, position)) View.VISIBLE else View.GONE
		holder.binding.liHeader.text = humanFriendlyDate(t.initiated_at)
		holder.itemView.setOnClickListener { selectListener.viewTransactionDetail(t.uuid) }
		setStatus(t, holder)
	}

	private fun setStatus(t: StaxTransaction, holder: HistoryViewHolder) {
		val a = findAction(t.action_id)
		holder.binding.liAmount.alpha =
			(if (t.status == Transaction.FAILED) 0.54 else 1.0).toFloat()
		holder.binding.transactionItemLayout.setBackgroundColor(ContextCompat.getColor(holder.binding.root.context, t.getBackgroundColor()))
		holder.binding.liDetail.text = t.shortDescription(a, holder.itemView.context)
		holder.binding.liDetail.setCompoundDrawablesRelativeWithIntrinsicBounds(
			t.getIcon(),
			0,
			0,
			0
		)
	}

	private fun findAction(public_id: String): HoverAction? {
		if (actions != null) {
			for (a in actions!!) {
				if (a.public_id == public_id) return a
			}
		}
		return null
	}

	private fun shouldShowDate(t: StaxTransaction, position: Int): Boolean {
		return position == 0 ||
				humanFriendlyDate(transactions!![position - 1].initiated_at) != humanFriendlyDate(
			t.initiated_at
		)
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

	class HistoryViewHolder(var binding: TransactionListItemBinding) : RecyclerView.ViewHolder(
		binding.root
	)
}