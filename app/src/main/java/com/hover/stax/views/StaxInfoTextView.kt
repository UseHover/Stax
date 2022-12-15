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
package com.hover.stax.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.hover.stax.R
import com.hover.stax.databinding.InfoTextLayoutBinding

class StaxInfoTextView(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {
    private val inflater = LayoutInflater.from(context)
    var binding = InfoTextLayoutBinding.inflate(inflater, this, true)
    private var infoTitle: String = ""
    private var infoContent: String = ""
    private var textValue: String = ""

    init {
        getAttrs(context, attrs)
        fillAttr()
    }

    private fun getAttrs(context: Context, attrs: AttributeSet) {
        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.StaxInfoTextView, 0, 0)
        try {
            infoTitle = a.getString(R.styleable.StaxInfoTextView_infoTitle) ?: ""
            infoContent = a.getString(R.styleable.StaxInfoTextView_infoContent) ?: ""
            textValue = a.getString(R.styleable.StaxInfoTextView_android_text) ?: ""
        } finally {
            a.recycle()
        }
    }

    private fun fillAttr() {
        binding.textId.text = textValue
        binding.root.setOnClickListener {
            StaxDialog(inflater)
                .setDialogIcon(R.drawable.info_offwhite)
                .setDialogTitle(infoTitle)
                .setDialogMessage(infoContent)
                .setPosButton(R.string.btn_ok, null)
                .showIt()
        }
    }
}