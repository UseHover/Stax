package com.hover.stax.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.hover.stax.R;
import com.wangjie.rapidfloatingactionbutton.RapidFloatingActionLayout;

class StaxNavView extends RapidFloatingActionLayout {
	private FrameLayout contentView;

	public StaxNavView(Context context, AttributeSet attrs) {
		super(context, attrs);
		LayoutInflater.from(context).inflate(R.layout.stax_nav_view, this);
		contentView = (FrameLayout) findViewById(R.id.content);
	}

	@Override
	public void addView(View child, int index, ViewGroup.LayoutParams params) {
		if (contentView == null)
			super.addView(child, index, params);
		else
			contentView.addView(child, index, params);
	}
}