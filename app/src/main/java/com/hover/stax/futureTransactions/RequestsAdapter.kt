/*
 * Copyright 2022 Stax
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hover.stax.futureTransactions

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hover.stax.databinding.TransactionListItemBinding
import com.hover.stax.futureTransactions.RequestsAdapter.RequestsViewHolder
import com.hover.stax.database.models.Request
import com.hover.stax.utils.DateUtils.humanFriendlyDate
import com.hover.stax.utils.Utils.formatAmount

class RequestsAdapter(
    private var requestList: List<Request>?,
    private val selectListener: SelectListener
) :
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
            r.date_sent
        )
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