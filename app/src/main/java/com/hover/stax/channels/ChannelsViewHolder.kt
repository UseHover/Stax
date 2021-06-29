package com.hover.stax.channels

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.checkbox.MaterialCheckBox
import com.hover.stax.databinding.StaxSpinnerItemWithLogoBinding
import com.hover.stax.utils.Constants
import com.hover.stax.utils.UIHelper
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import timber.log.Timber

class ChannelsViewHolder(val binding: StaxSpinnerItemWithLogoBinding) : RecyclerView.ViewHolder(binding.root), Target {

    var id: TextView? = null
    private var logo: ImageView? = null
    var channelText: AppCompatTextView? = null
    var checkBox: MaterialCheckBox? = null

    fun bindItems(channel: Channel, isMultiselect: Boolean = false, isSelected: Boolean = false) {
        logo = binding.serviceItemImageId
        channelText = binding.serviceItemNameId
        id = binding.serviceItemId
        checkBox = binding.serviceItemCheckbox

        if (isMultiselect) checkBox!!.visibility = View.VISIBLE else checkBox!!.visibility = View.GONE
        checkBox!!.isChecked = isSelected

        id!!.text = channel.id.toString()
        channelText!!.text = channel.toString()

        UIHelper.loadPicasso(channel.logoUrl, Constants.size55, this)
    }

    override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
        val d = RoundedBitmapDrawableFactory.create(id!!.context.resources, bitmap)
        d.isCircular = true
        logo!!.setImageDrawable(d)
    }

    override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
        Timber.e(e)
    }

    override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}

    fun getItemDetails(): ItemDetailsLookup.ItemDetails<Long> = object : ItemDetailsLookup.ItemDetails<Long>() {
        override fun getPosition(): Int = adapterPosition

        override fun getSelectionKey(): Long = itemId

        override fun inSelectionHotspot(e: MotionEvent): Boolean = true
    }

}