package com.hover.stax.wellness

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hover.stax.databinding.ItemWellnessTipsBinding
import com.hover.stax.utils.DateUtils

class WellnessAdapter(private val tips: List<WellnessTip>, val selectListener: SelectListener) : RecyclerView.Adapter<WellnessAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemWellnessTipsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setItems(tips[holder.adapterPosition])
    }

    override fun getItemCount(): Int = tips.size

    inner class ViewHolder(val binding: ItemWellnessTipsBinding) : RecyclerView.ViewHolder(binding.root) {

        fun setItems(tip: WellnessTip) {
            tip.date?.let {
                binding.date.text = DateUtils.timeAgo(binding.root.context, it.time)
            }
            binding.title.text = tip.title
            binding.contentText.text = tip.content

            binding.root.setOnClickListener { selectListener.onTipSelected(tip) }
        }
    }

    interface SelectListener {
        fun onTipSelected(tip: WellnessTip)
    }
}