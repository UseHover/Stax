package com.hover.stax.addChannels

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.hover.stax.channels.Channel
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
        val channel = getItem(holder.adapterPosition)
        holder.bind(channel, selectionTracker != null, selectionTracker?.isSelected(channel.id.toLong()))
        holder.itemView.setOnClickListener { selectListener?.clickedChannel(channel) }
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
                return oldItem == newItem
            }

        }
    }
}