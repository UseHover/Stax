package com.hover.stax.balances;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.RecyclerView;

import com.amplitude.api.Amplitude;
import com.hover.stax.R;
import com.hover.stax.channels.Channel;
import com.hover.stax.channels.ChannelDropdown;
import com.hover.stax.channels.ChannelDropdownViewModel;
import com.hover.stax.databinding.FragmentBalanceBinding;
import com.hover.stax.home.MainActivity;
import com.hover.stax.navigation.NavigationInterface;
import com.hover.stax.requests.Request;
import com.hover.stax.schedules.Schedule;
import com.hover.stax.transactions.TransactionHistoryAdapter;
import com.hover.stax.transactions.TransactionHistoryViewModel;
import com.hover.stax.utils.UIHelper;
import com.hover.stax.views.StaxCardView;

import java.util.List;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class BalancesFragment extends Fragment implements TransactionHistoryAdapter.SelectListener,
        ScheduledAdapter.SelectListener,
        RequestsAdapter.SelectListener,
        NavigationInterface {
    final public static String TAG = "BalanceFragment";

    private BalancesViewModel balancesViewModel;
    private FutureViewModel futureViewModel;
    private TransactionHistoryViewModel transactionsViewModel;
    private BalanceAdapter balanceAdapter;
    private ChannelDropdownViewModel channelDropdownViewModel;

    private TextView addChannelLink;
    private ChannelDropdown channelDropdown;
    private boolean balancesVisible = false;

    private RecyclerView balancesRecyclerView;

    private FragmentBalanceBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Amplitude.getInstance().logEvent(getString(R.string.visit_screen, getString(R.string.visit_balance_and_history)));
        balancesViewModel = new ViewModelProvider(requireActivity()).get(BalancesViewModel.class);
        channelDropdownViewModel = new ViewModelProvider(requireActivity()).get(ChannelDropdownViewModel.class);

        futureViewModel = new ViewModelProvider(requireActivity()).get(FutureViewModel.class);
        transactionsViewModel = new ViewModelProvider(requireActivity()).get(TransactionHistoryViewModel.class);

        binding = FragmentBalanceBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setUpBalances();
        setUpLinkNewAccount();
        setUpFuture();
        setUpHistory();
        binding.homeCardBalances.refreshAccountsBtn.setOnClickListener(this::refreshBalances);
    }

    @Override
    public void onStart() {
        super.onStart();

        ((MainActivity) getActivity()).getSupportActionBar().setTitle(null);
    }

    private void setUpBalances() {
        initBalanceCard();
        balancesViewModel.getSelectedChannels().observe(getViewLifecycleOwner(), this::updateServices);
    }

    private void setUpLinkNewAccount() {
        addChannelLink = binding.homeCardBalances.newAccountLink;
        addChannelLink.setOnClickListener(v -> navigateToLinkAccountFragment(NavHostFragment.findNavController(this)));
        channelDropdown = binding.homeCardBalances.channelDropdown;
    }

    private void initBalanceCard() {
        StaxCardView balanceCard = binding.homeCardBalances.balanceCard;
        balanceCard.setIcon(balancesVisible ? R.drawable.ic_visibility_on : R.drawable.ic_visibility_off);
        balanceCard.setOnClickIcon(v -> {
            balancesVisible = !balancesVisible;
            balanceCard.setIcon(balancesVisible ? R.drawable.ic_visibility_on : R.drawable.ic_visibility_off);
            balanceAdapter.showBalance(balancesVisible);
        });

        balancesRecyclerView = binding.homeCardBalances.balancesRecyclerView;
        balancesRecyclerView.setLayoutManager(UIHelper.setMainLinearManagers(getContext()));
        balancesRecyclerView.setHasFixedSize(true);
    }

    private void updateServices(List<Channel> channels) {
        balanceAdapter = new BalanceAdapter(channels, (MainActivity) getActivity());
        balancesRecyclerView.setAdapter(balanceAdapter);
        balancesRecyclerView.setVisibility(channels != null && channels.size() > 0 ? VISIBLE : GONE);

        binding.homeCardBalances.balanceCard.setBackButtonVisibility(channels != null && channels.size() > 0 ? VISIBLE : GONE);

        toggleLink(channels != null && channels.size() > 0);
        channelDropdown.setObservers(channelDropdownViewModel, getActivity());
    }

    public void toggleLink(boolean show) {
        addChannelLink.setVisibility(show ? VISIBLE : GONE);
        channelDropdown.setVisibility(show ? GONE : VISIBLE);
    }

    private void refreshBalances(View v) {
        if (channelDropdown.getHighlighted() != null) {
            balancesViewModel.getActions().observe(getViewLifecycleOwner(), actions -> {
                balancesViewModel.setAllRunning(v.getContext());
            });
            channelDropdownViewModel.setChannelSelected(channelDropdown.getHighlighted());

        } else if (channelDropdownViewModel.getSelectedChannels().getValue() == null || channelDropdownViewModel.getSelectedChannels().getValue().size() == 0)
            channelDropdown.setError(getString(R.string.refresh_balance_error));
        else
            balancesViewModel.setAllRunning(v.getContext());
    }

    private void setUpFuture() {
        futureViewModel.getScheduled().observe(getViewLifecycleOwner(), schedules -> {
            RecyclerView recyclerView = binding.scheduledCard.scheduledRecyclerView;
            recyclerView.setLayoutManager(UIHelper.setMainLinearManagers(getContext()));
            recyclerView.setAdapter(new ScheduledAdapter(schedules, this));
            setFutureVisible(schedules, futureViewModel.getRequests().getValue());
        });

        futureViewModel.getRequests().observe(getViewLifecycleOwner(), requests -> {
            RecyclerView rv = binding.scheduledCard.requestsRecyclerView;
            rv.setLayoutManager(UIHelper.setMainLinearManagers(getContext()));
            rv.setAdapter(new RequestsAdapter(requests, this));
            setFutureVisible(futureViewModel.getScheduled().getValue(), requests);
        });
    }

    private void setFutureVisible(List<Schedule> schedules, List<Request> requests) {
        boolean visible = (schedules != null && schedules.size() > 0) || (requests != null && requests.size() > 0);
        binding.scheduledCard.getRoot().setVisibility(visible ? VISIBLE : GONE);
    }

    private void setUpHistory() {
        RecyclerView rv = binding.homeCardTransactions.transactionHistoryRecyclerView;
        rv.setLayoutManager(UIHelper.setMainLinearManagers(getContext()));

        transactionsViewModel.getStaxTransactions().observe(getViewLifecycleOwner(), staxTransactions -> {
            rv.setAdapter(new TransactionHistoryAdapter(staxTransactions, BalancesFragment.this));
            binding.homeCardTransactions.noHistory.setVisibility(staxTransactions.size() > 0 ? GONE : VISIBLE);
        });
    }

    @Override
    public void viewTransactionDetail(String uuid) {
        navigateToTransactionDetailsFragment(uuid, this);
    }

    @Override
    public void viewScheduledDetail(int id) {
        navigateToScheduleDetailsFragment(id, this);
    }

    @Override
    public void viewRequestDetail(int id) {
        navigateToRequestDetailsFragment(id, this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
