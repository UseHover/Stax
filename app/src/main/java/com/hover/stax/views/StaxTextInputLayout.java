package com.hover.stax.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.hover.stax.R;

public class StaxTextInputLayout extends AbstractColoredInput {
    private final String TAG = "StaxTextInputLayout";

	private String hint;
	private int inputType;

	private TextInputEditText textInputEditText;

	public StaxTextInputLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);

		getAttrs(context,attrs);
		LayoutInflater.from(context).inflate(R.layout.stax_input, this);
		initViews();
		fillAttr();
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

	private void initViews() {
		textInputLayout = findViewById(R.id.textInputLayoutId);
		textInputEditText = findViewById(R.id.inputEditText);
	}

	private void fillAttr() {
		if (hint != null) textInputLayout.setHint(hint);
		if (inputType > 0) textInputEditText.setInputType(inputType);
	}
}
