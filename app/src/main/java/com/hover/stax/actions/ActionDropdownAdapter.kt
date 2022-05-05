package com.hover.stax.actions

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import com.hover.sdk.actions.HoverAction
import com.hover.stax.R
import com.hover.stax.accounts.Account
import com.hover.stax.databinding.StaxSpinnerItemWithLogoBinding
import com.hover.stax.utils.UIHelper

class ActionDropdownAdapter<T>(val actions: List<T>, context: Context) : ArrayAdapter<T>(context, 0, actions) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        val action = actions[position]
        val holder: ActionViewHolder

        if (view == null) {
            val binding = StaxSpinnerItemWithLogoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            view = binding.root
            holder = ActionViewHolder(binding)
            view.tag = holder
        } else
            holder = view.tag as ActionViewHolder

        holder.setAction(action, context.getString(R.string.root_url))

        return view
    }

    override fun getCount(): Int = actions.size

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getItem(position: Int): T? = if (actions.isEmpty()) null else actions[position]

    class ActionViewHolder(val binding: StaxSpinnerItemWithLogoBinding) {

        private var id: TextView? = null
        private var logo: ImageView? = null
        private var channelText: AppCompatTextView? = null

        init {
            id = binding.serviceItemId
            logo = binding.serviceItemImageId
            channelText = binding.serviceItemNameId
        }

        fun setAction(action: Any?, baseUrl: String) {
            var logoUrl: String? = null

            if (action is HoverAction) {
                id?.text = action.id.toString()
                channelText?.text = action.toString()
                logoUrl = baseUrl.plus(action.to_institution_logo)
            } else if (action is Account) {
                id?.text = action.id.toString()
                channelText?.text = action.alias
                logoUrl = baseUrl.plus(action.logoUrl)
            }

            UIHelper.loadImage(binding.root.context, logoUrl!!, logo!!)
        }
    }

}