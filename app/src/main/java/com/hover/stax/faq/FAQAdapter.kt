package com.hover.stax.faq

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hover.stax.databinding.FaqItemsBinding

class FAQAdapter(private val faqs: List<FAQ>, private val selectListener: SelectListener) :
        RecyclerView.Adapter<FAQAdapter.FAQViewHolder>() {

    class FAQViewHolder(private val itemBinding: FaqItemsBinding) : RecyclerView.ViewHolder(itemBinding.root) {
        fun setItem(faq: FAQ, selectListener: SelectListener) {
            itemBinding.faqTopicItem.text = faq.topic
            itemBinding.root.setOnClickListener { selectListener.onTopicClicked(faq) }
        }
    }

    interface SelectListener {
        fun onTopicClicked(faq: FAQ)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FAQViewHolder {
        val binding: FaqItemsBinding = FaqItemsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FAQViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FAQViewHolder, position: Int) {
        val faq = faqs[position]
        holder.setItem(faq, selectListener)
    }

    override fun getItemCount(): Int {
        return faqs.size
    }
}