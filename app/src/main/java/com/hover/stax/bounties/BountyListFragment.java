package com.hover.stax.bounties;

import static org.koin.java.KoinJavaComponent.get;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
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
import com.hover.stax.transactions.UpdateBountyTransactionsWorker;
import com.hover.stax.utils.UIHelper;
import com.hover.stax.utils.Utils;
import com.hover.stax.utils.network.NetworkMonitor;
import com.hover.stax.views.AbstractStatefulInput;
import com.hover.stax.views.StaxDialog;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class BountyListFragment extends Fragment implements NavigationInterface, BountyListItem.SelectListener, CountryAdapter.SelectListener {

    private NetworkMonitor networkMonitor;

    private BountyViewModel bountyViewModel;
    private FragmentBountyListBinding binding;

    private StaxDialog dialog;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Utils.logAnalyticsEvent(getString(R.string.visit_screen, getString(R.string.visit_bounty_list)), requireContext());
        bountyViewModel = new ViewModelProvider(this).get(BountyViewModel.class);
        networkMonitor = new NetworkMonitor(requireContext());
        binding = FragmentBountyListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initCountryDropdown();
        initRecyclerView();
        startObservers();
        handleBackPress();
    }

    @Override
    public void onResume() {
        super.onResume();
        forceUserToBeOnline();
    }

    private void forceUserToBeOnline() {
        if (isAdded() && networkMonitor.isNetworkConnected()) {
            updateActionConfig();
            updateChannelsWorker();
            updateBountyTransactionWorker();
        } else showOfflineDialog();
    }

    private void updateActionConfig() {
        Hover.updateActionConfigs(new Hover.DownloadListener() {
            @Override
            public void onError(String s) {
                Utils.logErrorAndReportToFirebase(BountyListFragment.class.getSimpleName(), "Failed to update action configs: " + s, null);
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
    private void updateBountyTransactionWorker() {
        WorkManager wm = WorkManager.getInstance(requireContext());
        wm.beginUniqueWork(UpdateBountyTransactionsWorker.Companion.getBOUNTY_TRANSACTION_WORK_ID(), ExistingWorkPolicy.REPLACE, UpdateBountyTransactionsWorker.Companion.makeWork()).enqueue();
        wm.enqueueUniquePeriodicWork(UpdateBountyTransactionsWorker.Companion.getTAG(), ExistingPeriodicWorkPolicy.REPLACE, UpdateBountyTransactionsWorker.Companion.makeToil());
    }

    private void showOfflineDialog() {
        dialog = new StaxDialog(requireActivity())
                .setDialogTitle(R.string.internet_required)
                .setDialogMessage(R.string.internet_required_bounty_desc)
                .setPosButton(R.string.try_again, view -> forceUserToBeOnline())
                .setNegButton(R.string.btn_cancel, view -> requireActivity().finish())
                .makeSticky();
        dialog.showIt();
    }

    public void initCountryDropdown() {
        binding.bountyCountryDropdown.setListener(this);
    }

    private void initRecyclerView() {
        binding.bountiesRecyclerView.setLayoutManager(UIHelper.setMainLinearManagers(getContext()));
    }

    private void startObservers() {
        bountyViewModel.getActions().observe(getViewLifecycleOwner(), actions -> Timber.v("actions update: %s", actions.size()));
        bountyViewModel.getTransactions().observe(getViewLifecycleOwner(), transactions -> Timber.v("transactions update: %s", transactions.size()));
        bountyViewModel.getSims().observe(getViewLifecycleOwner(), sims -> Timber.v("sim update: %s", sims.size()));
        bountyViewModel.getBounties().observe(getViewLifecycleOwner(), bounties -> updateChannelList(bountyViewModel.getChannels().getValue(), bounties));

        bountyViewModel.getChannels().observe(getViewLifecycleOwner(), channels -> {
            binding.bountyCountryDropdown.updateChoices(channels);
            updateChannelList(channels, bountyViewModel.getBounties().getValue());
        });
    }

    private void updateChannelList(List<Channel> channels, List<Bounty> bounties) {
        if (bounties != null && !bounties.isEmpty() && channels != null && !channels.isEmpty()
                && (bountyViewModel.country.equals(CountryAdapter.codeRepresentingAllCountries())
                || channels.get(0).countryAlpha2.equals(bountyViewModel.country))) {
            BountyChannelsAdapter adapter = new BountyChannelsAdapter(channels, bounties, this);
            binding.bountiesRecyclerView.setAdapter(adapter);
            hideLoadingState();
        }
    }

    @Override
    public void viewTransactionDetail(String uuid) {
        navigateToTransactionDetailsFragment(uuid, getChildFragmentManager(), true);
    }

    @Override
    public void viewBountyDetail(Bounty b) {
        if (bountyViewModel.isSimPresent(b)) showBountyDescDialog(b);
        else showSimErrorDialog(b);
    }

    void showSimErrorDialog(Bounty b) {
        dialog = new StaxDialog(requireActivity())
                .setDialogTitle(getString(R.string.bounty_sim_err_header))
                .setDialogMessage(getString(R.string.bounty_sim_err_desc, b.action.network_name))
                .setNegButton(R.string.btn_cancel, null)
                .setPosButton(R.string.retry, v -> retrySimMatch(b));
        dialog.showIt();
    }

    void showBountyDescDialog(Bounty b) {
        dialog = new StaxDialog(requireActivity())
                .setDialogTitle(getString(R.string.bounty_claim_title, b.action.root_code, HoverAction.getHumanFriendlyType(requireContext(), b.action.transaction_type), b.action.bounty_amount))
                .setDialogMessage(getString(R.string.bounty_claim_explained, b.action.bounty_amount, b.getInstructions(getContext())))
                .setPosButton(R.string.start_USSD_Flow, v -> startBounty(b));
        dialog.showIt();
    }

    private void startBounty(Bounty b) {
        Utils.setFirebaseMessagingTopic("BOUNTY" + b.action.root_code);
        ((BountyActivity) requireActivity()).makeCall(b.action);
    }

    void retrySimMatch(Bounty b) {
        bountyViewModel.getSims().removeObservers(getViewLifecycleOwner());
        bountyViewModel.getSims().observe(getViewLifecycleOwner(), sims -> viewBountyDetail(b));
        Hover.updateSimInfo(requireActivity());
    }

    @Override
    public void countrySelect(String countryCode) {
        showLoadingState();

        bountyViewModel.filterChannels(countryCode).observe(getViewLifecycleOwner(), channels ->
                updateChannelList(channels, bountyViewModel.getBounties().getValue()));
    }

    private void showLoadingState() {
        binding.bountyCountryDropdown.setState(getString(R.string.filtering_in_progress), AbstractStatefulInput.INFO);
        binding.bountiesRecyclerView.setVisibility(View.GONE);
    }

    private void hideLoadingState() {
        binding.bountyCountryDropdown.setState(null, AbstractStatefulInput.NONE);
        binding.bountiesRecyclerView.setVisibility(View.VISIBLE);
    }

    private void handleBackPress() {
        getActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                } else
                    getActivity().onBackPressed();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (dialog != null && dialog.isShowing()) dialog.dismiss();
        binding = null;
    }
}