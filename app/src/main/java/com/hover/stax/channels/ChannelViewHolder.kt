/*
 * Copyright 2022 Stax
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hover.stax.channels

import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.hover.stax.database.models.Channel
import com.google.android.material.checkbox.MaterialCheckBox
import com.hover.stax.databinding.StaxSpinnerItemWithLogoBinding
import com.hover.stax.utils.UIHelper.loadImage

class ChannelViewHolder(val binding: StaxSpinnerItemWithLogoBinding) : RecyclerView.ViewHolder(binding.root) {

    var id: TextView = binding.serviceItemId
    private var channelText: AppCompatTextView = binding.serviceItemNameId

    private var logo: ImageView = binding.serviceItemImageId
    private var checkBox: MaterialCheckBox = binding.serviceItemCheckbox

    fun bind(channel: Channel, isMultiselect: Boolean = false, isSelected: Boolean? = false) {
        if (isMultiselect) {
            checkBox.visibility = View.VISIBLE
            checkBox.isChecked = isSelected != null && isSelected
        } else checkBox.visibility = View.GONE

        id.text = channel.id.toString()
        channelText.text = channel.toString()

        logo.loadImage(binding.root.context, channel.logoUrl)
    }

    fun clear() {
        Glide.with(binding.root.context).clear(logo)
    }

    fun getItemDetails(): ItemDetailsLookup.ItemDetails<Long> = object : ItemDetailsLookup.ItemDetails<Long>() {
        override fun getPosition(): Int = adapterPosition

        override fun getSelectionKey(): Long = itemId

        override fun inSelectionHotspot(e: MotionEvent): Boolean = true
    }
}