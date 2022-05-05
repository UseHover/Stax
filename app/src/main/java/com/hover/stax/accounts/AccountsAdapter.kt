package com.hover.stax.accounts

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hover.stax.R
import com.hover.stax.databinding.StaxSpinnerItemWithLogoBinding
import com.hover.stax.utils.Constants
import com.hover.stax.utils.GlideApp

class AccountsAdapter(val accounts: List<Account>, val selectListener: SelectListener) : RecyclerView.Adapter<AccountsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = StaxSpinnerItemWithLogoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(accounts[holder.adapterPosition])
    }

    override fun getItemCount(): Int = accounts.size

    inner class ViewHolder(val binding: StaxSpinnerItemWithLogoBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(account: Account) {
            binding.serviceItemNameId.text = account.alias

            GlideApp.with(binding.root.context)
                .load(account.logoUrl)
                .placeholder(R.color.buttonColor)
                .circleCrop()
                .override(Constants.size55)
                .into(binding.serviceItemImageId)

            binding.root.setOnClickListener { selectListener.accountSelected(account) }
        }
    }

    interface SelectListener {
        fun accountSelected(account: Account)
    }
}