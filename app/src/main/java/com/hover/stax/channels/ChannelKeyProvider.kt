package com.hover.stax.channels

import androidx.recyclerview.selection.ItemKeyProvider

class ChannelKeyProvider(private val adapter: ChannelsAdapter) : ItemKeyProvider<Long>(SCOPE_CACHED) {

    override fun getKey(position: Int): Long = adapter.channelList[position].id.toLong()

    override fun getPosition(key: Long): Int = adapter.channelList.indexOfFirst { it.id.toLong() == key }
}