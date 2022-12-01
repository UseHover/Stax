/*
 * Copyright 2022 Stax
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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