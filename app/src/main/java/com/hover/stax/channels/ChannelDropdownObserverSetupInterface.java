package com.hover.stax.channels;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LifecycleOwner;

public interface ChannelDropdownObserverSetupInterface {
	 String TAG = "ChannelSetupInterface";
	default void setupChannelDropdownObservers(ChannelDropdownViewModel viewModel, ChannelDropdown dropdown, LifecycleOwner lifecycleOwner, Context ctx) {
		viewModel.getSims().observe(lifecycleOwner, sims -> Log.i(TAG, "Got sims: " + sims.size()));
		viewModel.getSimHniList().observe(lifecycleOwner, simList -> Log.i(TAG, "Got new sim hni list: " + simList));
		viewModel.getSimChannels().observe(lifecycleOwner, dropdown::updateChannels);
		viewModel.getChannels().observe(lifecycleOwner, dropdown::updateChannels);
		viewModel.getSimChannels().observe(lifecycleOwner, dropdown::updateChannels);
		viewModel.getSelectedChannels().observe(lifecycleOwner, channels -> {
			if (channels != null && channels.size() > 0) dropdown.setError(null);
		});
		viewModel.getError().observe(lifecycleOwner, dropdown::setError);
		viewModel.getHelper().observe(lifecycleOwner, helper -> dropdown.setHelper(helper != null ?  ctx.getString(helper) : null));

	}
	default void setupActionDropdownObservers(ChannelDropdownViewModel viewModel, LifecycleOwner lifecycleOwner)  {
		viewModel.getActiveChannel().observe(lifecycleOwner, channel -> Log.i(TAG, "Got new active channel: " + channel + " " + channel.countryAlpha2));
		viewModel.getChannelActions().observe(lifecycleOwner, actions -> Log.i(TAG, "Got new actions: " + actions.size()));

	}
}
