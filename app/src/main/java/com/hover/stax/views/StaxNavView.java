package com.hover.stax.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.hover.stax.databinding.StaxNavViewBinding;

public class StaxNavView extends CoordinatorLayout {

	private final FrameLayout contentView;

	public StaxNavView(Context context, AttributeSet attrs) {
		super(context, attrs);

		StaxNavViewBinding binding = StaxNavViewBinding.inflate(LayoutInflater.from(context), this, true);
		contentView = binding.content;
	}

	@Override
	public void addView(View child, int index, ViewGroup.LayoutParams params) {
		if (contentView == null)
			super.addView(child, index, params);
		else
			contentView.addView(child, index, params);
	}
}