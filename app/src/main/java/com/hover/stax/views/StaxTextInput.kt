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

import android.annotation.SuppressLint
import android.content.Context
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import com.google.android.material.textfield.TextInputEditText
import com.hover.stax.R
import com.hover.stax.databinding.StaxTextInputBinding

class StaxTextInput(context: Context, attrs: AttributeSet) : AbstractStatefulInput(context, attrs) {

    private var hint: String? = null
    private var inputType: Int = -1

    var editText: TextInputEditText? = null
    var binding: StaxTextInputBinding? = null

    init {
        getAttrs(context, attrs)
        initView()
        fillAttrs()
    }

    private fun getAttrs(context: Context, attrs: AttributeSet) {
        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.StaxTextInputLayout, 0, 0)
        try {
            hint = a.getString(R.styleable.StaxTextInputLayout_android_hint)
            inputType = a.getInt(R.styleable.StaxTextInputLayout_android_inputType, 0)
        } finally {
            a.recycle()
        }
    }

    override fun initView() {
        binding = StaxTextInputBinding.inflate(LayoutInflater.from(context), this, true)
        super.initView()
        editText = binding?.inputEditText
    }

    fun setHint(h: CharSequence?) {
        hint = h?.toString()
        hint?.let { binding?.inputLayout?.hint = it }
    }

    private fun fillAttrs() {
        hint?.let { binding?.inputLayout?.hint = it }
        if (inputType > 0) binding?.inputEditText?.inputType = inputType
    }

    fun setMultipartText(text: String?, subtext: String?) {
        if (text.isNullOrEmpty())
            setText(subtext)
        else if (subtext.isNullOrEmpty())
            setText(text)
        else
            setText("$text ($subtext)")
    }

    fun setText(text: String?) {
        text?.let {
            editText?.setText(it)

            if (it.isNotEmpty())
                setState(null, SUCCESS)
        }
    }

    fun setText(text: String?, updateState: Boolean) {
        text?.let {
            editText?.setText(it)

            if (updateState && it.isNotEmpty())
                setState(null, SUCCESS)
        }
    }

    val text get() = editText?.text.toString()

    override fun setOnFocusChangeListener(l: OnFocusChangeListener?) {
        editText?.onFocusChangeListener = l
    }

    fun addTextChangedListener(listener: TextWatcher) {
        editText?.addTextChangedListener(listener)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun setOnClickListener(listener: OnClickListener?) {
        editText?.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN && listener != null) {
                listener.onClick(this)
            }
            false
        }
    }
}