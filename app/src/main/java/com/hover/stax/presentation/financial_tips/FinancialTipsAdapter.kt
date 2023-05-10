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
package com.hover.stax.presentation.financial_tips

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hover.stax.databinding.ItemWellnessTipsBinding
import com.hover.stax.domain.model.FinancialTip
import com.hover.stax.core.DateUtils
import java.util.*

class FinancialTipsAdapter(private val tips: List<FinancialTip>, val selectListener: SelectListener) : RecyclerView.Adapter<FinancialTipsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemWellnessTipsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setItems(tips[holder.adapterPosition])
    }

    override fun getItemCount(): Int = tips.size

    inner class ViewHolder(val binding: ItemWellnessTipsBinding) : RecyclerView.ViewHolder(binding.root) {

        fun setItems(tip: FinancialTip) {
            tip.date?.let {
                binding.date.text = com.hover.stax.core.DateUtils.humanFriendlyDate(it)
            }
            binding.title.text = tip.title
            binding.snippet.text = tip.snippet

            binding.root.setOnClickListener { selectListener.onTipSelected(tip) }
        }
    }

    interface SelectListener {
        fun onTipSelected(tip: FinancialTip, isFromDeeplink: Boolean = false)
    }
}