package com.hover.stax.bounties

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hover.stax.bounties.BountyChannelsAdapter.CardedBountyListViewHolder
import com.hover.stax.channels.Channel
import com.hover.stax.databinding.BountyCardChannelBinding

internal class BountyChannelsAdapter(private var channelList: List<Channel>?, private val allBountiesList: List<Bounty>, private val selectListener: BountyListItem.SelectListener) : RecyclerView.Adapter<CardedBountyListViewHolder>() {

    init {
        channelList = channelList?.asSequence()?.filter { filterBounties(it.id).any { b -> b.action.bounty_is_open || b.transactionCount != 0 } }?.toList()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardedBountyListViewHolder {
        val binding = BountyCardChannelBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CardedBountyListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CardedBountyListViewHolder, position: Int) {
        val c = channelList!![position]
        holder.binding.bountyChannelCard.setTitle(c.ussdName)
        val channelBounties = filterBounties(c.id)
        for (b in channelBounties) {
            if (b.action.bounty_is_open || b.transactionCount != 0) {
                val bountyLi = BountyListItem(holder.binding.bountyChannelCard.context, null)
                bountyLi.setBounty(b, selectListener)
                holder.binding.bountyList.addView(bountyLi)
            }
        }
    }

    private fun filterBounties(channelId: Int): List<Bounty> {
        val matches: MutableList<Bounty> = ArrayList()
        for (b in allBountiesList) {
            if (b.action.channel_id == channelId) {
                matches.add(b)
            }
        }
        return matches
    }

    override fun getItemCount(): Int {
        return channelList?.size ?: 0
    }

    internal class CardedBountyListViewHolder(var binding: BountyCardChannelBinding) : RecyclerView.ViewHolder(binding.root)

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }
}