package com.hover.stax.addChannels

import androidx.recyclerview.selection.ItemKeyProvider
import com.hover.stax.channels.ChannelsAdapter

class ChannelKeyProvider(private val adapter: ChannelsAdapter) : ItemKeyProvider<Long>(SCOPE_CACHED) {

    override fun getKey(position: Int): Long = adapter.channels[position].id.toLong()

    override fun getPosition(key: Long): Int = adapter.channels.indexOfFirst { it.id.toLong() == key }
}