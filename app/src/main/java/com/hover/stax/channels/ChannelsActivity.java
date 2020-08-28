package com.hover.stax.channels;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.ExistingWorkPolicy;
import androidx.work.WorkManager;

import com.hover.sdk.api.Hover;
import com.hover.sdk.sims.SimInfo;
import com.hover.stax.R;
import com.hover.stax.pins.PinsActivity;
import com.hover.stax.utils.PermissionUtils;
import com.hover.stax.utils.UIHelper;

import java.util.ArrayList;
import java.util.List;

public class ChannelsActivity extends AppCompatActivity implements ChannelsAdapter.SelectListener {
	public final static String TAG = "ChannelsActivity";

	ChannelViewModel channelViewModel;
	List<String> simCountryList;
	List<String> simHniList;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		WorkManager.getInstance(this).beginUniqueWork(UpdateChannelsWorker.CHANNELS_WORK_ID, ExistingWorkPolicy.KEEP, UpdateChannelsWorker.makeWork()).enqueue();
		setContentView(R.layout.choose_channels);
		channelViewModel = new ViewModelProvider(this).get(ChannelViewModel.class);

		if (!PermissionUtils.hasPhonePerm(this))
			PermissionUtils.requestPhonePerms(this, 0);
		else init();
	}

	private void init() {
		getHnis();
		getCountries();
		addChannels();
		watchSelected();
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
			simCountryList = new ArrayList<>();
			for (SimInfo sim: sims) {
				if (!simCountryList.contains(sim.getCountryIso().toUpperCase()))
					simCountryList.add(sim.getCountryIso().toUpperCase());
			}
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
			((LinearLayout) findViewById(R.id.section_wrapper)).removeAllViews();
			//YourSIM
			addSection(getString(R.string.sims_section), getSimChannels(channels));
			//COUNTRIES
			for (String countryAlpha2: simCountryList){
				addSection(getString(R.string.country_section, countryAlpha2.toUpperCase()), getCountryChannels(countryAlpha2, channels));
			}
			//ALL SERVICES
			addSection(getString(R.string.all_section), channels);
		});
	}

	private void addSection(String sectionTitle, List<Channel> channels) {
		LayoutInflater inflater = (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View section = inflater.inflate(R.layout.channel_grid, null);
		((TextView) section.findViewById(R.id.section_title)).setText(sectionTitle);
		GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 4, GridLayoutManager.VERTICAL, false);
		ChannelsAdapter instAdapter = new ChannelsAdapter(channels, this);

		RecyclerView view = section.findViewById(R.id.section_recycler);
		view.setHasFixedSize(true);
		view.setLayoutManager(gridLayoutManager);
		view.setAdapter(instAdapter);
		((LinearLayout) findViewById(R.id.section_wrapper)).addView(section);
		channelViewModel.getPendingSelected().observe(this, instAdapter::updateSelected);
	}

	private void watchSelected() {
		channelViewModel.getPendingSelected().observe(this, this::onDone);
	}

	private void onDone(List<Integer> ids) {
		if (ids.size() > 0) {
			findViewById(R.id.choose_serves_done).setOnClickListener(view -> saveAndContinue());
		} else {
			findViewById(R.id.choose_serves_done).setOnClickListener(view -> UIHelper.flashMessage(ChannelsActivity.this, getString(R.string.no_selection_error)));
		}
	}

	private void saveAndContinue() {
		channelViewModel.saveSelected();
		startActivity(new Intent(ChannelsActivity.this, PinsActivity.class));
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
	private List<Channel> getCountryChannels(String countryAlpha2, List<Channel> channels) {
		List<Channel> countryChannels = new ArrayList<>();
		for (int i = 0; i < channels.size(); i++) {
			if (countryAlpha2.equals(channels.get(i).countryAlpha2.toUpperCase()))
				countryChannels.add(channels.get(i));
		}
		return countryChannels;
	}

	public void onTap(int id) {
		channelViewModel.setSelected(id);
	}
}
