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
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.hover.stax.R
import com.hover.stax.database.models.Paybill
import com.hover.stax.databinding.ItemPaybillSavedBinding
import com.hover.stax.utils.UIHelper.loadImage

class PaybillAdapter(
    private val paybills: List<Paybill>,
    private val clickListener: ClickListener
) : RecyclerView.Adapter<PaybillAdapter.PaybillViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaybillViewHolder {
        val binding =
            ItemPaybillSavedBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PaybillViewHolder(binding, clickListener)
    }

    override fun onBindViewHolder(holder: PaybillViewHolder, position: Int) {
        holder.bindItems(paybills[holder.adapterPosition])
    }

    override fun getItemCount(): Int = paybills.size

    inner class PaybillViewHolder(
        val binding: ItemPaybillSavedBinding,
        private val clickListener: ClickListener
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bindItems(paybill: Paybill) {
            binding.nickname.text = paybill.toString()
            binding.accountNumber.text =
                binding.root.context.getString(R.string.account_no_detail, paybill.accountNo)

            if (paybill.logo != 0) {
                binding.billLogo.visibility = View.GONE
                binding.iconLayout.visibility = View.VISIBLE
                Glide.with(binding.root.context).clear(binding.billLogo)
                binding.billIcon.setImageDrawable(
                    ContextCompat.getDrawable(
                        binding.billIcon.context,
                        paybill.logo
                    )
                )
            } else {
                binding.iconLayout.visibility = View.GONE
                binding.billLogo.visibility = View.VISIBLE

                binding.billLogo.loadImage(binding.root.context, paybill.logoUrl)
            }

            binding.root.setOnClickListener { clickListener.onSelectPaybill(paybill) }
            binding.removeBill.setOnClickListener { clickListener.onDeletePaybill(paybill) }
        }
    }

    interface ClickListener {
        fun onDeletePaybill(paybill: Paybill)

        fun onSelectPaybill(paybill: Paybill)
    }
}