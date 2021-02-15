package com.hover.stax.channels;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

import com.google.android.material.textfield.TextInputLayout;
import com.hover.stax.R;
import com.hover.stax.utils.Utils;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.List;

public class ChannelDropdown extends TextInputLayout implements Target {
	private static String TAG = "ChannelDropdown";

	private TextInputLayout input;
	private AutoCompleteTextView dropdownView;
	private TextView linkView;

	private String label;
	private boolean showSelected, showLink;
	private Channel highlightedChannel;
	private HighlightListener highlightListener;
	private LinkViewClickListener linkViewClickListener;

	public ChannelDropdown(Context context, AttributeSet attrs) {
		super(context, attrs);
		getAttrs(context, attrs);
		LayoutInflater.from(context).inflate(R.layout.channel_dropdown, this);
		input = findViewById(R.id.channel_dropdown_input);
		dropdownView = findViewById(R.id.channel_autoComplete);
		linkView = findViewById(R.id.new_account_link);
		fillFromAttrs();
	}

	private void getAttrs(Context context, AttributeSet attrs) {
		TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ChannelDropdown, 0, 0);
		try {
			label = a.getString(R.styleable.ChannelDropdown_label);
			showSelected = a.getBoolean(R.styleable.ChannelDropdown_showSelected, true);
			showLink = a.getBoolean(R.styleable.ChannelDropdown_showLink, false);
		} finally {
			a.recycle();
		}
	}

	private void fillFromAttrs() {
		if (label != null && !label.isEmpty())
			input.setHint(label);
		linkView.setOnClickListener(v -> {
			if(linkViewClickListener !=null) linkViewClickListener.navigateLinkAccountFragment();
		});
		toggleLink(showLink);
	}

	public void setListener(HighlightListener hl) { highlightListener = hl; }
	public void setLinkViewClickListener(LinkViewClickListener linkViewClickListener) {this.linkViewClickListener = linkViewClickListener;}

	public void updateChannels(List<Channel> channels) {
		if (channels == null || channels.size() == 0) return;
		if (highlightedChannel == null)
			setDropdownValue(null);
		ChannelDropdownAdapter channelDropdownAdapter = new ChannelDropdownAdapter(ChannelDropdownAdapter.sort(channels, showSelected), getContext());
		dropdownView.setAdapter(channelDropdownAdapter);
		dropdownView.setOnItemClickListener((adapterView, view2, pos, id) -> onSelect((Channel) adapterView.getItemAtPosition(pos)));
		for (Channel c: channels) {
			if (c.defaultAccount && !showLink)
				setDropdownValue(c);
		}
	}

	private void setDropdownValue(Channel c) {
		dropdownView.setText(c == null ? "" : c.toString(), false);
		if (c == null)
			dropdownView.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0);
		else
			Picasso.get().load(c.logoUrl).resize(55,55).into(this);
	}

	private void onSelect(Channel c) {
		setDropdownValue(c);
		if (highlightListener != null) { highlightListener.highlightChannel(c); }
		highlightedChannel = c;
	}

	public Channel getHighlighted() { return highlightedChannel; }


	public void toggleLink(boolean show) {
		linkView.setVisibility(show ? VISIBLE : GONE);
		input.setVisibility(show ? GONE : VISIBLE);
		if (show) {
			reset();
		}
	}

	@Override
	public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
		RoundedBitmapDrawable d = RoundedBitmapDrawableFactory.create (getContext().getResources(), bitmap);
		d.setCircular(true);
		dropdownView.setCompoundDrawablesRelativeWithIntrinsicBounds(d, null, null, null);
	}

	@Override
	public void onBitmapFailed(Exception e, Drawable errorDrawable) {}

	@Override
	public void onPrepareLoad(Drawable placeHolderDrawable) {
		dropdownView.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_grey_circle_small, 0, 0, 0);
	}

	public void setError(String message) {
		input.setError(message);
		input.setErrorIconDrawable(message != null ? R.drawable.ic_error_warning_24dp : 0);
	}

	public void setHelper(String message) {
		input.setHelperText(message);
	}

	public void reset() {
		if (Utils.isConnected(getContext()))
			setDropdownValue(null);
		highlightedChannel = null;
	}

	public interface HighlightListener {
		void highlightChannel(Channel c);
	}
	//Created a separate listener so it dosent need to be called by channelDropdownViewModel
	public interface LinkViewClickListener {
		void navigateLinkAccountFragment();
	}
}
