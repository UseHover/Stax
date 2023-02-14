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
package com.hover.stax.addChannels

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.hover.stax.storage.channel.entity.Channel
import com.hover.stax.channels.ChannelViewHolder
import com.hover.stax.databinding.StaxSpinnerItemWithLogoBinding

class ChannelsAdapter(var selectListener: SelectListener?) : ListAdapter<Channel, ChannelViewHolder>(diffUtil) {

    private var selectionTracker: SelectionTracker<Long>? = null

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChannelViewHolder {
        val binding = StaxSpinnerItemWithLogoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChannelViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChannelViewHolder, position: Int) {
        getItem(holder.adapterPosition)?.let { channel ->
            holder.bind(channel, selectionTracker != null, selectionTracker?.isSelected(channel.id.toLong()))
            holder.itemView.setOnClickListener { selectListener?.clickedChannel(channel) }
        } ?: run { holder.clear() }
    }

    override fun getItemId(position: Int): Long {
        return getItem(position).id.toLong()
    }

    fun setTracker(tracker: SelectionTracker<Long>) {
        selectionTracker = tracker
    }

    interface SelectListener {
        fun clickedChannel(channel: Channel)
    }

    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<Channel>() {
            override fun areItemsTheSame(oldItem: Channel, newItem: Channel): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Channel, newItem: Channel): Boolean {
                return oldItem.name == newItem.name
            }
        }
    }
}