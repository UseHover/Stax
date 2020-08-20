package com.hover.stax.channels;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hover.sdk.api.Hover;
import com.hover.sdk.sims.SimInfo;
import com.hover.stax.R;
import com.hover.stax.ui.chooseService.pin.ServicesPinActivity;
import com.hover.stax.utils.PermissionUtils;

import java.util.ArrayList;
import java.util.List;

public class ChannelsActivity extends AppCompatActivity implements ChannelsAdapter.SelectListener {
	public final static String TAG = "ChannelsActivity";

	ChannelViewModel channelViewModel;
	List<String> countryList;
	List<String> simHniList;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.choose_channels);
		findViewById(R.id.choose_serves_done).setOnClickListener(view -> startActivity(new Intent(this, ServicesPinActivity.class)));

		channelViewModel = new ViewModelProvider(this).get(ChannelViewModel.class);

		if (!PermissionUtils.hasPhonePerm(this))
			PermissionUtils.requestPhonePerms(this, 0);
		else init();
	}

	private void init() {
		getHnis();
		getCountries();
		addChannels();
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (PermissionUtils.permissionsGranted(grantResults)) {
			Hover.updateSimInfo(this);
			init();
		}
	}

	private void getCountries() {
		channelViewModel.getSims().observe(this, sims -> {
			countryList = new ArrayList<>();
			for (SimInfo sim: sims) {
				if (!countryList.contains(sim.getCountryIso()))
					countryList.add(sim.getCountryIso());
			}
			if (countryList.size() > 0)
				((TextView) findViewById(R.id.other_services_in)).setText(getString(R.string.country_section, countryList.get(0).toUpperCase()));
		});
	}

	private void getHnis() {
		channelViewModel.getSims().observe(this, sims -> {
			simHniList = new ArrayList<>();
			for (SimInfo sim: sims) {
				if (!simHniList.contains(sim.getOSReportedHni()))
					simHniList.add(sim.getOSReportedHni());
			}
		});
	}

	private void addChannels() {
		channelViewModel.getChannels().observe(this, channels -> {
			addGrid(findViewById(R.id.choose_service_recycler_yourSIMS), getSimChannels(channels));
			addGrid(findViewById(R.id.choose_service_recycler_inCountry), getCountryChannels(channels));
			addGrid(findViewById(R.id.choose_service_recycler_allservices), channels);
		});
	}

	private void addGrid(RecyclerView view, List<Channel> channels) {
		GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 4, GridLayoutManager.VERTICAL, false);
		view.setHasFixedSize(true);
		view.setLayoutManager(gridLayoutManager);
		ChannelsAdapter instAdapter = new ChannelsAdapter(channels, this);
		view.setAdapter(instAdapter);
		channelViewModel.getSelected().observe(this, instAdapter::updateSelected);
	}

	// Filtering the main list here is probably not faster than a second DB query in view model
	private List<Channel> getSimChannels(List<Channel> channels) {
		List<Channel> simChannels = new ArrayList<>();
		for (int i = 0; i < channels.size(); i++) {
			String[] hniArr = channels.get(i).hniList.split(",");
			for (int l = 0; l < hniArr.length; l++) {
				if (simHniList.contains(hniArr[l]))
					simChannels.add(channels.get(i));
			}
		}
		return simChannels;
	}
	// Filtering the main list here is probably not faster than a second DB query in view model
	private List<Channel> getCountryChannels(List<Channel> channels) {
		List<Channel> countryChannels = new ArrayList<>();
		for (int i = 0; i < channels.size(); i++) {
			if (countryList.contains(channels.get(i).countryAlpha2.toLowerCase()))
				countryChannels.add(channels.get(i));
		}
		return countryChannels;
	}

	public void onTap(int id) {
		channelViewModel.setSelected(id);
	}
}
