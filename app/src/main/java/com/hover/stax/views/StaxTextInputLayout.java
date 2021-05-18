package com.hover.stax.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.hover.stax.R;
import com.hover.stax.databinding.StaxInputBinding;

public class StaxTextInputLayout extends AbstractStatefulInput {
    private final String TAG = "StaxTextInputLayout";

    private String hint;
    private int inputType;
    private TextInputEditText editText;

    StaxInputBinding binding;

    public StaxTextInputLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        getAttrs(context, attrs);

        binding = StaxInputBinding.inflate(LayoutInflater.from(context), this, true);

        initView();
        fillAttr();
    }

    protected void initView() {
        super.initView();
        editText = binding.inputEditText;
    }

    private void getAttrs(Context context, AttributeSet attrs) {
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.StaxTextInputLayout, 0, 0);
        try {
            hint = a.getString(R.styleable.StaxTextInputLayout_android_hint);
            inputType = a.getInt(R.styleable.StaxTextInputLayout_android_inputType, 0);
        } finally {
            a.recycle();
        }
    }

    private void fillAttr() {
        if (hint != null) ((TextInputLayout) findViewById(R.id.inputLayout)).setHint(hint);
        if (inputType > 0)
            ((TextInputEditText) findViewById(R.id.inputEditText)).setInputType(inputType);
    }

    public void setText(String text) {
        editText.setText(text);
        if (text != null && !text.isEmpty())
            setState(null, SUCCESS);
    }

    public String getText() {
        return editText.getText().toString();
    }

    @Override
    public void setOnFocusChangeListener(OnFocusChangeListener l) {
        editText.setOnFocusChangeListener(l);
    }

    public void addTextChangedListener(TextWatcher listener) {
        editText.addTextChangedListener(listener);
    }
}
