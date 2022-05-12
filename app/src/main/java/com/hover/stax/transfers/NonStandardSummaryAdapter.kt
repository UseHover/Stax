package com.hover.stax.transfers

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hover.stax.databinding.SummarycardNontemplateItemsBinding
import com.hover.stax.utils.splitCamelCase

class NonStandardSummaryAdapter(private var items: LinkedHashMap<String, String>) : RecyclerView.Adapter<NonStandardSummaryAdapter.ViewHolder>() {

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(key: String, value: String) {
        items[key] = value
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: SummarycardNontemplateItemsBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bindItems(key: String, value: String) {
            binding.itemLabel.text = key.splitCamelCase()
            binding.itemValue.text = value
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = SummarycardNontemplateItemsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        items.onEachIndexed { index, entry ->
            if (index == position) holder.bindItems(entry.key, entry.value)
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }
}