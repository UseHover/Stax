package com.hover.stax.utils.customViews;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;

import androidx.cardview.widget.CardView;

import com.hover.stax.R;

public class StaxCardView extends CardView {


	public StaxCardView(Context context, AttributeSet attrs) {
		super(context, attrs);
		LayoutInflater inflater;
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.stax_card_view, this);
	}
}