package com.hover.stax.account

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.recyclerview.widget.RecyclerView
import com.hover.stax.databinding.StaxSpinnerItemWithLogoBinding
import com.hover.stax.utils.Constants
import com.hover.stax.utils.UIHelper
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import timber.log.Timber

class AccountsAdapter(val accounts: List<Account>, val selectListener: SelectListener) : RecyclerView.Adapter<AccountsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = StaxSpinnerItemWithLogoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(accounts[holder.adapterPosition])
    }

    override fun getItemCount(): Int = accounts.size

    inner class ViewHolder(val binding: StaxSpinnerItemWithLogoBinding) : RecyclerView.ViewHolder(binding.root), Target {

        fun bind(account: Account) {
            binding.serviceItemNameId.text = account.name.plus(" - ").plus(account.accountNo)
            UIHelper.loadPicasso(account.logoUrl, Constants.size55, this)

            binding.root.setOnClickListener { selectListener.accountSelected(account.id) }
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

    interface SelectListener {
        fun accountSelected(id: Int)
    }
}