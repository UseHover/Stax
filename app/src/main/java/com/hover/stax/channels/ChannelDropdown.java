package com.hover.stax.channels;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;

import com.hover.sdk.actions.HoverAction;
import com.hover.stax.R;
import com.hover.stax.utils.UIHelper;
import com.hover.stax.views.AbstractStatefulInput;
import com.hover.stax.views.StaxDropdownLayout;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;


import java.util.List;

import timber.log.Timber;

import static com.hover.stax.utils.Constants.size55;


public class ChannelDropdown extends StaxDropdownLayout implements Target{

    private boolean showSelected;
    private String initial_helper_text;
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
            initial_helper_text = a.getString(R.styleable.ChannelDropdown_initial_helper_text);
        } finally {
            a.recycle();
        }
    }

    public void setListener(HighlightListener hl) {
        highlightListener = hl;
    }

    public void channelUpdateIfNull(List<Channel> channels) {
        if (channels != null && channels.size() > 0 && !hasExistingContent()) {
            setState(getContext().getString(R.string.channels_error_nosim), INFO);
            updateChoices(channels);
        } else if (!hasExistingContent())
            setEmptyState();
    }

    public void channelUpdate(List<Channel> channels) {
        if (channels != null && channels.size() > 0) {
            setState(null, NONE);
            updateChoices(channels);
        } else if (!hasExistingContent())
            setEmptyState();
    }

    private boolean hasExistingContent() {
        return autoCompleteTextView.getAdapter() != null && autoCompleteTextView.getAdapter().getCount() > 0;
    }

    private void setEmptyState() {
        autoCompleteTextView.setDropDownHeight(0);
        setState(getContext().getString(R.string.channels_error_nodata), ERROR);
    }

    private void setDropdownValue(Channel c) {
        autoCompleteTextView.setText(c == null ? "" : c.toString(), false);
        if (c != null)
            UIHelper.loadPicasso(c.logoUrl, size55, this);
    }

    private void updateChoices(List<Channel> channels) {
        if (highlightedChannel == null) setDropdownValue(null);
        ChannelsDropdownAdapter channelsDropdownAdapter = new ChannelsDropdownAdapter(Channel.sort(channels, showSelected), getContext());
        autoCompleteTextView.setAdapter(channelsDropdownAdapter);
        autoCompleteTextView.setDropDownHeight(UIHelper.dpToPx(300));
        autoCompleteTextView.setOnItemClickListener((adapterView, view2, pos, id) -> onSelect((Channel) adapterView.getItemAtPosition(pos)));

        for (Channel c : channels) {
            if (c.defaultAccount && showSelected)
                setDropdownValue(c);
        }
    }

    private void onSelect(Channel c) {
        setDropdownValue(c);
        if (highlightListener != null) {
            highlightListener.highlightChannel(c);
        }
        highlightedChannel = c;
    }

    public Channel getHighlighted() {
        return highlightedChannel;
    }

    @Override
    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
        RoundedBitmapDrawable d = RoundedBitmapDrawableFactory.create(getContext().getResources(), bitmap);
        d.setCircular(true);
        autoCompleteTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(d, null, null, null);
    }

    @Override
    public void onBitmapFailed(Exception e, Drawable errorDrawable) {
    }

    @Override
    public void onPrepareLoad(Drawable placeHolderDrawable) {
        autoCompleteTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_grey_circle_small, 0, 0, 0);
    }

    public void setObservers(@NonNull ChannelsViewModel viewModel, @NonNull LifecycleOwner lifecycleOwner) {
        viewModel.getSims().observe(lifecycleOwner, sims -> Timber.i("Got sims: %s", sims.size()));
        viewModel.getSimHniList().observe(lifecycleOwner, simList -> Timber.i("Got new sim hni list: %s", simList));
        viewModel.getAllChannels().observe(lifecycleOwner, this::channelUpdateIfNull);
        viewModel.getSimChannels().observe(lifecycleOwner, this::channelUpdate);

        //This is to prevent the SAM constructor from being compiled to singleton causing breakages. See
        //https://stackoverflow.com/a/54939860/2371515
        viewModel.getSelectedChannels().observe(lifecycleOwner, new Observer<List<Channel>>() {
            @Override
            public void onChanged(List<Channel> channels) {
                Timber.i("Got new selected channels: %s", channels.size());
            }
        });
        viewModel.getActiveChannel().observe(lifecycleOwner, channel -> {
            if (channel != null && showSelected) setState(initial_helper_text, NONE);
        });
        viewModel.getChannelActions().observe(lifecycleOwner, actions -> setState(actions, viewModel));
    }

    private void setState(List<HoverAction> actions, ChannelsViewModel viewModel) {
        if (viewModel.getActiveChannel().getValue() != null && (actions == null || actions.size() == 0))
            setState(getContext().getString(R.string.no_actions_fielderror, HoverAction.getHumanFriendlyType(getContext(), viewModel.getType())), AbstractStatefulInput.ERROR);
        else if (actions != null && actions.size() == 1 && !actions.get(0).requiresRecipient() && !viewModel.getType().equals(HoverAction.BALANCE))
            setState(getContext().getString(actions.get(0).transaction_type.equals(HoverAction.AIRTIME) ? R.string.self_only_airtime_warning : R.string.self_only_money_warning), INFO);
        else if (viewModel.getActiveChannel().getValue() != null && showSelected)
            setState(initial_helper_text, AbstractStatefulInput.SUCCESS);
    }

    public interface HighlightListener {
        void highlightChannel(Channel c);
    }
}