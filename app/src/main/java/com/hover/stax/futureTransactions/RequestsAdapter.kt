package com.hover.stax.futureTransactions

import com.hover.stax.utils.Utils.formatAmount
import com.hover.stax.utils.DateUtils.humanFriendlyDate
import androidx.recyclerview.widget.RecyclerView
import com.hover.stax.futureTransactions.RequestsAdapter.RequestsViewHolder
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import com.hover.stax.databinding.TransactionListItemBinding
import com.hover.stax.requests.Request

class RequestsAdapter(private var requestList: List<Request>?,
                      private val selectListener: SelectListener) :
	RecyclerView.Adapter<RequestsViewHolder>() {
	fun updateData(requests: List<Request>?) {
		requestList = requests
		notifyDataSetChanged()
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestsViewHolder {
		val binding =
			TransactionListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
		return RequestsViewHolder(binding)
	}

	override fun onBindViewHolder(holder: RequestsViewHolder, position: Int) {
		val r = requestList!![position]
		holder.binding.liTitle.text = r.description
		holder.binding.liAmount.text = if (r.amount != null) formatAmount(r.amount!!) else "none"
		holder.binding.liHeader.visibility = if (shouldShowDate(r, position)) View.VISIBLE else View.GONE
		holder.binding.liHeader.text = humanFriendlyDate(r.date_sent)
		holder.itemView.setOnClickListener { selectListener.viewRequestDetail(r.id) }
	}

	private fun shouldShowDate(r: Request, position: Int): Boolean {
		return position == 0 || humanFriendlyDate(requestList!![position - 1].date_sent) != humanFriendlyDate(
			r.date_sent)
	}

	override fun getItemCount(): Int {
		return requestList?.size ?: 0
	}

	override fun getItemId(position: Int): Long {
		return position.toLong()
	}

	override fun getItemViewType(position: Int): Int {
		return position
	}

	interface SelectListener {
		fun viewRequestDetail(id: Int)
	}

	class RequestsViewHolder(var binding: TransactionListItemBinding) :
		RecyclerView.ViewHolder(binding.root)
}