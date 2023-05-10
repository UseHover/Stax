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
package com.hover.stax.transactions

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hover.sdk.actions.HoverAction
import com.hover.sdk.transactions.Transaction
import com.hover.stax.database.models.StaxTransaction
import com.hover.stax.databinding.TransactionListItemBinding
import com.hover.stax.transactions.TransactionHistoryAdapter.HistoryViewHolder
import com.hover.stax.utils.DateUtils.humanFriendlyDate

class TransactionHistoryAdapter(private val selectListener: SelectListener) : ListAdapter<TransactionHistoryItem, HistoryViewHolder>(diffUtil) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding =
            TransactionListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val history = getItem(holder.adapterPosition)
        val t = history.staxTransaction
        holder.binding.liTitle.text = t.toString(holder.itemView.context)
        holder.binding.liAmount.text = t.getSignedAmount(t.amount)
        holder.binding.liHeader.visibility = if (shouldShowDate(t, position)) View.VISIBLE else View.GONE
        holder.binding.liHeader.text = humanFriendlyDate(t.initiated_at)
        holder.itemView.setOnClickListener { selectListener.viewTransactionDetail(t.uuid) }
        setStatus(t, history.action, history.institutionName, holder)
    }

    private fun setStatus(
        t: StaxTransaction,
        a: HoverAction?,
        institutionName: String,
        holder: HistoryViewHolder
    ) {
        holder.binding.liAmount.alpha = (if (t.status == Transaction.FAILED) 0.54 else 1.0).toFloat()
        holder.binding.transactionItemLayout.setBackgroundColor(ContextCompat.getColor(holder.binding.root.context, t.getBackgroundColor()))
        holder.binding.liDetail.text = t.shortStatusExplain(a, institutionName, holder.itemView.context)
        holder.binding.liDetail.setCompoundDrawablesRelativeWithIntrinsicBounds(t.getIcon(), 0, 0, 0)
    }

    private fun shouldShowDate(t: StaxTransaction, position: Int): Boolean {
        if (position == 0) return true

        val history = getItem(position - 1)
        val transaction = history.staxTransaction
        return humanFriendlyDate(transaction.initiated_at) != humanFriendlyDate(t.initiated_at)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    interface SelectListener {
        fun viewTransactionDetail(uuid: String?)
    }

    class HistoryViewHolder(var binding: TransactionListItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    companion object {
        private val diffUtil = object : DiffUtil.ItemCallback<TransactionHistoryItem>() {
            override fun areItemsTheSame(
                oldItem: TransactionHistoryItem,
                newItem: TransactionHistoryItem
            ): Boolean {
                return oldItem.staxTransaction.uuid == newItem.staxTransaction.uuid
            }

            override fun areContentsTheSame(
                oldItem: TransactionHistoryItem,
                newItem: TransactionHistoryItem
            ): Boolean {
                return oldItem.staxTransaction.uuid == newItem.staxTransaction.uuid
            }
        }
    }
}