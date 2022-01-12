package com.hover.stax.paybill

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.recyclerview.widget.RecyclerView
import com.hover.stax.R
import com.hover.stax.databinding.ItemPaybillSavedBinding
import com.hover.stax.utils.Constants
import com.hover.stax.utils.UIHelper
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import timber.log.Timber

class PaybillAdapter(private val paybills: List<Paybill>, private val clickListener: ClickListener) : RecyclerView.Adapter<PaybillAdapter.PaybillViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaybillViewHolder {
        val binding = ItemPaybillSavedBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PaybillViewHolder(binding, clickListener)
    }

    override fun onBindViewHolder(holder: PaybillViewHolder, position: Int) {
        holder.bindItems(paybills[holder.adapterPosition])
    }

    override fun getItemCount(): Int = paybills.size

    inner class PaybillViewHolder(val binding: ItemPaybillSavedBinding, private val clickListener: ClickListener) : RecyclerView.ViewHolder(binding.root), Target {

        fun bindItems(paybill: Paybill) {
            binding.nickname.text = paybill.toString()
            binding.accountNumber.text = binding.root.context.getString(R.string.account_no_label, paybill.accountNo)

            if (paybill.logo != 0)
                binding.billIcon.setImageDrawable(ContextCompat.getDrawable(binding.billIcon.context, paybill.logo))
            else
                UIHelper.loadPicasso(paybill.logoUrl, Constants.size55, this)

            binding.root.setOnClickListener { clickListener.onSelectPaybill(paybill) }
            binding.removeBill.setOnClickListener { clickListener.onDeletePaybill(paybill) }
        }

        override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
            val d = RoundedBitmapDrawableFactory.create(binding.billIcon.context.resources, bitmap)
            d.isCircular = true
            binding.billIcon.setImageDrawable(d)
        }

        override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
            Timber.e(e)
        }

        override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}

    }

    interface ClickListener {
        fun onDeletePaybill(paybill: Paybill)

        fun onSelectPaybill(paybill: Paybill)
    }

}