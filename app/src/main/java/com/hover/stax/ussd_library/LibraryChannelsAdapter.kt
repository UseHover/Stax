package com.hover.stax.ussd_library

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hover.stax.R
import com.hover.stax.channels.Channel
import com.hover.stax.databinding.LibraryListItemBinding
import com.hover.stax.utils.Utils

class LibraryChannelsAdapter : ListAdapter<Channel, LibraryChannelsAdapter.ViewHolder>(diffUtil) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = LibraryListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItems(getItem(holder.adapterPosition))
    }

    inner class ViewHolder(val binding: LibraryListItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bindItems(channel: Channel) {
            with(binding) {
                liTitle.text = channel.name
                liButton.apply {
                    text = liButton.context.getString(R.string.dial_btn, channel.rootCode)
                    setOnClickListener { Utils.dial(channel.rootCode, binding.root.context) }
                }
            }
        }
    }

    companion object {
        private val diffUtil = object: DiffUtil.ItemCallback<Channel>() {
            override fun areItemsTheSame(oldItem: Channel, newItem: Channel): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Channel, newItem: Channel): Boolean {
                return oldItem == newItem
            }
        }
    }
}