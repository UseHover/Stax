package com.hover.stax.library;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.amplitude.api.Amplitude;
import com.hover.stax.R;
import com.hover.stax.channels.Channel;
import com.hover.stax.countries.CountryAdapter;
import com.hover.stax.databinding.ActivityLibraryBinding;
import com.hover.stax.navigation.AbstractNavigationActivity;
import com.hover.stax.utils.UIHelper;

import java.util.List;

public class LibraryActivity extends AbstractNavigationActivity implements LibraryListItem.DialListener, CountryAdapter.SelectListener {
	private static final String TAG = "LibraryActivity";

	public LibraryViewModel viewModel;

	private ActivityLibraryBinding binding;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Amplitude.getInstance().logEvent(getString(R.string.visit_screen, TAG));

		viewModel = new ViewModelProvider(this).get(LibraryViewModel.class);
		binding = ActivityLibraryBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());
		binding.countryDropdown.setListener(this);
		binding.shortcodes.setLayoutManager(UIHelper.setMainLinearManagers(this));

		setUpNav();
		viewModel.getAllChannels().observe(this, channels -> {
			if (channels == null) return;
			binding.countryDropdown.updateChoices(channels);
			viewModel.filterChannels(CountryAdapter.codeRepresentingAllCountries());
		});
		viewModel.getFilteredChannels().observe(this, this::updateList);
	}

	private void updateList(List<Channel> channels) {
		if (channels != null && channels.size() > 0) {
			ChannelsAdapter adapter = new ChannelsAdapter(channels, this);
			binding.shortcodes.setAdapter(adapter);
		}
	}

	@Override
	public void dial(String shortCode) {
		Intent i = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + shortCode.replaceAll("#", Uri.encode("#"))));
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(i);
	}

	@Override
	public void countrySelect(String countryCode) {
		viewModel.filterChannels(countryCode);
	}
}
