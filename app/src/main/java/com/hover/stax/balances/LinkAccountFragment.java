package com.hover.stax.balances;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.amplitude.api.Amplitude;
import com.hover.stax.R;
import com.hover.stax.channels.ChannelDropdown;
import com.hover.stax.channels.ChannelDropdownViewModel;

public class LinkAccountFragment extends Fragment{
	final public static String TAG = "LinkAccountFragment";

	private ChannelDropdownViewModel channelDropdownViewModel;
	private BalancesViewModel balancesViewModel;
	private ChannelDropdown channelDropdown;

	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Amplitude.getInstance().logEvent(getString(R.string.visit_screen, getString(R.string.visit_link_account)));
		channelDropdownViewModel = new ViewModelProvider(requireActivity()).get(ChannelDropdownViewModel.class);
		balancesViewModel = new ViewModelProvider(requireActivity()).get(BalancesViewModel.class);
		return inflater.inflate(R.layout.fragment_link_account, container, false);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		setUpChannelDropdown(view);
		setUpCancelAndLinkAccountBtn(view);
	}

	private void setUpChannelDropdown(View view) {
		channelDropdown = view.findViewById(R.id.channel_dropdown);
		channelDropdown.setObservers(channelDropdownViewModel, channelDropdown, getViewLifecycleOwner()); }

	private void setUpCancelAndLinkAccountBtn(View view) {
		view.findViewById(R.id.neg_btn).setOnClickListener(v->getActivity().onBackPressed());
		view.findViewById(R.id.pos_btn).setOnClickListener(this::linkAccount);
	}

	public void linkAccount(View v) {
		if (channelDropdown.getHighlighted() != null) {
			channelDropdownViewModel.setChannelSelected(channelDropdown.getHighlighted());
			if(getActivity() !=null) getActivity().onBackPressed();
			balancesViewModel.getActions().observe(getViewLifecycleOwner(), actions ->  balancesViewModel.setAllRunning(v.getContext()));
		} else channelDropdown.setError(getString(R.string.refresh_balance_error));
	}

}
