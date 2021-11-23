package com.hover.stax.wellness

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hover.stax.databinding.FaqItemsBinding

class WellnessAdapter(private val tips: List<WellnessTip>, val selectListener: SelectListener) : RecyclerView.Adapter<WellnessAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = FaqItemsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setItems(tips[holder.adapterPosition])
    }

    override fun getItemCount(): Int = tips.size

    inner class ViewHolder(val binding: FaqItemsBinding) : RecyclerView.ViewHolder(binding.root) {

        fun setItems(tip: WellnessTip) {
            binding.faqTopicItem.apply {
                text = tip.title
                setOnClickListener { selectListener.onTipSelected(tip) }
            }

        }
    }

    interface SelectListener {
        fun onTipSelected(tip: WellnessTip)
    }
}