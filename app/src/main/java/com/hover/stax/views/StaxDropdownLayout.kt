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
import android.text.InputType
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import android.widget.AutoCompleteTextView
import androidx.annotation.CallSuper
import com.google.android.material.textfield.TextInputLayout
import com.hover.stax.R
import com.hover.stax.databinding.StaxDropdownBinding

open class StaxDropdownLayout(context: Context, attrs: AttributeSet) : AbstractStatefulInput(context, attrs) {

    private val binding: StaxDropdownBinding

    lateinit var autoCompleteTextView: AutoCompleteTextView
    private var hint: String? = null
    private var defaultText: String? = null
    private var inputType:String? = null
    private var imeOptions = 0

    @CallSuper
    open fun getAttrs(context: Context, attrs: AttributeSet) {
        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.StaxDropdownLayout, 0, 0)
        try {
            hint = a.getString(R.styleable.StaxDropdownLayout_android_hint)
            defaultText = a.getString(R.styleable.StaxDropdownLayout_android_text)
            inputType = a.getString(R.styleable.StaxDropdownLayout_android_inputType)
            imeOptions = a.getInt(R.styleable.StaxDropdownLayout_android_imeOptions, 0)
        } finally {
            a.recycle()
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        fillAttrs()
    }

    fun setHint(helperText: CharSequence?) {
        if (helperText != null) binding.inputLayout.hint = helperText.toString()
    }

    final override fun initView() {
        super.initView()
        autoCompleteTextView = binding.autoCompleteView
    }

    private fun fillAttrs() {
        if (hint != null) (findViewById<View>(R.id.inputLayout) as TextInputLayout).hint = hint
        autoCompleteTextView.inputType =
            if (inputType !=null) InputType.TYPE_CLASS_PHONE else InputType.TYPE_NULL
        if (!defaultText.isNullOrEmpty()) autoCompleteTextView.setText(
            defaultText
        )
        if (imeOptions > 0) autoCompleteTextView.imeOptions = imeOptions
    }

    override fun setOnFocusChangeListener(l: OnFocusChangeListener) {
        autoCompleteTextView.onFocusChangeListener = l
    }

    init {
        getAttrs(context, attrs)
        binding = StaxDropdownBinding.inflate(LayoutInflater.from(context), this, true)
        initView()
        fillAttrs()
    }
}