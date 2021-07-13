package com.hover.stax.channels

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hover.stax.databinding.StaxSpinnerItemWithLogoBinding

class ChannelsRecyclerViewAdapter(val channelList: List<Channel>, val selectListener: SelectListener) : RecyclerView.Adapter<ChannelsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChannelsViewHolder {
        val binding = StaxSpinnerItemWithLogoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChannelsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChannelsViewHolder, position: Int) {
        val channel = channelList[holder.adapterPosition]
        holder.bindItems(channel)
        holder.itemView.setOnClickListener { selectListener.clickedChannel(channel) }
    }

    override fun getItemCount(): Int = channelList.size

    interface SelectListener {
        fun clickedChannel(channel: Channel)
    }
}