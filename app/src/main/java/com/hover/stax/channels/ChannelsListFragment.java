package com.hover.stax.channels;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.hover.stax.R;
import com.hover.stax.balances.BalanceAdapter;
import com.hover.stax.balances.BalancesViewModel;
import com.hover.stax.databinding.FragmentChannelsListBinding;
import com.hover.stax.home.MainActivity;
import com.hover.stax.utils.UIHelper;
import com.hover.stax.utils.Utils;
import com.hover.stax.views.StaxDialog;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class ChannelsListFragment extends Fragment implements ChannelsRecyclerViewAdapter.SelectListener {
    final public static String TAG = "ChannelListFragment";
    static final public String FORCE_RETURN_DATA = "force_return_data";
    private boolean IS_FORCE_RETURN = true;

    private ChannelsViewModel channelsViewModel;
    private BalancesViewModel balancesViewModel;

    private FragmentChannelsListBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Utils.logAnalyticsEvent(getString(R.string.visit_screen, getString(R.string.visit_link_account)), requireContext());
        channelsViewModel = new ViewModelProvider(requireActivity()).get(ChannelsViewModel.class);
        balancesViewModel = new ViewModelProvider(requireActivity()).get(BalancesViewModel.class);
        initArguments();

        binding = FragmentChannelsListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
    private void initArguments() {
        if (getArguments() != null) {
            IS_FORCE_RETURN = getArguments().getBoolean(FORCE_RETURN_DATA, true);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupSelectedChannels();
        setupSimSupportedChannels();
    }

    private void setupSelectedChannels() {
        RecyclerView selectedChannelsListView = binding.selectedChannelsRecyclerView;
        selectedChannelsListView.setLayoutManager(UIHelper.setMainLinearManagers(requireContext()));
        channelsViewModel.getSelectedChannels().observe(getViewLifecycleOwner(), channels -> {
            if(channels!=null && channels.size() > 0) {
                updateCardVisibilities(true);
                selectedChannelsListView.setAdapter(new ChannelsRecyclerViewAdapter(channels, this));
            }
            else {
                updateCardVisibilities(false);
            }
        });
    }

    private void updateCardVisibilities(boolean visible) {
        binding.selectedChannelsCard.setVisibility(visible ? VISIBLE : GONE);
        binding.simSupportedChannelsCard.setBackButtonVisibility(visible ? GONE : VISIBLE);
    }

    private void setupSimSupportedChannels() {
        RecyclerView simSupportedChannelsListView = binding.simSupportedChannelsRecyclerView;
        simSupportedChannelsListView.setLayoutManager(UIHelper.setMainLinearManagers(requireContext()));
        channelsViewModel.getSimChannels().observe(getViewLifecycleOwner(), channels -> {
            if(channels!=null) {
                if(channels.size() > 0) simSupportedChannelsListView.setAdapter(new ChannelsRecyclerViewAdapter(Channel.sort(channels, false), this));
                else showEmptySimChannelsDialog();
            }
        });
    }

    private void showEmptySimChannelsDialog() {
        new StaxDialog(requireActivity())
                .setDialogTitle(R.string.no_connecion)
                .setDialogMessage(R.string.empty_channels_internet_err)
                .setPosButton(R.string.btn_ok, view -> requireActivity().onBackPressed())
                .showIt();
    }
    private void showCheckBalanceDialog(Channel channel) {
        new StaxDialog(requireActivity())
                .setDialogTitle(R.string.check_balance_title)
                .setDialogMessage(R.string.check_balance_desc)
                .setNegButton(R.string.later, view -> saveChannel(channel, false))
                .setPosButton(R.string.check_balance_title, view -> saveChannel(channel, true))
                .showIt();
    }
    private void saveChannel(Channel channel, boolean checkBalance) {
            channelsViewModel.setChannelSelected(channel);
            requireActivity().onBackPressed();
            if(balancesViewModel !=null && checkBalance) balancesViewModel.getActions().observe(getViewLifecycleOwner(), actions -> balancesViewModel.setRunning(channel.id));
    }
    private void goToChannelsDetailsScreen(Channel channel) {
        BalanceAdapter.BalanceListener balanceListener = (MainActivity) getActivity();
        if(balanceListener!=null) {
            balanceListener.onTapDetail(channel.id);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void clickedChannel(Channel channel) {
        if(IS_FORCE_RETURN || !channel.selected) showCheckBalanceDialog(channel);
        else  goToChannelsDetailsScreen(channel);
    }
}
