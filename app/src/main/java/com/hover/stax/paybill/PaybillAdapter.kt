package com.hover.stax.paybill

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hover.stax.R
import com.hover.stax.databinding.ItemPaybillSavedBinding

class PaybillAdapter(private val paybills: List<Paybill>, private val clickListener: ClickListener) : RecyclerView.Adapter<PaybillAdapter.PaybillViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaybillViewHolder {
        val binding = ItemPaybillSavedBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PaybillViewHolder(binding, clickListener)
    }

    override fun onBindViewHolder(holder: PaybillViewHolder, position: Int) {
        holder.bindItems(paybills[holder.adapterPosition])
    }

    override fun getItemCount(): Int = paybills.size

    inner class PaybillViewHolder(val binding: ItemPaybillSavedBinding, private val clickListener: ClickListener) : RecyclerView.ViewHolder(binding.root) {

        fun bindItems(paybill: Paybill) {
            formatLayout(paybill)

            binding.nickname.text = binding.root.context.getString(R.string.paybill_nickname_label, paybill.name, paybill.businessNo)

            if (paybill.isSaved)
                binding.accountNumber.text = binding.root.context.getString(R.string.account_no_label, paybill.name)

            binding.root.setOnClickListener { clickListener.onSelectPaybill(paybill) }
            binding.removeBill.setOnClickListener { clickListener.onDeletePaybill(paybill) }
        }

        private fun formatLayout(paybill: Paybill) = if (!paybill.isSaved) {
            binding.removeBill.visibility = View.GONE
            binding.accountNumber.visibility = View.GONE
        } else {
            binding.removeBill.visibility = View.VISIBLE
            binding.accountNumber.visibility = View.VISIBLE
        }

    }

    interface ClickListener {
        fun onDeletePaybill(paybill: Paybill)

        fun onSelectPaybill(paybill: Paybill)
    }

}