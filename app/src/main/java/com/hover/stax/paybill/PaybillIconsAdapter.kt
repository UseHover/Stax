package com.hover.stax.paybill

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.hover.stax.R
import com.hover.stax.databinding.ItemIconsBinding

class PaybillIconsAdapter(private val iconListener: IconSelectListener) : RecyclerView.Adapter<PaybillIconsAdapter.IconsViewHolder>() {

    private val iconList = intArrayOf(R.drawable.ic_garbage, R.drawable.ic_internet, R.drawable.ic_rent, R.drawable.ic_dialpad, R.drawable.ic_tv, R.drawable.ic_water)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IconsViewHolder {
        val binding = ItemIconsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return IconsViewHolder(binding, iconListener)
    }

    override fun onBindViewHolder(holder: IconsViewHolder, position: Int) {
        holder.bindItems(iconList[holder.adapterPosition])
    }

    override fun getItemCount(): Int = iconList.size

    inner class IconsViewHolder(val binding: ItemIconsBinding, private val iconListener: IconSelectListener) : RecyclerView.ViewHolder(binding.root) {

        fun bindItems(iconId: Int) {
            binding.billIcon.setImageDrawable(ContextCompat.getDrawable(binding.billIcon.context, iconId))
            binding.iconLayout.setOnClickListener { iconListener.onSelectIcon(iconId) }
        }
    }

    interface IconSelectListener {
        fun onSelectIcon(id: Int)
    }
}