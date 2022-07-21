package com.hover.stax.presentation.bounties

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hover.stax.databinding.BountyCardChannelBinding
import com.hover.stax.domain.model.ChannelBounties
import timber.log.Timber

class BountyAdapter(private val selectListener: BountyListItem.SelectListener) : RecyclerView.Adapter<BountyAdapter.BountyViewHolder>() {

    private var channelBounties = mutableListOf<ChannelBounties>()

    fun clear() {
        channelBounties.clear()
    }

    fun addItems(items: List<ChannelBounties>) {
        if(channelBounties.isEmpty()) {
            channelBounties.addAll(items)
            notifyDataSetChanged()
            return
        }

        val lastIndex = channelBounties.lastIndex
        channelBounties.addAll(items)
        notifyItemRangeInserted(lastIndex + 1, items.size)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BountyViewHolder {
        return BountyViewHolder(BountyCardChannelBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: BountyViewHolder, position: Int) {
        holder.bindItems(channelBounties[position], selectListener)
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

    override fun getItemCount(): Int = channelBounties.size

}
