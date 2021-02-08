package com.hover.stax.channels;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputLayout;
import com.hover.sdk.utils.VolleySingleton;
import com.hover.stax.R;
import com.hover.stax.utils.UIHelper;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.IOException;
import java.util.List;

public class ChannelDropdown extends TextInputLayout {
	private static String TAG = "ChannelDropdown";

	private TextInputLayout input;
	private AutoCompleteTextView dropdownView;
	private TextView linkView;

	private String label;
	private boolean showSelected, showLink;
	private Channel highlightedChannel;
	private HighlightListener highlightListener;

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
		linkView.setOnClickListener(v -> toggleLink(false));
		toggleLink(showLink);
	}

	public void setListener(HighlightListener hl) { highlightListener = hl; }

	public void updateChannels(List<Channel> channels) {
		if (channels == null || channels.size() == 0) return;
		if (highlightedChannel == null)
			dropdownView.setText("");
		ChannelDropdownAdapter channelDropdownAdapter = new ChannelDropdownAdapter(ChannelDropdownAdapter.sort(channels, showSelected), getContext());
		dropdownView.setAdapter(channelDropdownAdapter);
		dropdownView.setOnItemClickListener((adapterView, view2, pos, id) -> onSelect((Channel) adapterView.getItemAtPosition(pos)));
		for (Channel c: channels) {
			if (c.defaultAccount && !showLink) {
				dropdownView.setText(c.toString() + " " + c.countryAlpha2.toUpperCase(), false);
				UIHelper.setAutoCompleteTextDrawable(dropdownView, c.logoUrl, getContext());
			}
		}
	}

	private void onSelect(Channel c) {
		dropdownView.setText(c.name, false);
		UIHelper.setAutoCompleteTextDrawable(dropdownView, c.logoUrl, getContext());
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

	public void setError(String message) {
		input.setError(message);
		input.setErrorIconDrawable(message != null ? R.drawable.ic_error_warning_24dp : 0);
	}

	public void setHelper(String message) {
		input.setHelperText(message);
	}

	public void reset() {
		if (VolleySingleton.isConnected(getContext()))
			dropdownView.setText("");
		highlightedChannel = null;
	}

	public interface HighlightListener {
		void highlightChannel(Channel c);
	}
}
