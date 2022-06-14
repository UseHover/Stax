package com.hover.stax.accounts

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hover.stax.databinding.StaxSpinnerItemWithLogoBinding
import com.hover.stax.R
import com.hover.stax.utils.GlideApp

class AccountsAdapter(var accounts: List<Account>) : RecyclerView.Adapter<AccountsAdapter.ViewHolder>() {

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = StaxSpinnerItemWithLogoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val account = accounts[holder.adapterPosition]
        holder.setAccount(account)
    }

    override fun getItemCount(): Int = accounts.size

    inner class ViewHolder(val binding: StaxSpinnerItemWithLogoBinding) : RecyclerView.ViewHolder(binding.root) {

        fun setAccount(account: Account) {
            binding.serviceItemNameId.text = account.alias

            GlideApp.with(binding.root.context)
                .load(account.logoUrl)
                .placeholder(R.color.buttonColor)
                .circleCrop()
                .override(binding.root.context.resources.getDimensionPixelSize(R.dimen.logoDiam))
                .into(binding.serviceItemImageId)
        }
    }
}