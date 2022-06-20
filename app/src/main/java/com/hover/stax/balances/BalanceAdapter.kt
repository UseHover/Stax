package com.hover.stax.balances

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.RecyclerView
import com.hover.stax.R
import com.hover.stax.databinding.BalanceItemBinding
import com.hover.stax.domain.model.Account
import com.hover.stax.domain.model.DUMMY
import com.hover.stax.utils.DateUtils
import com.hover.stax.utils.UIHelper
import com.hover.stax.utils.Utils


class BalanceAdapter(val accounts: List<Account>, val balanceListener: BalanceListener?) : RecyclerView.Adapter<BalanceAdapter.BalancesViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BalancesViewHolder {
        val binding = BalanceItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BalancesViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BalancesViewHolder, position: Int) {
        val account = accounts[holder.adapterPosition]
        holder.bindItems(account, holder)
    }

    override fun getItemCount(): Int = accounts.size

    private fun setColors(holder: BalancesViewHolder, primary: Int, secondary: Int) {
        holder.binding.root.setCardBackgroundColor(primary)
        holder.binding.balanceSubtitle.setTextColor(secondary)
        holder.binding.balanceAmount.setTextColor(secondary)
        holder.binding.balanceChannelName.setTextColor(secondary)
        holder.binding.balanceRefreshIcon.setColorFilter(secondary)
    }

    private fun setColorForEmptyAmount(holder: BalancesViewHolder, secondary: Int) {
        var drawable = ContextCompat.getDrawable(holder.itemView.context, R.drawable.ic_remove)

        if (drawable != null) {
            drawable = DrawableCompat.wrap(drawable)
            DrawableCompat.setTint(drawable.mutate(), secondary)
            holder.binding.balanceAmount.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null)
        }
    }

    inner class BalancesViewHolder(val binding: BalanceItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bindItems(account: Account, holder: BalancesViewHolder) {
            UIHelper.setTextUnderline(binding.balanceChannelName, account.alias)

            binding.balanceSubtitle.visibility = View.GONE

            when {
                account.latestBalance != null -> {
                    binding.balanceSubtitle.visibility = View.VISIBLE
                    binding.balanceSubtitle.text = DateUtils.humanFriendlyDateTime(account.latestBalanceTimestamp)
                    binding.balanceAmount.text = Utils.formatAmount(account.latestBalance!!)
                }
                account.latestBalance == null -> {
                    binding.balanceAmount.text = "-"
                    binding.balanceSubtitle.visibility = View.VISIBLE
                    binding.balanceSubtitle.text = itemView.context.getString(R.string.refresh_balance_desc)
                }
                else -> {
                    binding.balanceAmount.text = ""
                    setColorForEmptyAmount(holder, UIHelper.getColor(account.secondaryColorHex, false, binding.root.context))
                }
            }

            setColors(
                holder, UIHelper.getColor(account.primaryColorHex, true, holder.itemView.context),
                UIHelper.getColor(account.secondaryColorHex, false, holder.itemView.context)
            )

            if (account.id == DUMMY) {
                holder.binding.balanceSubtitle.visibility = View.GONE
                holder.binding.balanceRefreshIcon.setImageResource(R.drawable.ic_add_icon_24)
            }

            binding.root.setOnClickListener {
                balanceListener?.onTapDetail(account.id)
            }

            binding.balanceRefreshIcon.setOnClickListener {
                balanceListener?.onTapRefresh(account)
            }
        }
    }

    interface BalanceListener {
        fun onTapRefresh(account: Account?)

        fun onTapDetail(accountId: Int)
    }
}