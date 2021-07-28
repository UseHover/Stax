package com.hover.stax.balances

import android.content.Context
import android.view.ViewGroup
import com.hover.stax.channels.Channel
import com.hover.stax.databinding.StackBalanceCardBinding
import com.hover.stax.utils.UIHelper
import com.hover.stax.views.staxcardstack.StaxCardStackAdapter
import com.hover.stax.views.staxcardstack.StaxCardStackView

class BalanceCardStackAdapter(private val ctx: Context): StaxCardStackAdapter<Channel>(ctx) {

    override fun onCreateView(parent: ViewGroup?, viewType: Int): StaxCardStackView.ViewHolder {
        return MyViewHolder(StackBalanceCardBinding.inflate(layoutInflater, parent, false))
    }

    override fun bindView(channel: Channel, position: Int, holder: StaxCardStackView.ViewHolder?) {
        if(holder is MyViewHolder) holder.bind(channel.primaryColorHex)
    }

    inner class MyViewHolder(val binding: StackBalanceCardBinding): StaxCardStackView.ViewHolder(binding.root) {

        fun bind(hex: String) {
            binding.root.setCardBackgroundColor(UIHelper.getColor(hex, false, ctx))
        }
    }
}