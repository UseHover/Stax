package com.hover.stax.paybill

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.recyclerview.widget.RecyclerView
import com.hover.sdk.actions.HoverAction
import com.hover.stax.databinding.ItemPaybillSavedBinding
import com.hover.stax.utils.Constants
import com.hover.stax.utils.UIHelper
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import timber.log.Timber

class PaybillActionsAdapter(private val paybillActions: List<HoverAction>, private val clickListener: PaybillActionsClickListener) : RecyclerView.Adapter<PaybillActionsAdapter.ActionsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActionsViewHolder {
        val binding = ItemPaybillSavedBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ActionsViewHolder(binding, clickListener)
    }

    override fun onBindViewHolder(holder: ActionsViewHolder, position: Int) = holder.bindItems(paybillActions[holder.adapterPosition])

    override fun getItemCount(): Int = paybillActions.size

    class ActionsViewHolder(val binding: ItemPaybillSavedBinding, private val clickListener: PaybillActionsClickListener) : RecyclerView.ViewHolder(binding.root), Target {

        val logo = binding.billIcon

        init {
            binding.removeBill.visibility = View.GONE
            binding.accountNumber.visibility = View.GONE
        }

        fun bindItems(action: HoverAction) {
            binding.nickname.text = buildString {
                append(action.to_institution_name)
                append("(")
                append(action.to_institution_id)
                append(")")
            }

            binding.root.setOnClickListener { clickListener.onSelectPaybill(action) }

            UIHelper.loadPicasso(action.to_institution_logo, Constants.size55, this)
        }

        override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
            val d = RoundedBitmapDrawableFactory.create(logo.context.resources, bitmap)
            d.isCircular = true
            logo.setImageDrawable(d)
        }

        override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
            Timber.e(e)
        }

        override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}
    }

    interface PaybillActionsClickListener {
        fun onSelectPaybill(action: HoverAction)
    }
}