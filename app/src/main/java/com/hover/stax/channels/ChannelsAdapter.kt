package com.hover.stax.channels

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.RecyclerView
import com.hover.stax.databinding.StaxSpinnerItemWithLogoBinding

class ChannelsAdapter(var channels: List<Channel>, var selectListener: SelectListener?) : RecyclerView.Adapter<ChannelViewHolder>() {

    private var selectionTracker: SelectionTracker<Long>? = null

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChannelViewHolder {
        val binding = StaxSpinnerItemWithLogoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChannelViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChannelViewHolder, position: Int) {
        val channel = channels[holder.adapterPosition]
        holder.bind(channel, selectionTracker != null, selectionTracker?.isSelected(channel.id.toLong()))
        holder.itemView.setOnClickListener { selectListener?.clickedChannel(channel) }
    }

    override fun getItemCount(): Int = channels.size

    override fun getItemId(position: Int): Long {
        return channels[position].id.toLong()
    }

    fun updateList(list: List<Channel>) {
        channels = list
        notifyDataSetChanged()
    }

    fun setTracker(tracker: SelectionTracker<Long>) {
        selectionTracker = tracker
    }

    interface SelectListener {
        fun clickedChannel(channel: Channel)
    }
}