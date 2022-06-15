package com.hover.stax.bounties

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hover.stax.databinding.BountyCardChannelBinding

class BountyAdapter(private val selectListener: BountyListItem.SelectListener) : ListAdapter<ChannelBounties, BountyAdapter.BountyViewHolder>(diffUtil) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BountyViewHolder {
        return BountyViewHolder(BountyCardChannelBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: BountyViewHolder, position: Int) {
        getItem(holder.adapterPosition)?.let {
            holder.bindItems(it, selectListener)
        }
    }

    class BountyViewHolder(var binding: BountyCardChannelBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bindItems(channelBounties: ChannelBounties, listener: BountyListItem.SelectListener) {
            binding.bountyChannelCard.setTitle(channelBounties.channel.ussdName)
            binding.bountyList.removeAllViews()

            for (b in channelBounties.bounties) {
                val bountyLi = BountyListItem(binding.bountyChannelCard.context, null)
                bountyLi.setBounty(b, listener)
                binding.bountyList.addView(bountyLi)
            }
        }

    }

    companion object {
        private val diffUtil = object : DiffUtil.ItemCallback<ChannelBounties>() {
            override fun areItemsTheSame(oldItem: ChannelBounties, newItem: ChannelBounties): Boolean {
                return oldItem.channel.id == newItem.channel.id
            }

            override fun areContentsTheSame(oldItem: ChannelBounties, newItem: ChannelBounties): Boolean {
                return oldItem.channel == newItem.channel
            }
        }
    }

}
