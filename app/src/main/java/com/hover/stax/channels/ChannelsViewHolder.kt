package com.hover.stax.channels

import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.checkbox.MaterialCheckBox
import com.hover.stax.R
import com.hover.stax.databinding.StaxSpinnerItemWithLogoBinding
import com.hover.stax.utils.GlideApp

class ChannelsViewHolder(val binding: StaxSpinnerItemWithLogoBinding) : RecyclerView.ViewHolder(binding.root) {

    var id: TextView? = null
    private var channelText: AppCompatTextView? = null

    private var logo: ImageView? = null
    private var checkBox: MaterialCheckBox? = null

    fun bind(channel: Channel, isMultiselect: Boolean = false, isSelected: Boolean? = false) {
        logo = binding.serviceItemImageId
        channelText = binding.serviceItemNameId
        id = binding.serviceItemId
        checkBox = binding.serviceItemCheckbox

        if (isMultiselect) {
            checkBox!!.visibility = View.VISIBLE
            checkBox!!.isChecked = isSelected != null && isSelected
        } else checkBox!!.visibility = View.GONE

        id!!.text = channel.id.toString()
        channelText!!.text = channel.toString()

        GlideApp.with(binding.root.context)
            .load(channel.logoUrl)
            .placeholder(R.color.buttonColor)
            .circleCrop()
            .into(logo!!)
    }

    fun getItemDetails(): ItemDetailsLookup.ItemDetails<Long> = object : ItemDetailsLookup.ItemDetails<Long>() {
        override fun getPosition(): Int = adapterPosition

        override fun getSelectionKey(): Long = itemId

        override fun inSelectionHotspot(e: MotionEvent): Boolean = true
    }

}