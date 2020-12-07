package com.hover.stax.channels;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.amplitude.api.Amplitude;
import com.hover.stax.R;
import com.hover.stax.database.Constants;
import com.hover.stax.requestAccount.RequestAccountActivity;
import com.hover.stax.utils.UIHelper;

import java.util.ArrayList;
import java.util.List;

public class ChannelListFragment extends Fragment implements ChannelsAdapter.SelectListener {
	private ChannelListViewModel channelViewModel;

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		channelViewModel = new ViewModelProvider(requireActivity()).get(ChannelListViewModel.class);
		Amplitude.getInstance().logEvent(getString(R.string.visit_screen, getString(R.string.visit_choose_channels)));
		return inflater.inflate(R.layout.fragment_channels, container, false);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		setupChannelObservers(view);
		view.findViewById(R.id.request_accounts_btn).setOnClickListener(v -> {
			startActivity(new Intent(getActivity(), RequestAccountActivity.class));
		});
	}

	private void setupChannelObservers(View view) {
		observeToSetupChannelViews(view);
		observeToSetupCountryChannels(view);
		observeToSetupAllChannels(view);
	}
	private void observeToSetupChannelViews(View view) {
		channelViewModel.getSimChannels().observe(getViewLifecycleOwner(), channels -> {
			if (channels.size() > 0) {
				view.findViewById(R.id.sim_card).setVisibility(View.VISIBLE);
				view.findViewById(R.id.all_card).setVisibility(View.GONE);
				fillSection(view.findViewById(R.id.sim_card), getString(R.string.simaccts_cardhead), channels);
			} else {
				view.findViewById(R.id.sim_card).setVisibility(View.GONE);
				view.findViewById(R.id.all_card).setVisibility(View.VISIBLE);
			}
		});
	}
	private void observeToSetupCountryChannels(View view) {
		channelViewModel.getCountryChannels().observe(getViewLifecycleOwner(), channels -> {
			((LinearLayout) view.findViewById(R.id.country_wrapper)).removeAllViews();
			if (channels.size() > 0 && channelViewModel.simCountryList.getValue() != null) {
				for (String countryAlpha2 : channelViewModel.simCountryList.getValue()) {
					List<Channel> channelsAvailableInACountryByAlpha = getCountryChannels(countryAlpha2, channels);
					addCountrySection(view, getString(R.string.countryaccts_cardhead, countryAlpha2.toUpperCase()), channelsAvailableInACountryByAlpha);
				}
			}
		});
	}
	private void observeToSetupAllChannels(View view) {
		channelViewModel.getChannels().observe(getViewLifecycleOwner(), channels -> {
			view.findViewById(R.id.no_accounts).setVisibility(channels == null || channels.size() == 0 ? View.VISIBLE : View.GONE);
			fillSection(view.findViewById(R.id.all_card), getString(R.string.allaccts_cardhead), channels);
		});
	}


	private void fillSection(View card, String title, List<Channel> channels) {
		((TextView) card.findViewById(R.id.title)).setText(title);
		GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 4, GridLayoutManager.VERTICAL, false);
		ChannelsAdapter channelsAdapter = new ChannelsAdapter(channels, this);
		RecyclerView view = card.findViewById(R.id.section_recycler);
		view.setHasFixedSize(true);
		view.setLayoutManager(gridLayoutManager);
		view.setAdapter(channelsAdapter);
		channelViewModel.getSelected().observe(getViewLifecycleOwner(), channelsAdapter::updateSelected);
	}

	private void addCountrySection(View root, String sectionTitle, List<Channel> channels) {
		if (channels.size() > 0) {
			LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View section = inflater.inflate(R.layout.channel_card_grid, null);
			fillSection(section, sectionTitle, channels);
			((LinearLayout) root.findViewById(R.id.country_wrapper)).addView(section);
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
	public void onTap(int id) {
		channelViewModel.setSelected(id);
	}
}
