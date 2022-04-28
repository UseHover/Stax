package com.hover.stax.views

import android.annotation.SuppressLint
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import com.google.android.material.textfield.TextInputEditText
import com.hover.stax.R
import com.hover.stax.databinding.StaxTextInputBinding
import timber.log.Timber


class StaxTextInput(context: Context, attrs: AttributeSet) : AbstractStatefulInput(context, attrs) {

    private var hint: String? = null
    private var currentText: String? = null

    private var inputType: Int = -1
    var editText: TextInputEditText? = null

    var clickListener: OnClickListener? = null

    var binding: StaxTextInputBinding? = null

    init {
        getAttrs(context, attrs)
        binding = StaxTextInputBinding.inflate(LayoutInflater.from(context), this, true)
        initView()
        fillAttrs()
    }

    override fun initView() {
        super.initView()
        editText = binding?.inputEditText
        if (inputType > 0) binding?.inputEditText?.inputType = inputType
    }

    private fun getAttrs(context: Context, attrs: AttributeSet) {
        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.StaxTextInputLayout, 0, 0)
        Timber.e("Initing text layout %s", a.getString(R.styleable.StaxTextInputLayout_android_hint))
        try {
            hint = a.getString(R.styleable.StaxTextInputLayout_android_hint)
            inputType = a.getInt(R.styleable.StaxTextInputLayout_android_inputType, 0)
        } finally {
            a.recycle()
        }
    }

    fun setHint(helperText: CharSequence?) {
        hint = helperText?.toString()
        hint?.let { binding?.inputLayout?.hint = it }
        invalidate()
        requestLayout()
    }

    private fun fillAttrs() {
        hint?.let { binding?.inputLayout?.hint = it }
        currentText?.let { if (editText?.text.toString() != currentText) editText?.setText(currentText) }
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
            editText?.setText(it)

            if (it.isNotEmpty())
                setState(null, SUCCESS)
        }
        invalidate()
        requestLayout()
    }

    fun setText(text: String?, updateState: Boolean) {
        currentText = text
        text?.let {
            editText?.setText(it)

            if (updateState && it.isNotEmpty())
                setState(null, SUCCESS)
        }
        invalidate()
        requestLayout()
    }

    val text get() = editText?.text.toString()

    override fun setOnFocusChangeListener(l: OnFocusChangeListener?) {
        editText?.onFocusChangeListener = l
    }

    fun addTextChangedListener(listener: TextWatcher) {
        editText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) { listener.beforeTextChanged(charSequence, i, i1, i2)}
            override fun afterTextChanged(editable: Editable) { listener.afterTextChanged(editable)}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                currentText = charSequence.toString()
                listener.onTextChanged(charSequence, i, i1, i2)
            }
        })
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun setOnClickListener(listener: OnClickListener?) {
        clickListener = listener
        editText?.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN && clickListener != null) {
                clickListener?.onClick(this)
                clickListener != null
            }
            false
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        fillAttrs()
    }

    public override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        Timber.e("Destroying view")
        clickListener = null
        editText = null
        hint = null
        binding = null

    }
}