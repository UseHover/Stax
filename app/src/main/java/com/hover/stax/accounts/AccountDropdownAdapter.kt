package com.hover.stax.accounts

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.hover.stax.R
import com.hover.stax.databinding.StaxSpinnerItemWithLogoBinding
import com.hover.stax.domain.model.Account
import com.hover.stax.utils.UIHelper.loadImage

class AccountDropdownAdapter(val accounts: List<Account>, context: Context) : ArrayAdapter<Account>(context, 0, accounts) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        val account = accounts[position]
        val holder: ViewHolder

        if (view == null) {
            val binding = StaxSpinnerItemWithLogoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            view = binding.root
            holder = ViewHolder(binding)
            view.tag = holder
        } else
            holder = view.tag as ViewHolder

        holder.setAccount(account)

        return view
    }

    override fun getCount(): Int = accounts.size

    override fun getItem(position: Int): Account? {
        return if (accounts.isEmpty()) null else accounts[position]
    }

    inner class ViewHolder(val binding: StaxSpinnerItemWithLogoBinding) {

        fun setAccount(account: Account) {
            binding.serviceItemNameId.text = account.userAlias

            if (account.logoUrl.isEmpty())
                binding.serviceItemImageId.loadImage(binding.root.context, R.drawable.ic_add)
            else
                binding.serviceItemImageId.loadImage(binding.root.context, account.logoUrl)

        }

    }
}