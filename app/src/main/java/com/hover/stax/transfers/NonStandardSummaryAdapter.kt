package com.hover.stax.transfers

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hover.stax.databinding.SummarycardNontemplateItemsBinding

class NonStandardSummaryAdapter : RecyclerView.Adapter<NonStandardSummaryAdapter.ViewHolder>() {
    private var items :  List<NonStandardVariable> = listOf()

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(items :  List<NonStandardVariable>) {
        this.items = items
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
        val item = items[position]
        holder.bindItems(item.key, item.value ?: "")
    }

    override fun getItemCount(): Int {
        return items.size
    }
}