package com.hover.stax.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.InputType;
import android.util.AttributeSet;
import android.widget.AutoCompleteTextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.textfield.TextInputLayout;
import com.hover.stax.R;

public class StaxDropdownLayout extends AbstractStatefulInput {
	private final String TAG = "StaxDropdownLayout";

	private String hint, defaultText;
	private boolean editable;

	protected AutoCompleteTextView autoCompleteTextView;

	public StaxDropdownLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
		getAttrs(context, attrs);
		inflate(context, R.layout.stax_dropdown, this);
		initView();
		fillAttr();
	}

	private void getAttrs(Context context, AttributeSet attrs) {
		TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.StaxDropdownLayout, 0, 0);
		try {
			hint = a.getString(R.styleable.StaxDropdownLayout_android_hint);
			defaultText = a.getString(R.styleable.StaxDropdownLayout_android_text);
			editable = a.getBoolean(R.styleable.StaxDropdownLayout_android_editable, false);
		} finally {
			a.recycle();
		}
	}

	protected void initView() {
		super.initView();
		autoCompleteTextView = findViewById(R.id.autoCompleteView);
	}

	private void fillAttr() {
		if (hint != null) if (hint != null) ((TextInputLayout) findViewById(R.id.inputLayout)).setHint(hint);
		autoCompleteTextView.setInputType(editable ? InputType.TYPE_TEXT_VARIATION_NORMAL : InputType.TYPE_NULL);
		if (defaultText != null && !defaultText.isEmpty()) autoCompleteTextView.setText(defaultText);
	}
}
