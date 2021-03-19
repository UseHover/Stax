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

import com.hover.sdk.actions.HoverAction;
import com.hover.stax.R;
import com.hover.stax.views.AbstractStatefulInput;
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

	public void channelUpdateIfNull(List<Channel> channels) {
		if (channels != null && channels.size() > 0 && (autoCompleteTextView.getAdapter() == null || autoCompleteTextView.getAdapter().getCount() == 0))
			channelUpdate(channels);
	}
	public void channelUpdate(List<Channel> channels) {
		if ((channels == null || channels.size() == 0) && (autoCompleteTextView.getAdapter() == null || autoCompleteTextView.getAdapter().getCount() == 0)) {
			setState(getContext().getString(R.string.channels_error_nodata), ERROR);
			return;
		}
		setState(null, NONE);
		updateChoices(channels);
	}

	private void setDropdownValue(Channel c) {
		autoCompleteTextView.setText(c == null ? "" : c.toString(), false);
		if (c != null)
			Picasso.get().load(c.logoUrl).resize(55, 55).into(this);
	}

	private void updateChoices(List<Channel> channels) {
		if (highlightedChannel == null) setDropdownValue(null);
		ChannelDropdownAdapter channelDropdownAdapter = new ChannelDropdownAdapter(ChannelDropdownAdapter.sort(channels, showSelected), getContext());
		autoCompleteTextView.setAdapter(channelDropdownAdapter);
		autoCompleteTextView.setOnItemClickListener((adapterView, view2, pos, id) -> onSelect((Channel) adapterView.getItemAtPosition(pos)));

		for (Channel c: channels) {
			if (c.defaultAccount && showSelected)
				setDropdownValue(c);
		}
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
		viewModel.getChannels().observe(lifecycleOwner, this::channelUpdateIfNull);
		viewModel.getSimChannels().observe(lifecycleOwner, this::channelUpdate);
		viewModel.getSelectedChannels().observe(lifecycleOwner, channels -> Log.i(TAG, "Got new selected channels: " + channels.size()));
		viewModel.getActiveChannel().observe(lifecycleOwner, channel -> { if (channel != null) setState(null, NONE); });
		viewModel.getChannelActions().observe(lifecycleOwner, actions -> setState(actions, viewModel));
	}

	private void setState(List<HoverAction> actions, ChannelDropdownViewModel viewModel) {
		if (viewModel.getActiveChannel().getValue() != null && (actions == null || actions.size() == 0))
			setState(getContext().getString(R.string.no_actions_fielderror, HoverAction.getHumanFriendlyType(getContext(), viewModel.getType())), AbstractStatefulInput.ERROR);
		else if (actions != null && actions.size() == 1 && !actions.get(0).requiresRecipient() && !viewModel.getType().equals(HoverAction.BALANCE))
			setState(getContext().getString(actions.get(0).transaction_type.equals(HoverAction.AIRTIME) ? R.string.self_only_airtime_warning : R.string.self_only_money_warning), INFO);
		else if (viewModel.getActiveChannel().getValue() != null && showSelected)
			setState(null, AbstractStatefulInput.SUCCESS);
	}

	public interface HighlightListener {
		void highlightChannel(Channel c);
	}
}
