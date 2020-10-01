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

import com.amplitude.api.Amplitude;
import com.hover.sdk.api.Hover;
import com.hover.stax.R;
import com.hover.stax.security.PinsActivity;
import com.hover.stax.utils.PermissionUtils;
import com.hover.stax.utils.UIHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ChannelsActivity extends AppCompatActivity implements ChannelsAdapter.SelectListener {
	public final static String TAG = "ChannelsActivity";

	ChannelListViewModel channelViewModel;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		WorkManager.getInstance(this).beginUniqueWork(UpdateChannelsWorker.CHANNELS_WORK_ID, ExistingWorkPolicy.KEEP, UpdateChannelsWorker.makeWork()).enqueue();
		setContentView(R.layout.choose_channels);
		channelViewModel = new ViewModelProvider(this).get(ChannelListViewModel.class);

		if (!PermissionUtils.hasPhonePerm(this))
			PermissionUtils.requestPhonePerms(this, 0);
		else init();
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (PermissionUtils.permissionsGranted(grantResults))
			init();
	}

	private void init() {
		Hover.updateSimInfo(this);
		addChannels();
		watchSelected();
	}

	private void addChannels() {
		channelViewModel.getSimChannels().observe(this, channels -> {
			if (channels.size() > 0)
				fillSection(findViewById(R.id.sim_section), getString(R.string.sims_section), channels);
			else
				findViewById(R.id.sim_section).setVisibility(View.GONE);
		});

		channelViewModel.getCountryChannels().observe(this, channels -> {
			((LinearLayout) findViewById(R.id.country_wrapper)).removeAllViews();
			if (channels.size() > 0 && channelViewModel.simCountryList.getValue() != null) {
				for (String countryAlpha2 : channelViewModel.simCountryList.getValue()) {
					addCountrySection(getString(R.string.country_section, countryAlpha2.toUpperCase()), getCountryChannels(countryAlpha2, channels));
				}
			}
		});

		channelViewModel.getChannels().observe(this, channels -> {
			if (channels.size() == 0) {
				findViewById(R.id.loading_title).setVisibility(View.VISIBLE);
				findViewById(R.id.section_wrapper).setVisibility(View.GONE);
			} else {
				findViewById(R.id.loading_title).setVisibility(View.GONE);
				findViewById(R.id.section_wrapper).setVisibility(View.VISIBLE);
				fillSection(findViewById(R.id.all_section), getString(R.string.all_section), channels);
			}
		});
	}

	private void fillSection(View section, String title, List<Channel> channels) {
		section.setVisibility(View.VISIBLE);
		((TextView) section.findViewById(R.id.section_title)).setText(title);
		GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 4, GridLayoutManager.VERTICAL, false);
		ChannelsAdapter channelsAdapter = new ChannelsAdapter(channels, this);
		RecyclerView view = section.findViewById(R.id.section_recycler);
		view.setHasFixedSize(true);
		view.setLayoutManager(gridLayoutManager);
		view.setAdapter(channelsAdapter);
		channelViewModel.getSelected().observe(ChannelsActivity.this, channelsAdapter::updateSelected);
	}

	private void addCountrySection(String sectionTitle, List<Channel> channels) {
		if (channels.size() > 0) {
			LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View section = inflater.inflate(R.layout.channel_grid, null);
			fillSection(section, sectionTitle, channels);
			((LinearLayout) findViewById(R.id.country_wrapper)).addView(section);
		}
	}

	private List<Channel> getCountryChannels(String countryAlpha2, List<Channel> channels) {
		List<Channel> countryChannels = new ArrayList<>();
		for (int i = 0; i < channels.size(); i++) {
			if (countryAlpha2.equals(channels.get(i).countryAlpha2.toUpperCase()))
				countryChannels.add(channels.get(i));
		}
		return countryChannels;
	}

	private void watchSelected() {
		channelViewModel.getSelected().observe(this, this::onDone);
	}

	private void onDone(List<Integer> ids) {
		if (ids.size() > 0) {
			findViewById(R.id.choose_serves_done).setOnClickListener(view -> {
				JSONObject event = new JSONObject();
				try {
					event.put(getString(R.string.account_select_count_key), ids.size());
				} catch (JSONException ignored) {
				}
				Amplitude.getInstance().logEvent(getString(R.string.finished_account_select), event);
				saveAndContinue();
			});
		} else {
			findViewById(R.id.choose_serves_done).setOnClickListener(view -> UIHelper.flashMessage(ChannelsActivity.this, getString(R.string.no_selection_error)));
		}
	}

	public void onTap(int id) {
		channelViewModel.setSelected(id);
	}

	private void saveAndContinue() {
		channelViewModel.saveSelected();
		startActivityForResult(new Intent(ChannelsActivity.this, PinsActivity.class), 0);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		setResult(RESULT_OK, addReturnData(new Intent()));
		finish();
	}

	private Intent addReturnData(Intent i) {
		if (channelViewModel.getSelected().getValue() != null) {
			Bundle bundle = new Bundle();
			bundle.putIntegerArrayList("selected", new ArrayList<>(channelViewModel.getSelected().getValue()));
			i.putExtra("selected", bundle);
		}
		return i;
	}
}
