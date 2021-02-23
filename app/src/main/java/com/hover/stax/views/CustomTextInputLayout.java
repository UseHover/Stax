package com.hover.stax.views;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.hover.stax.R;
import com.hover.stax.utils.Utils;

public class CustomTextInputLayout extends TextInputLayout {
    private final String TAG = "CustomTextInputLayout";
	private String hint;
	private int inputType;
	private TextInputLayout textInputLayout;
	private TextInputEditText textInputEditText;

	public CustomTextInputLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);

		getAttrs(context,attrs);
		LayoutInflater.from(context).inflate(R.layout.custom_textinputlayout, this);
		initViews();
		fillAttr();
	}

	//INITIALIZATIONS
	private void initViews() {
		textInputLayout = findViewById(R.id.textInputLayoutId);
		textInputEditText = findViewById(R.id.textInputEditTextId);
	}
	private void getAttrs(Context context, AttributeSet attrs) {
		TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.CustomTextInputLayout, 0, 0);
		try {
			hint = a.getString(R.styleable.CustomTextInputLayout_hint);
			inputType = a.getInt(R.styleable.CustomTextInputLayout_android_inputType, 0);
		} finally {
			a.recycle();
		}
	}
	private void fillAttr() {
		if(hint !=null)  textInputLayout.setHint(hint);
		if(inputType >0) textInputEditText.setInputType(inputType);
	}

	//SET STATES
	public void setError(String message) {
		if(message !=null) {
			textInputLayout.setError(message);
			showErrorIcon();
		}
		else { setNormal(); }
	}

	public void setWarning(String message) {
		if(message !=null) {
			textInputLayout.setHelperText(message);
			setHelperColorState(R.color.yellow_state_color, true);
			textInputLayout.setBoxStrokeColor(getResources().getColor(R.color.stax_state_yellow));
			showWarningIcon();
		}
		else { setNormal(); }
	}

	public void setInfo(String message) {
		if(message !=null) {
			textInputLayout.setHelperText(message);
			setHelperColorState(R.color.blue_state_color, true);
			textInputLayout.setBoxStrokeColor(getResources().getColor(R.color.stax_state_blue));
			showInfoIcon();
		}
		else { setNormal(); }
	}

	public void setSuccess(String message) {
		if(message !=null) {
			textInputLayout.setHelperText(message);
			setHelperColorState(R.color.green_state_color, true);
			textInputLayout.setBoxStrokeColor(getResources().getColor(R.color.stax_state_green));
			showSuccessIcon();
		} else setNormal();
	}
	public void setNormal() {
		setHelperColorState(R.color.offwhite_state_color, false);
		textInputLayout.setHelperText(null);
		textInputLayout.setError(null);
		removeNoticeIcon();
		textInputLayout.setBoxStrokeColor(getResources().getColor(R.color.offWhite));

	}

	//PRIVATE METHODS
	private void setHelperColorState(int id, boolean requestFocus) {
		try {
			XmlResourceParser parser = getResources().getXml(id);
			ColorStateList colors = ColorStateList.createFromXml(getResources(), parser);
			textInputLayout.setHelperTextColor(colors);
			textInputLayout.setHintTextColor(colors);
			if(requestFocus) textInputLayout.requestFocus();
		} catch (Exception e) { Utils.logErrorAndReportToFirebase(TAG, e.getMessage(), e); }
	}
	private void showSuccessIcon() {
		textInputEditText.setCompoundDrawablesRelativeWithIntrinsicBounds(0,0,R.drawable.ic_success_check_circle_24, 0);
	}
	private void showErrorIcon() {
		textInputEditText.setCompoundDrawablesRelativeWithIntrinsicBounds(0,0,R.drawable.ic_error_warning_24dp, 0);
	}
	private void showWarningIcon() {
		textInputEditText.setCompoundDrawablesRelativeWithIntrinsicBounds(0,0,R.drawable.ic_warning_yellow_24, 0);
	}
	private void showInfoIcon() {
		textInputEditText.setCompoundDrawablesRelativeWithIntrinsicBounds(0,0,R.drawable.ic_info_24dp, 0);
	}
	private void removeNoticeIcon(){
		textInputEditText.setCompoundDrawablesRelativeWithIntrinsicBounds(0,0,0, 0);
	}
}
