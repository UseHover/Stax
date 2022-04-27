package com.hover.stax.views

import android.content.Context
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import com.google.android.material.textfield.TextInputEditText
import com.hover.stax.R
import com.hover.stax.databinding.StaxInputBinding


class StaxTextInputLayout(context: Context, attrs: AttributeSet) : AbstractStatefulInput(context, attrs) {

    private var hint: String? = null
    private var inputType: Int = -1
    lateinit var editText: TextInputEditText

    val binding: StaxInputBinding

    init {
        getAttrs(context, attrs)
        binding = StaxInputBinding.inflate(LayoutInflater.from(context), this, true)

        initView()
        fillAttrs()
    }

    override fun initView() {
        super.initView()
        editText = binding.inputEditText
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

    private fun fillAttrs() {
        hint?.let { binding.inputLayout.hint = it }
        if (inputType > 0) binding.inputEditText.inputType = inputType
    }

    fun setMutlipartText(text: String?, subtext: String?) {
        if (text.isNullOrEmpty())
            setText(subtext)
        else if (subtext.isNullOrEmpty())
            setText(text)
        else
            setText("$text ($subtext)")
    }

    fun setText(text: String?) {
        text?.let {
            editText.setText(it)

            if (it.isNotEmpty())
                setState(null, SUCCESS)
        }
    }

    fun setText(text: String?, updateState: Boolean) {
        text?.let {
            editText.setText(it)

            if (updateState && it.isNotEmpty())
                setState(null, SUCCESS)
        }
    }

    val text get() = editText.text.toString()

    override fun setOnFocusChangeListener(l: OnFocusChangeListener?) {
        editText.onFocusChangeListener = l
    }

    fun addTextChangedListener(listener: TextWatcher) = editText.addTextChangedListener(listener)
}