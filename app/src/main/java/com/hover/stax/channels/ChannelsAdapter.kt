package com.hover.stax.channels

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.RecyclerView
import com.hover.stax.databinding.StaxSpinnerItemWithLogoBinding

class ChannelsAdapter(var channelList: List<Channel>, var selectListener: SelectListener?) : RecyclerView.Adapter<ChannelsViewHolder>() {

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
        holder.bind(channel, selectionTracker != null, selectionTracker?.isSelected(channel.id.toLong()))
        holder.itemView.setOnClickListener { selectListener?.clickedChannel(channel) }
    }

    override fun getItemCount(): Int = channelList.size

    override fun getItemId(position: Int): Long {
        return channelList[position].id.toLong()
    }

    fun updateList(list: List<Channel>) {
        channelList = list
        notifyDataSetChanged()
    }

    fun setTracker(tracker: SelectionTracker<Long>) {
        selectionTracker = tracker
    }

    interface SelectListener {
        fun clickedChannel(channel: Channel)
    }
}