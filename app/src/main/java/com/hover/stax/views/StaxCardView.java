package com.hover.stax.views;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.hover.stax.R;
import com.hover.stax.databinding.StaxCardViewBinding;

public class StaxCardView extends FrameLayout {

	private String title;
	private boolean showBack, useContextBackPress;

	private int backDrawable = 0;
	private int bgColor;

	private final StaxCardViewBinding binding;

	public StaxCardView(Context context, AttributeSet attrs) {
		super(context, attrs);
		getAttrs(context, attrs);

		binding = StaxCardViewBinding.inflate(LayoutInflater.from(context), this, true);
		fillFromAttrs();
	}

	private void getAttrs(Context context, AttributeSet attrs) {
		TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.StaxCardView, 0, 0);
		try {
			title = a.getString(R.styleable.StaxCardView_title);
			showBack = a.getBoolean(R.styleable.StaxCardView_showBack, false);
			useContextBackPress = a.getBoolean(R.styleable.StaxCardView_defaultBackPress, true);
			backDrawable = a.getResourceId(R.styleable.StaxCardView_backRes, 0);
			bgColor = a.getColor(R.styleable.StaxCardView_staxCardColor, context.getResources().getColor(R.color.colorPrimary));
		} finally {
			a.recycle();
		}
	}
	
	@SuppressLint("ResourceType")
	public void setBackgroundColor(int colorRes) {
		bgColor = getContext().getResources().getColor(colorRes);
		binding.content.setBackgroundColor(bgColor);
	}

	public void setTitle(String t) {
		if (t != null) binding.title.setText(t);
	}

	public void setTitle(int titleString) {
		if (titleString != 0) binding.title.setText(getContext().getString(titleString));
	}

	public void setIcon(int icon) {
		if (icon != 0) { binding.backButton.setImageResource(icon); }
	}

	public void setOnClickIcon(OnClickListener listener) {
		if (listener != null) { binding.backButton.setOnClickListener(listener); }
	}

	private void fillFromAttrs() {
		if (title != null) binding.title.setText(title);
		else findViewById(R.id.header).setVisibility(GONE);

		if (useContextBackPress) binding.backButton.setOnClickListener(view -> triggerBack());
		if (showBack) binding.backButton.setVisibility(VISIBLE);
		if (backDrawable != 0) binding.backButton.setImageResource(backDrawable);
		binding.content.setBackgroundColor(bgColor);
	}

	@Override
	public void addView(View child, int index, ViewGroup.LayoutParams params) {
		if (binding.content == null)
			super.addView(child, index, params);
		else
			binding.content.addView(child, index, params);
	}

	private void triggerBack() {
		try {
			((Activity) getContext()).onBackPressed();
		} catch (Exception ignored) {
		}
	}
}