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
package com.hover.stax.paybill

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.hover.stax.R
import com.hover.stax.databinding.ItemIconsBinding

class PaybillIconsAdapter(private val iconListener: IconSelectListener) : RecyclerView.Adapter<PaybillIconsAdapter.IconsViewHolder>() {

    private val iconList = intArrayOf(R.drawable.ic_garbage, R.drawable.ic_internet, R.drawable.ic_rent, R.drawable.ic_dialpad, R.drawable.ic_tv, R.drawable.ic_water)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IconsViewHolder {
        val binding = ItemIconsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return IconsViewHolder(binding, iconListener)
    }

    override fun onBindViewHolder(holder: IconsViewHolder, position: Int) {
        holder.bindItems(iconList[holder.adapterPosition])
    }

    override fun getItemCount(): Int = iconList.size

    inner class IconsViewHolder(
        val binding: ItemIconsBinding,
        private val iconListener: IconSelectListener
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bindItems(iconId: Int) = with(binding.billIcon) {
            setImageDrawable(ContextCompat.getDrawable(context, iconId))
            setOnClickListener { iconListener.onSelectIcon(iconId) }
        }
    }

    interface IconSelectListener {
        fun onSelectIcon(id: Int)
    }
}