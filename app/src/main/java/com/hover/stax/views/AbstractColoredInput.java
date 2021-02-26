package com.hover.stax.views;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.textfield.TextInputLayout;
import com.hover.stax.R;

public abstract class AbstractColoredInput extends TextInputLayout {
	public final static int NONE = 0, INFO = 1, WARN = 2, ERROR = 3, SUCCESS = 4;

	protected TextInputLayout textInputLayout;

	public AbstractColoredInput(@NonNull Context context, @Nullable AttributeSet attrs) {
		super(context, attrs, R.style.StaxLabeledInput);
	}

	public void setError(String message) {
		textInputLayout.setError(message);
		setState(message, message == null ? NONE : ERROR);
	}

	public void setNormal() {
		setState(null, NONE);
	}

	public void setState(String message, int state) {
		if (state != ERROR) {
			textInputLayout.setError(null);
			textInputLayout.setHelperText(message);
		}
		switch (state) {
			case INFO: setColorAndIcon(R.color.stax_state_blue, R.drawable.ic_info); break;
			case WARN: setColorAndIcon(R.color.stax_state_yellow, R.drawable.ic_warning); break;
			case SUCCESS: setColorAndIcon(R.color.stax_state_green, R.drawable.ic_success); break;
			case ERROR: setColorAndIcon(R.color.stax_state_red, R.drawable.ic_error); break;
			case NONE:
			default: setColorAndIcon(R.color.offWhite, 0); break;
		}
	}

	protected void setColorAndIcon(int color, int drawable) {
		setColor(color);
		textInputLayout.setEndIconDrawable(drawable);
	}

	protected void setColor(int color) {
		ColorStateList csl = new ColorStateList(new int[][]{new int[]{}}, new int[]{color});
		textInputLayout.setHelperTextColor(csl);
		textInputLayout.setDefaultHintTextColor(csl);
		textInputLayout.setBoxStrokeColorStateList(csl);
	}
}
