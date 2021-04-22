package com.hover.stax.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.AutoCompleteTextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.textfield.TextInputLayout;
import com.hover.stax.R;
import com.hover.stax.databinding.StaxDropdownBinding;

public class StaxDropdownLayout extends AbstractStatefulInput {
	private final String TAG = "StaxDropdownLayout";

	private String hint, defaultText;
	private boolean editable;
	private int imeOptions;

	public AutoCompleteTextView autoCompleteTextView;

	private final StaxDropdownBinding binding;

	public StaxDropdownLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
		getAttrs(context, attrs);

		binding = StaxDropdownBinding.inflate(LayoutInflater.from(context), this, true);

		initView();
		fillAttr();
	}

	private void getAttrs(Context context, AttributeSet attrs) {
		TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.StaxDropdownLayout, 0, 0);
		try {
			hint = a.getString(R.styleable.StaxDropdownLayout_android_hint);
			defaultText = a.getString(R.styleable.StaxDropdownLayout_android_text);
			editable = a.getBoolean(R.styleable.StaxDropdownLayout_android_editable, false);
			imeOptions = a.getInt(R.styleable.StaxDropdownLayout_android_imeOptions, 0);
		} finally {
			a.recycle();
		}
	}

	protected void initView() {
		super.initView();
		autoCompleteTextView = binding.autoCompleteView;
	}

	private void fillAttr() {
		if (hint != null) if (hint != null) ((TextInputLayout) findViewById(R.id.inputLayout)).setHint(hint);
		autoCompleteTextView.setInputType(editable ? InputType.TYPE_TEXT_VARIATION_NORMAL : InputType.TYPE_NULL);
		if (defaultText != null && !defaultText.isEmpty()) autoCompleteTextView.setText(defaultText);
		if(imeOptions > 0) autoCompleteTextView.setImeOptions(imeOptions);
	}

	@Override
	public void setOnFocusChangeListener(OnFocusChangeListener l) { autoCompleteTextView.setOnFocusChangeListener(l); }
}
