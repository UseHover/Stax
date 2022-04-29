package com.hover.stax.channels

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.RecyclerView
import com.hover.stax.accounts.Account
import com.hover.stax.databinding.StaxSpinnerItemWithLogoBinding
import com.hover.stax.utils.UIHelper
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import timber.log.Timber

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

    override fun getItemId(position: Int): Long {
        return accounts[position].id.toLong()
    }

    fun updateList(list: List<Account>) {
        accounts = list
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: StaxSpinnerItemWithLogoBinding) : RecyclerView.ViewHolder(binding.root), Target {

        fun setAccount(account: Account) {
            binding.serviceItemNameId.text = account.alias
            UIHelper.loadPicasso(account.logoUrl, this)
        }

        override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
            val d = RoundedBitmapDrawableFactory.create(binding.root.context.resources, bitmap)
            d.isCircular = true
            binding.serviceItemImageId.setImageDrawable(d)
        }

        override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
            Timber.e(e)
        }

        override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}

    }
}