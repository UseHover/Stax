package com.hover.stax.paybill

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hover.sdk.actions.HoverAction
import com.hover.stax.R
import com.hover.stax.databinding.ItemPaybillActionBinding
import com.hover.stax.utils.GlideApp
import com.hover.stax.utils.UIHelper

class PaybillActionsAdapter(private val paybillActions: List<HoverAction>, private val clickListener: PaybillActionsClickListener)
    : RecyclerView.Adapter<PaybillActionsAdapter.ActionsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActionsViewHolder {
        val binding = ItemPaybillActionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ActionsViewHolder(binding, clickListener)
    }

    override fun onBindViewHolder(holder: ActionsViewHolder, position: Int) = holder.bindItems(paybillActions[holder.adapterPosition])

    override fun getItemCount(): Int = paybillActions.size

    class ActionsViewHolder(val binding: ItemPaybillActionBinding, private val clickListener: PaybillActionsClickListener) : RecyclerView.ViewHolder(binding.root) {

        fun bindItems(action: HoverAction) {
            binding.billNumber.text = buildString {
                append(action.to_institution_name)
                append(" (")
                append(action.to_institution_id)
                append(")")
            }

            binding.root.setOnClickListener { clickListener.onSelectPaybill(action) }

            GlideApp.with(binding.root.context)
                .load(binding.billIcon.context.getString(R.string.root_url).plus(action.to_institution_logo))
                .placeholder(R.color.buttonColor)
                .circleCrop()
                .into(binding.billIcon)
        }
    }

    interface PaybillActionsClickListener {
        fun onSelectPaybill(action: HoverAction)
    }
}