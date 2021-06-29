package com.hover.stax.channels

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.hover.stax.databinding.StaxSpinnerItemWithLogoBinding

class ChannelsMultiSelectAdapter(val channelList: List<Channel>) : ListAdapter<Channel, ChannelsViewHolder>(ChannelDiffCallback()) {

    private var selectionTracker: SelectionTracker<Long>? = null

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChannelsViewHolder {
        val binding = StaxSpinnerItemWithLogoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChannelsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChannelsViewHolder, position: Int) {
        val channel = channelList[holder.adapterPosition]
        selectionTracker?.let {
            holder.bindItems(channel, true, it.isSelected(position.toLong()))
        }
    }

    override fun getItemCount(): Int = channelList.size

    override fun getItemId(position: Int): Long = position.toLong()

    fun setTracker(tracker: SelectionTracker<Long>) {
        selectionTracker = tracker
    }

    class ChannelDiffCallback : DiffUtil.ItemCallback<Channel>() {

        override fun areItemsTheSame(oldItem: Channel, newItem: Channel): Boolean = oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Channel, newItem: Channel): Boolean = oldItem == newItem

    }
}

