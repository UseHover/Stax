package com.hover.stax.accounts

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import com.hover.stax.databinding.StaxSpinnerItemWithLogoBinding
import com.hover.stax.utils.Constants.size55
import com.hover.stax.utils.UIHelper
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import timber.log.Timber

class AccountDropdownAdapter(val accounts: List<Account>, context: Context) : ArrayAdapter<Account>(context, 0, accounts) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        val account = accounts[position]
        val holder: ViewHolder

        if(view == null){
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

    inner class ViewHolder(val binding: StaxSpinnerItemWithLogoBinding) : Target {

        fun setAccount(account: Account) {
            binding.serviceItemNameId.text = account.alias
            UIHelper.loadPicasso(account.logoUrl, size55, this)
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