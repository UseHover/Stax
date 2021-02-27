package com.hover.stax.views;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.AttributeSet;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.textfield.TextInputLayout;
import com.hover.stax.R;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

public abstract class AbstractColoredInput extends TextInputLayout {
	private final static String TAG = "AbstractColoredInput";
	public final static int NONE = 0, INFO = 1, WARN = 2, ERROR = 3, SUCCESS = 4;

	private String currentMessage;
	private int currentState;

	public AbstractColoredInput(@NonNull Context context, @Nullable AttributeSet attrs) {
		super(context, attrs, R.style.StaxLabeledInput);
	}

	@Override
	public void setError(@Nullable CharSequence errorText) {
		setState(errorText == null ? null : errorText.toString(), errorText == null ? NONE : ERROR);
	}

	@Override
	public void setHelperText(@Nullable CharSequence helperText) {
		super.setHelperText(helperText);
		setState(helperText == null ? null : helperText.toString(), helperText == null ? NONE : INFO);
	}

	public void setState(String message, int state) {
		if (currentState == state) return;
		currentState = state;
		Log.e(TAG, "setting state: " + state);
		super.setError(null);
		switch (state) {
			case INFO: setColorAndIcon(R.color.blue_state_color, R.drawable.ic_info); break;
			case WARN: setColorAndIcon(R.color.yellow_state_color, R.drawable.ic_warning); break;
			case SUCCESS: setColorAndIcon(R.color.green_state_color, R.drawable.ic_success); break;
			case ERROR: setColorAndIcon(R.color.red_state_color, R.drawable.ic_error); super.setError(message); break;
			case NONE:
			default: setColorAndIcon(R.color.offwhite_state_color, 0); break;
		}
	}

	protected void setColorAndIcon(int color, int drawable) {
		if (findViewById(R.id.inputLayout) != null)
			((TextInputLayout) findViewById(R.id.inputLayout)).setEndIconDrawable(drawable);
		setColor(color);
	}

	protected void setColor(int color) {
		try {
			ColorStateList csl = ColorStateList.createFromXml(getResources(), getResources().getXml(color));
			((TextInputLayout) findViewById(R.id.inputLayout)).setHelperTextColor(csl);
			((TextInputLayout) findViewById(R.id.inputLayout)).setEndIconTintList(csl);
			((TextInputLayout) findViewById(R.id.inputLayout)).setHintTextColor(csl);
			((TextInputLayout) findViewById(R.id.inputLayout)).setBoxStrokeColorStateList(csl);
		} catch (IOException | XmlPullParserException | NullPointerException e) { Log.e(TAG, "Failed to load color state list", e); }
	}
}
