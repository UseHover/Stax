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
import com.hover.stax.databinding.StaxSpinnerItemWithLogoBinding
import com.hover.stax.utils.UIHelper.loadImage

class ActionDropdownAdapter(val actions: List<HoverAction>, context: Context) : ArrayAdapter<HoverAction>(context, 0, actions) {

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

    override fun getItem(position: Int): HoverAction? = if (actions.isEmpty()) null else actions[position]

    class ActionViewHolder(val binding: StaxSpinnerItemWithLogoBinding) {

        private var id: TextView = binding.serviceItemId
        private var logo: ImageView = binding.serviceItemImageId
        private var channelText: AppCompatTextView = binding.serviceItemNameId

        fun setAction(action: HoverAction, baseUrl: String) {
            id.text = action.id.toString()
            channelText.text = action.toString()
            val logoUrl = baseUrl.plus(action.to_institution_logo)
            logo.loadImage(binding.root.context, logoUrl)
        }
    }
}