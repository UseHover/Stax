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
package com.hover.stax.paybill

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hover.sdk.actions.HoverAction
import com.hover.stax.R
import com.hover.stax.databinding.ItemPaybillActionBinding
import com.hover.stax.utils.UIHelper.loadImage

class PaybillActionsAdapter(
    private val paybillActions: List<HoverAction>,
    private val clickListener: PaybillActionsClickListener
) :
    RecyclerView.Adapter<PaybillActionsAdapter.ActionsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActionsViewHolder {
        val binding = ItemPaybillActionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ActionsViewHolder(binding, clickListener)
    }

    override fun onBindViewHolder(holder: ActionsViewHolder, position: Int) = holder.bindItems(paybillActions[holder.adapterPosition])

    override fun getItemCount(): Int = paybillActions.size

    class ActionsViewHolder(
        val binding: ItemPaybillActionBinding,
        private val clickListener: PaybillActionsClickListener
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bindItems(action: HoverAction) {
            binding.billNumber.text = buildString {
                append(action.to_institution_name)
                append(" (")
                append(action.getVarValue(BUSINESS_NO))
                append(")")
            }

            binding.root.setOnClickListener { clickListener.onSelectPaybill(action) }

            binding.billIcon.loadImage(binding.root.context, binding.billIcon.context.getString(R.string.root_url).plus(action.to_institution_logo))
        }
    }

    interface PaybillActionsClickListener {
        fun onSelectPaybill(action: HoverAction)
    }
}