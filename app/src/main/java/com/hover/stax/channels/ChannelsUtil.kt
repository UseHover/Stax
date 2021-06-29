package com.hover.stax.channels

import android.view.MotionEvent
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.ItemKeyProvider
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

class ChannelDiffCallback : DiffUtil.ItemCallback<Channel>() {

    override fun areItemsTheSame(oldItem: Channel, newItem: Channel): Boolean = oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: Channel, newItem: Channel): Boolean = oldItem == newItem

}

class ChannelKeyProvider(val adapter: ChannelsMultiSelectAdapter) : ItemKeyProvider<Long>(SCOPE_CACHED) {

    override fun getKey(position: Int): Long = adapter.currentList[position].id.toLong()

    override fun getPosition(key: Long): Int = adapter.currentList.indexOfFirst { it.id.toLong() == key }

}

class ChannelLookup(val recyclerView: RecyclerView) : ItemDetailsLookup<Long>() {

    override fun getItemDetails(e: MotionEvent): ItemDetails<Long>? {
        val view = recyclerView.findChildViewUnder(e.x, e.y)

        return if (view != null)
            (recyclerView.getChildViewHolder(view) as ChannelsViewHolder).getItemDetails()
        else
            null
    }

}