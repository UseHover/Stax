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
import com.hover.stax.utils.fieldstates.FieldState;
import com.hover.stax.utils.Utils;

public class StaxDropdownLayout extends TextInputLayout {

	private final String TAG = "StaxDropdownLayout";
	private String hint, defaultText;
	private boolean editable;
	private TextInputLayout textInputLayout;
	private AutoCompleteTextView autoCompleteTextView;
	private ImageView dropDownIcon;

	public StaxDropdownLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
		getAttrs(context, attrs);
		LayoutInflater.from(context).inflate(R.layout.custom_channeldropdown_layout, this);
		initViews();
		fillAttr();
	}

	private void initViews() {
		textInputLayout = findViewById(R.id.dropdownInputLayout);
		autoCompleteTextView = findViewById(R.id.dropdownInputTextView);
		dropDownIcon = findViewById(R.id.dropdownNoticeIcon);
	}

	private void getAttrs(Context context, AttributeSet attrs) {
		TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.StaxDropdownLayout, 0, 0);
		try {
			hint = a.getString(R.styleable.StaxDropdownLayout_hint_value);
			defaultText = a.getString(R.styleable.StaxDropdownLayout_defaultText);
			editable = a.getBoolean(R.styleable.StaxDropdownLayout_editable, true);
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

	public void setFieldState(FieldState fieldState) {
		if(fieldState == null) setNormal();
		else {
			switch (fieldState.getFieldStateType()) {
				case INFO: setInfo(fieldState.getMessage());
					break;
				case WARNING: setWarning(fieldState.getMessage());
					break;
				case ERROR: setError(fieldState.getMessage());
					break;
				case SUCCESS: setSuccess(fieldState.getMessage());
					break;
			}
		}
	}

	public void setError(String message) {
		if (message != null) {
			textInputLayout.setError(message);
			showErrorIcon();
		} else {
			setNormal();
		}
	}

	public void setWarning(String message) {
		if (message != null) {
			textInputLayout.setHelperText(message);
			setHelperColorState(R.color.yellow_state_color);
			showWarningIcon();
		} else {
			setNormal();
		}
	}

	public void setInfo(String message) {
		if (message != null) {
			textInputLayout.setHelperText(message);
			setHelperColorState(R.color.blue_state_color);
			showInfoIcon();
		} else {
			setNormal();
		}
	}

	public void setSuccess(String message) {
		if (message != null) {
			textInputLayout.setError(null);
			textInputLayout.setHelperText(message);
			setHelperColorState(R.color.green_state_color);
			showSuccessIcon();
		} else setNormal();
	}

	public void setNormal() {
		setHelperColorState(R.color.offwhite_state_color);
		textInputLayout.setHelperText(null);
		textInputLayout.setError(null);
		removeNoticeIcon();
	}

	private void setHelperColorState(int id) {
		try {
			XmlResourceParser parser = getResources().getXml(id);
			ColorStateList colors = ColorStateList.createFromXml(getResources(), parser);
			textInputLayout.setHelperTextColor(colors);
			textInputLayout.setDefaultHintTextColor(colors);
			textInputLayout.setBoxStrokeColorStateList(colors);
		} catch (Exception e) {
			Utils.logErrorAndReportToFirebase(TAG, e.getMessage(), e);
		}
	}

	private void showSuccessIcon() {
		dropDownIcon.setImageResource(R.drawable.ic_success_check_circle_24);
		dropDownIcon.setVisibility(VISIBLE);
	}

	private void showErrorIcon() {
		dropDownIcon.setImageResource(R.drawable.ic_error_warning_24dp);
		dropDownIcon.setVisibility(VISIBLE);
	}

	private void showWarningIcon() {
		dropDownIcon.setImageResource(R.drawable.ic_warning_yellow_24);
		dropDownIcon.setVisibility(VISIBLE);
	}

	private void showInfoIcon() {
		dropDownIcon.setImageResource(R.drawable.ic_info_24dp);
		dropDownIcon.setVisibility(VISIBLE);
	}

	private void removeNoticeIcon() {
		dropDownIcon.setImageResource(0);
		dropDownIcon.setVisibility(GONE);
	}
}
