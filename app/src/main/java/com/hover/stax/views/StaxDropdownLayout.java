package com.hover.stax.views;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.textfield.TextInputLayout;
import com.hover.stax.R;
import com.hover.stax.utils.Utils;

public class StaxDropdownLayout extends AbstractColoredInput {
	private final String TAG = "StaxDropdownLayout";

	private String hint, defaultText;
	private boolean editable;

	private AutoCompleteTextView autoCompleteTextView;

	public StaxDropdownLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
		getAttrs(context, attrs);
		inflate(context, R.layout.stax_dropdown, this);
		initViews();
		fillAttr();
	}

	private void initViews() {
		textInputLayout = findViewById(R.id.dropdownInputLayout);
		autoCompleteTextView = findViewById(R.id.autoCompleteView);
	}

	private void getAttrs(Context context, AttributeSet attrs) {
		TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.StaxDropdownLayout, 0, 0);
		try {
			hint = a.getString(R.styleable.StaxDropdownLayout_android_hint);
			defaultText = a.getString(R.styleable.StaxDropdownLayout_android_text);
			editable = a.getBoolean(R.styleable.StaxDropdownLayout_android_editable, true);
		} finally {
			a.recycle();
		}
	}

	private void fillAttr() {
		if (hint != null) setHint(hint);
		autoCompleteTextView.setInputType(editable ? InputType.TYPE_TEXT_VARIATION_NORMAL : InputType.TYPE_NULL);
		if (defaultText != null && !defaultText.isEmpty()) autoCompleteTextView.setText(defaultText);
	}

	public void setHint(String message) {
		textInputLayout.setHint(message);
	}
}
