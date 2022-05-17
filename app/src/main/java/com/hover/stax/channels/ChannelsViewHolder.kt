package com.hover.stax.channels

import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.checkbox.MaterialCheckBox
import com.hover.stax.databinding.StaxSpinnerItemWithLogoBinding
import com.hover.stax.utils.UIHelper

class ChannelsViewHolder(val binding: StaxSpinnerItemWithLogoBinding) : RecyclerView.ViewHolder(binding.root) {

    val id: TextView = binding.serviceItemId
    private val channelText: AppCompatTextView = binding.serviceItemNameId
    private val logo: ImageView = binding.serviceItemImageId
    private val checkBox: MaterialCheckBox = binding.serviceItemCheckbox

    fun bind(channel: Channel, isMultiselect: Boolean = false, isSelected: Boolean? = false) {
        if (isMultiselect) {
            checkBox.visibility = View.VISIBLE
            checkBox.isChecked = isSelected != null && isSelected
        } else checkBox.visibility = View.GONE

        id.text = channel.id.toString()
        channelText.text = channel.toString()

        UIHelper.loadImage(binding.root.context, channel.logoUrl, logo)
    }

    fun getItemDetails(): ItemDetailsLookup.ItemDetails<Long> = object : ItemDetailsLookup.ItemDetails<Long>() {
        override fun getPosition(): Int = adapterPosition

        override fun getSelectionKey(): Long = itemId

        override fun inSelectionHotspot(e: MotionEvent): Boolean = true
    }

}