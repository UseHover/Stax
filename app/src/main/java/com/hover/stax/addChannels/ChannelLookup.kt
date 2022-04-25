package com.hover.stax.addChannels

import android.view.MotionEvent
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.widget.RecyclerView
import com.hover.stax.channels.ChannelsViewHolder

class ChannelLookup(val recyclerView: RecyclerView) : ItemDetailsLookup<Long>() {
    override fun getItemDetails(e: MotionEvent): ItemDetails<Long>? {
        val view = recyclerView.findChildViewUnder(e.x, e.y)

        return if (view != null)
            (recyclerView.getChildViewHolder(view) as ChannelsViewHolder).getItemDetails()
        else
            null
    }
}