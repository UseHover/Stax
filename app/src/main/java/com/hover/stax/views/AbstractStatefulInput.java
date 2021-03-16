package com.hover.stax.views;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.textfield.TextInputLayout;
import com.hover.stax.R;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

public abstract class AbstractStatefulInput extends FrameLayout {
	private final static String TAG = "AbstractColoredInput";
	public final static int NONE = 0, INFO = 1, WARN = 2, ERROR = 3, SUCCESS = 4, DISABLED = 5;

	private int currentState;
	private TextInputLayout inputLayout;

	public AbstractStatefulInput(@NonNull Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
	}

	protected void initView() { inputLayout = findViewById(R.id.inputLayout); }

	public void setError(@Nullable CharSequence errorText) {
		setState(errorText == null ? null : errorText.toString(), errorText == null ? NONE : ERROR);
	}

	public void setHint(@Nullable CharSequence helperText) { inputLayout.setHint(helperText); }

	public void setState(String message, int state) {
		Log.e(TAG, "setting state: " + state);
		currentState = state;
		inputLayout.setHelperText(message);
		switch (state) {
			case INFO: setColorAndIcon(R.color.blue_state_color, R.drawable.ic_info); break;
			case WARN: setColorAndIcon(R.color.yellow_state_color, R.drawable.ic_warning); break;
			case SUCCESS: setColorAndIcon(R.color.green_state_color, R.drawable.ic_success); break;
			case ERROR: setColorAndIcon(R.color.red_state_color, R.drawable.ic_error); break;
			case DISABLED: setColorAndIcon(R.color.grey_state_color, 1); break;
			default: setColorAndIcon(R.color.offwhite_state_color, 0); break;
		}
	}

	private void setColorAndIcon(int color, int drawable) {
		if (inputLayout != null) {
			if(drawable != 0) inputLayout.setEndIconDrawable(drawable);
			setColor(color);
		}
	}

	private void setColor(int color) {
		try {
			ColorStateList csl = ColorStateList.createFromXml(getResources(), getResources().getXml(color));
			inputLayout.setHelperTextColor(csl);
			inputLayout.setEndIconTintList(csl);
			inputLayout.setHintTextColor(csl);
			inputLayout.setBoxStrokeColorStateList(csl);
		} catch (IOException | XmlPullParserException | NullPointerException e) { Log.e(TAG, "Failed to load color state list", e); }
	}
}
