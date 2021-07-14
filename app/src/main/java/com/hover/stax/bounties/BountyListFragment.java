package com.hover.stax.bounties;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.WorkManager;

import com.hover.sdk.actions.HoverAction;
import com.hover.sdk.api.Hover;
import com.hover.stax.R;
import com.hover.stax.channels.Channel;
import com.hover.stax.channels.UpdateChannelsWorker;
import com.hover.stax.countries.CountryAdapter;
import com.hover.stax.databinding.FragmentBountyListBinding;
import com.hover.stax.navigation.NavigationInterface;
import com.hover.stax.utils.UIHelper;
import com.hover.stax.utils.Utils;
import com.hover.stax.views.StaxDialog;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class BountyListFragment extends Fragment implements NavigationInterface, BountyListItem.SelectListener, CountryAdapter.SelectListener {

    private static final String TAG = "BountyListFragment";

    private BountyViewModel bountyViewModel;
    private FragmentBountyListBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Utils.logAnalyticsEvent(getString(R.string.visit_screen, getString(R.string.visit_bounty_list)), requireContext());
        bountyViewModel = new ViewModelProvider(this).get(BountyViewModel.class);

        binding = FragmentBountyListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initCountryDropdown();
        initRecyclerView();
        startObservers();
    }

    @Override
    public void onResume() {
        super.onResume();
        forceUserToBeOnline();
    }

    private void forceUserToBeOnline() {
        if (isAdded() && Utils.isNetworkAvailable(requireActivity())) {
            updateActionConfig();
            updateChannelsWorker();
        } else showOfflineDialog();
    }

    private void updateActionConfig() {
        Hover.updateActionConfigs(new Hover.DownloadListener() {
            @Override
            public void onError(String s) {
                forceUserToBeOnline();
            }

            @Override
            public void onSuccess(ArrayList<HoverAction> arrayList) {

            }
        }, requireContext());
    }

    private void updateChannelsWorker() {
        WorkManager wm = WorkManager.getInstance(requireContext());
        wm.beginUniqueWork(UpdateChannelsWorker.CHANNELS_WORK_ID, ExistingWorkPolicy.REPLACE, UpdateChannelsWorker.makeWork()).enqueue();
        wm.enqueueUniquePeriodicWork(UpdateChannelsWorker.TAG, ExistingPeriodicWorkPolicy.REPLACE, UpdateChannelsWorker.makeToil());
    }

    private void showOfflineDialog() {
        new StaxDialog(requireActivity())
                .setDialogTitle(R.string.internet_required)
                .setDialogMessage(R.string.internet_required_bounty_desc)
                .setPosButton(R.string.try_again, view -> {
                    forceUserToBeOnline();
                })
                .setNegButton(R.string.btn_cancel, view -> {
                    requireActivity().finish();
                })
                .makeSticky()
                .showIt();
    }

    public void initCountryDropdown() {
        binding.bountyCountryDropdown.setListener(this);
    }

    private void initRecyclerView() {
        binding.bountiesRecyclerView.setLayoutManager(UIHelper.setMainLinearManagers(getContext()));
    }

    private void startObservers() {
        bountyViewModel.getActions().observe(getViewLifecycleOwner(), actions -> Log.v(TAG, "actions update: " + actions.size()));
        bountyViewModel.getTransactions().observe(getViewLifecycleOwner(), transactions -> Log.v(TAG, "transactions update: " + transactions.size()));
        bountyViewModel.getSims().observe(getViewLifecycleOwner(), sims -> Log.v(TAG, "sim update: " + sims.size()));
        bountyViewModel.getBounties().observe(getViewLifecycleOwner(), bounties -> updateChannelList(bountyViewModel.getChannels().getValue(), bounties));

        bountyViewModel.getChannels().observe(getViewLifecycleOwner(), channels -> {
            binding.bountyCountryDropdown.updateChoices(channels);
            updateChannelList(channels, bountyViewModel.getBounties().getValue());
        });
    }

    private void updateChannelList(List<Channel> channels, List<Bounty> bounties) {
        if (bounties != null && bounties.size() > 0 && channels != null && channels.size() > 0 && (bountyViewModel.country.equals(CountryAdapter.codeRepresentingAllCountries()) || channels.get(0).countryAlpha2.equals(bountyViewModel.country))) {
            BountyChannelsAdapter adapter = new BountyChannelsAdapter(channels, bounties, this);
            binding.bountiesRecyclerView.setAdapter(adapter);
        }
    }

    @Override
    public void viewTransactionDetail(String uuid) {
        navigateToTransactionDetailsFragment(uuid, this);
    }

    @Override
    public void viewBountyDetail(Bounty b) {
        if (bountyViewModel.isSimPresent(b)) showBountyDescDialog(b);
        else showSimErrorDialog(b);
    }

    void showSimErrorDialog(Bounty b) {
        Timber.e("showing sim error dialog %s", b.action.root_code);
        new StaxDialog(requireActivity())
                .setDialogTitle(getString(R.string.bounty_sim_err_header))
                .setDialogMessage(getString(R.string.bounty_sim_err_desc, b.action.network_name))
                .setNegButton(R.string.btn_cancel, null)
                .setPosButton(R.string.retry, v -> retrySimMatch(b))
                .showIt();
    }

    void showBountyDescDialog(Bounty b) {
        Timber.e("showing dialog %s", b.action);
        new StaxDialog(requireActivity())
                .setDialogTitle(getString(R.string.bounty_claim_title, b.action.root_code, HoverAction.getHumanFriendlyType(getContext(), b.action.transaction_type), b.action.bounty_amount))
                .setDialogMessage(getString(R.string.bounty_claim_explained, b.action.bounty_amount, b.getInstructions(getContext())))
                .setPosButton(R.string.start_USSD_Flow, v -> ((BountyActivity) requireActivity()).makeCall(b.action))
                .showIt();
    }

    void retrySimMatch(Bounty b) {
        bountyViewModel.getSims().removeObservers(getViewLifecycleOwner());
        bountyViewModel.getSims().observe(getViewLifecycleOwner(), sims -> viewBountyDetail(b));
        Hover.updateSimInfo(requireActivity());
    }

    @Override
    public void countrySelect(String countryCode) {
        bountyViewModel.filterChannels(countryCode).observe(getViewLifecycleOwner(), channels ->
                updateChannelList(channels, bountyViewModel.getBounties().getValue()));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        binding = null;
    }
}
