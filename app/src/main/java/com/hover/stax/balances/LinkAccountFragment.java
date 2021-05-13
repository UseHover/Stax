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
import com.hover.stax.databinding.FragmentLinkAccountBinding;
import com.hover.stax.utils.Utils;

public class LinkAccountFragment extends Fragment {
    final public static String TAG = "LinkAccountFragment";

    private ChannelDropdownViewModel channelDropdownViewModel;
    private BalancesViewModel balancesViewModel;
    private ChannelDropdown channelDropdown;

    private FragmentLinkAccountBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Utils.logAnalyticsEvent(getString(R.string.visit_screen, getString(R.string.visit_link_account)), requireContext());
        channelDropdownViewModel = new ViewModelProvider(requireActivity()).get(ChannelDropdownViewModel.class);
        balancesViewModel = new ViewModelProvider(requireActivity()).get(BalancesViewModel.class);

        binding = FragmentLinkAccountBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUpChannelDropdown();
        setUpCancelAndLinkAccountBtn();
    }

    private void setUpChannelDropdown() {
        channelDropdown = binding.channelDropdown;
        channelDropdown.setObservers(channelDropdownViewModel, requireActivity());
    }

    private void setUpCancelAndLinkAccountBtn() {
        binding.negBtn.setOnClickListener(v -> requireActivity().onBackPressed());
        binding.posBtn.setOnClickListener(this::linkAccount);
    }

    public void linkAccount(View v) {
        if (channelDropdown.getHighlighted() != null) {
            channelDropdownViewModel.setChannelSelected(channelDropdown.getHighlighted());
            requireActivity().onBackPressed();
            balancesViewModel.getActions().observe(getViewLifecycleOwner(), actions -> balancesViewModel.setRunning(channelDropdown.getHighlighted().id));
        } else channelDropdown.setError(getString(R.string.refresh_balance_error));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        binding = null;
    }
}
