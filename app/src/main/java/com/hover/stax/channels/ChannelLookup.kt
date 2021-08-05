package com.hover.stax.channels

import android.view.MotionEvent
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.widget.RecyclerView

class ChannelLookup(val recyclerView: RecyclerView) : ItemDetailsLookup<Long>() {
    override fun getItemDetails(e: MotionEvent): ItemDetails<Long>? {
        val view = recyclerView.findChildViewUnder(e.x, e.y)

        return if (view != null)
            (recyclerView.getChildViewHolder(view) as ChannelsViewHolder).getItemDetails()
        else
            null
    }
}