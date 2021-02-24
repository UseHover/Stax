package com.hover.stax.views;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.util.AttributeSet;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.hover.stax.R;

public class StaxTextInputLayout extends TextInputLayout {
    private final String TAG = "StaxTextInputLayout";
    public final static int NONE = 0, INFO = 1, WARN = 2, ERROR = 3, SUCCESS = 4;

	private TextInputLayout textInputLayout;
	private String hint;

	public StaxTextInputLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
		getAttrs(context, attrs);
		inflate(context, R.layout.stax_input, this);
		textInputLayout = findViewById(R.id.textInputLayoutId);
		textInputLayout.setHint(hint);
	}

	private void getAttrs(Context context, AttributeSet attrs) {
		TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.StaxTextInputLayout, 0, 0);
		try {
			hint = a.getString(R.styleable.StaxTextInputLayout_android_hint);
		} finally {
			a.recycle();
		}
	}

//	@Override
//	public void addView(View child, int index, ViewGroup.LayoutParams params) {
//		if (textInputLayout == null)
//			super.addView(child, index, params);
//		else
//			textInputLayout.addView(child, index, params);
//		setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
//	}

	public void setError(String message) {
		textInputLayout.setError(message);
		setState(message, message == null ? NONE : ERROR);
	}

	public void setState(String message, int state) {
		if (state != ERROR)
			textInputLayout.setHelperText(message);
		switch (state) {
			case INFO: setColorAndIcon(R.color.stax_state_blue, R.drawable.ic_info); break;
			case WARN: setColorAndIcon(R.color.stax_state_yellow, R.drawable.ic_warning); break;
			case SUCCESS: setColorAndIcon(R.color.stax_state_green, R.drawable.ic_success); break;
			case ERROR: setColorAndIcon(R.color.stax_state_red, R.drawable.ic_error); break;
			case NONE:
			default: setColorAndIcon(R.color.offWhite, 0); break;
		}
	}

	private void setColorAndIcon(int color, int drawable) {
		setColor(color);
		textInputLayout.setEndIconDrawable(drawable);
	}

	private void setColor(int color) {
		ColorStateList csl = new ColorStateList(new int[][] { new int[]{} }, new int[]{ color });
		textInputLayout.setHelperTextColor(csl);
		textInputLayout.setDefaultHintTextColor(csl);
		textInputLayout.setBoxStrokeColorStateList(csl);
	}
}
