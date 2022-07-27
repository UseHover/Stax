package com.hover.stax.views

import android.content.Context
import com.hover.stax.views.AbstractStatefulInput
import android.widget.AutoCompleteTextView
import androidx.annotation.CallSuper
import android.content.res.TypedArray
import android.text.InputType
import android.util.AttributeSet
import com.hover.stax.R
import com.google.android.material.textfield.TextInputLayout
import android.view.View.OnFocusChangeListener
import android.view.LayoutInflater
import android.view.View
import com.hover.stax.databinding.StaxDropdownBinding

open class StaxDropdownLayout(context: Context, attrs: AttributeSet): AbstractStatefulInput(context, attrs) {

    private val binding: StaxDropdownBinding

    lateinit var autoCompleteTextView: AutoCompleteTextView
    private var hint: String? = null
    private var defaultText: String? = null
    private var editable = false
    private var imeOptions = 0

    @CallSuper
    open fun getAttrs(context: Context, attrs: AttributeSet) {
        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.StaxDropdownLayout, 0, 0)
        try {
            hint = a.getString(R.styleable.StaxDropdownLayout_android_hint)
            defaultText = a.getString(R.styleable.StaxDropdownLayout_android_text)
            editable = a.getBoolean(R.styleable.StaxDropdownLayout_android_editable, false)
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
            if (editable) InputType.TYPE_TEXT_VARIATION_FILTER else InputType.TYPE_NULL
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