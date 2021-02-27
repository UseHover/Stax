package com.hover.stax.channels;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.lifecycle.LifecycleOwner;

import com.hover.stax.R;
import com.hover.stax.views.StaxDropdownLayout;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.List;

public class ChannelDropdown extends StaxDropdownLayout implements Target {
	private static String TAG = "ChannelDropdown";

	private boolean showSelected;
	private Channel highlightedChannel;
	private HighlightListener highlightListener;

	public ChannelDropdown(Context context, AttributeSet attrs) {
		super(context, attrs);
		getAttrs(context, attrs);
	}

	private void getAttrs(Context context, AttributeSet attrs) {
		TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ChannelDropdown, 0, 0);
		try {
			showSelected = a.getBoolean(R.styleable.ChannelDropdown_show_selected, true);
		} finally {
			a.recycle();
		}
	}

	public void setListener(HighlightListener hl) { highlightListener = hl; }

	public void updateChannels(List<Channel> channels) {
		if (channels == null || channels.size() == 0) return;
		Log.e(TAG, "found some channels " + channels.size());
		if (highlightedChannel == null) setDropdownValue(null);
		ChannelDropdownAdapter channelDropdownAdapter = new ChannelDropdownAdapter(ChannelDropdownAdapter.sort(channels, showSelected), getContext());
		autoCompleteTextView.setAdapter(channelDropdownAdapter);
		autoCompleteTextView.setOnItemClickListener((adapterView, view2, pos, id) -> onSelect((Channel) adapterView.getItemAtPosition(pos)));

		for (Channel c: channels) {
			if (c.defaultAccount && showSelected)
				setDropdownValue(c);
		}
	}

	private void setDropdownValue(Channel c) {
		autoCompleteTextView.setText(c == null ? "" : c.toString(), false);
		if (c != null)
			Picasso.get().load(c.logoUrl).resize(55, 55).into(this);
	}

	private void onSelect(Channel c) {
		setDropdownValue(c);
		if (highlightListener != null) { highlightListener.highlightChannel(c); }
		highlightedChannel = c;
	}

	public Channel getHighlighted() { return highlightedChannel; }

	@Override
	public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
		RoundedBitmapDrawable d = RoundedBitmapDrawableFactory.create (getContext().getResources(), bitmap);
		d.setCircular(true);
		autoCompleteTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(d, null, null, null);
	}

	@Override
	public void onBitmapFailed(Exception e, Drawable errorDrawable) {}

	@Override
	public void onPrepareLoad(Drawable placeHolderDrawable) {
		autoCompleteTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_grey_circle_small, 0, 0, 0);
	}

	public void setObservers(@NonNull ChannelDropdownViewModel viewModel, @NonNull LifecycleOwner lifecycleOwner) {
		viewModel.getSims().observe(lifecycleOwner, sims -> Log.i(TAG, "Got sims: " + sims.size()));
		viewModel.getSimHniList().observe(lifecycleOwner, simList -> Log.i(TAG, "Got new sim hni list: " + simList));
		viewModel.getChannels().observe(lifecycleOwner, this::updateChannels);
		viewModel.getSimChannels().observe(lifecycleOwner, this::updateChannels);
		viewModel.getSelectedChannels().observe(lifecycleOwner, channels -> {
			if (channels != null && channels.size() > 0) setError(null);
		});
		viewModel.getChannelActions().observe(lifecycleOwner, actions -> { if (actions != null && actions.size() > 0) setState(null, SUCCESS); });
//		viewModel.getHelper().observe(lifecycleOwner, helper -> setState(helper != null ?  getContext().getString(helper) : null, AbstractColoredInput.NONE));
		viewModel.getError().observe(lifecycleOwner, error -> setError(error));
	}

	public interface HighlightListener {
		void highlightChannel(Channel c);
	}
}
