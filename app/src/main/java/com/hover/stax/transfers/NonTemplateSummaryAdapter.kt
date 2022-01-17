package com.hover.stax.transfers

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hover.stax.databinding.SummarycardNontemplateItemsBinding

class NonTemplateSummaryAdapter : RecyclerView.Adapter<NonTemplateSummaryAdapter.ViewHolder>() {
    private var itemMap = mutableMapOf<String, String>()

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(key: String, value: String) {
        itemMap.put(key, value)
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: SummarycardNontemplateItemsBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bindItems(key: String, value: String) {
            binding.itemLabel.text = key
            binding.itemValue.setText(value)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = SummarycardNontemplateItemsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        itemMap.onEachIndexed { index, entry -> if(index == position)   {
            holder.bindItems(entry.key, entry.value)
        }
        }
    }

    override fun getItemCount(): Int {
        return itemMap.size
    }
}