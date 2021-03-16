package com.hover.stax.views;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;

import com.hover.stax.R;

public class StaxButton extends CardView {
	private String button_text;
	private int buttonElevation;
	private int button_backgroundRes;
	private int button_text_color;

	private CardView cardView;
	private TextView textView;
	private ProgressBar progressBar;
	private ObjectAnimator objectAnimator;

	public StaxButton(@NonNull Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
		getAttrs(context, attrs);
		LayoutInflater.from(context).inflate(R.layout.stax_button_layout, this);
		initViews();
		fillFromAttr();
	}
	private void getAttrs(Context context, AttributeSet attrs) {
		TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.StaxButton, 0, 0);
		try {
			button_text = a.getString(R.styleable.StaxButton_button_text);
			button_text_color = a.getColor(R.styleable.StaxButton_button_text_color, context.getResources().getColor(R.color.colorPrimary));
			buttonElevation = a.getInteger(R.styleable.StaxButton_button_elevation, 5);
			button_backgroundRes = a.getResourceId(R.styleable.StaxButton_button_background, R.drawable.button_bg_colored);
		} finally {
			a.recycle();
		}
	}

	private void initViews() {
		cardView = findViewById(R.id.button_cardview);
		textView = findViewById(R.id.button_text);
		progressBar = findViewById(R.id.button_progress_bar);
	}

	private void fillFromAttr() {
		if(button_text !=null) textView.setText(button_text);
		if(button_text_color !=0) textView.setTextColor(button_text_color);

		cardView.setCardElevation(buttonElevation);
		if(button_backgroundRes !=0) cardView.setBackgroundResource(button_backgroundRes);
	}
	public void setText(String string) {
		textView.setText(string);
	}

	public void setTextColor(int colorRes) {
		textView.setTextColor(colorRes);
	}

	public void startAnimation(int duration) {
		if(objectAnimator == null) {
			progressBar.setVisibility(VISIBLE);
			objectAnimator = ObjectAnimator.ofInt(progressBar, "progress", progressBar.getProgress(), 100).setDuration(duration);
			objectAnimator.addUpdateListener(valueAnimator -> {
				int progress = (int) valueAnimator.getAnimatedValue();
				progressBar.setProgress(progress);
				if(progress == 100) textView.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
				else textView.setTextColor(getResources().getColor(R.color.offWhite));
			});
		}

		objectAnimator.start();
	}

	public void endAnimation() {
		if(objectAnimator !=null) objectAnimator.end();
	}
}
