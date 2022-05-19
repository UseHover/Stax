package com.hover.stax.transactions

import com.hover.stax.utils.DateUtils.humanFriendlyDate
import com.hover.sdk.actions.HoverAction
import androidx.recyclerview.widget.RecyclerView
import com.hover.stax.transactions.TransactionHistoryAdapter.HistoryViewHolder
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import androidx.core.text.HtmlCompat
import com.hover.sdk.transactions.Transaction
import com.hover.stax.databinding.TransactionListItemBinding
import java.util.Locale

class TransactionHistoryAdapter(private var listOfTransactionActionPair: List<Pair<StaxTransaction, HoverAction?>>,
                                private val selectListener: SelectListener) :
	RecyclerView.Adapter<HistoryViewHolder>() {

	fun updateData(listOfPair: List<Pair<StaxTransaction, HoverAction?>>) {
		this.listOfTransactionActionPair = listOfPair
		notifyDataSetChanged()
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
		val binding =
			TransactionListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
		return HistoryViewHolder(binding)
	}

	override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
		val pair = listOfTransactionActionPair!![position]
		val t = pair.first
		val action = pair.second
		holder.binding.liDescription.text = String.format("%s%s", t.description.substring(0, 1).uppercase(Locale.getDefault()), t.description.substring(1))
		holder.binding.liAmount.text = t.displayAmount
		holder.binding.liHeader.visibility = if (shouldShowDate(t, position)) View.VISIBLE else View.GONE
		holder.binding.liHeader.text = humanFriendlyDate(t.initiated_at)
		holder.itemView.setOnClickListener { selectListener.viewTransactionDetail(t.uuid) }
		setStatus(t,action, holder)
	}

	private fun setStatus(t: StaxTransaction, a: HoverAction?,  holder: HistoryViewHolder) {
		val ts = TransactionStatus(t)
		holder.binding.liAmount.alpha = (if (t.status == Transaction.FAILED) 0.54 else 1.0).toFloat()
		holder.binding.transactionItemLayout.setBackgroundColor(holder.binding.root.context.resources.getColor(ts.getBackgroundColor()))
		holder.binding.liStatus.text = HtmlCompat.fromHtml(ts.getShortStatusDetail(a, holder.binding.root.context), HtmlCompat.FROM_HTML_MODE_LEGACY)
		holder.binding.liStatus.setCompoundDrawablesRelativeWithIntrinsicBounds(ts.getIcon(), 0, 0, 0)
	}

	private fun shouldShowDate(t: StaxTransaction, position: Int): Boolean {
		if(position > 1) {
			val pair = listOfTransactionActionPair!![position -1]
			val transaction = pair.first
			return position == 0 || humanFriendlyDate(transaction.initiated_at) != humanFriendlyDate(t.initiated_at)
		}
		return true
	}

	override fun getItemCount(): Int {
		return listOfTransactionActionPair?.size ?: 0
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