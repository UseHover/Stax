package com.hover.stax.ussd_library

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hover.stax.R
import com.hover.stax.channels.Channel
import com.hover.stax.databinding.LibraryListItemBinding
import com.hover.stax.utils.Utils

class ChannelsAdapter(private val channelList: List<Channel>) : RecyclerView.Adapter<ChannelsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = LibraryListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItems(channelList[holder.adapterPosition])
    }

    override fun getItemCount(): Int = channelList.size

    inner class ViewHolder(val binding: LibraryListItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bindItems(channel: Channel) {
            with(binding) {
                liDescription.text = channel.name
                liButton.apply {
                    text = liButton.context.getString(R.string.dial_btn, channel.rootCode)
                    setOnClickListener { Utils.dial(channel.rootCode, binding.root.context) }
                }
            }
        }
    }
}