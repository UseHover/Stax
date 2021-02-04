package com.hover.stax.balances;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.RecyclerView;

import com.amplitude.api.Amplitude;
import com.hover.sdk.transactions.TransactionContract;
import com.hover.stax.R;
import com.hover.stax.channels.Channel;
import com.hover.stax.channels.ChannelDropdown;
import com.hover.stax.channels.ChannelDropdownViewModel;
import com.hover.stax.home.MainActivity;
import com.hover.stax.requests.Request;
import com.hover.stax.schedules.Schedule;
import com.hover.stax.transactions.TransactionHistoryAdapter;
import com.hover.stax.transactions.TransactionHistoryViewModel;
import com.hover.stax.utils.UIHelper;
import com.hover.stax.views.StaxCardView;

import java.util.List;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class BalancesFragment extends Fragment implements TransactionHistoryAdapter.SelectListener, ScheduledAdapter.SelectListener, RequestsAdapter.SelectListener {
	final public static String TAG = "BalanceFragment";

	private BalancesViewModel balancesViewModel;
	private FutureViewModel futureViewModel;
	private TransactionHistoryViewModel transactionsViewModel;
	private BalanceAdapter balanceAdapter;
	private ChannelDropdownViewModel channelDropdownViewModel;
	private ChannelDropdown channelDropdown;

	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Amplitude.getInstance().logEvent(getString(R.string.visit_screen, getString(R.string.visit_balance_and_history)));
		balancesViewModel = new ViewModelProvider(requireActivity()).get(BalancesViewModel.class);
		channelDropdownViewModel = new ViewModelProvider(requireActivity()).get(ChannelDropdownViewModel.class);

		futureViewModel = new ViewModelProvider(requireActivity()).get(FutureViewModel.class);
		transactionsViewModel = new ViewModelProvider(requireActivity()).get(TransactionHistoryViewModel.class);
		return inflater.inflate(R.layout.fragment_balance, container, false);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		channelDropdown = view.findViewById(R.id.channel_dropdown);

		setUpBalances(view);
		setUpChannelDropdown();
		setUpFuture(view);
		setUpHistory(view);
		view.findViewById(R.id.refresh_accounts_btn).setOnClickListener(this::refreshBalances);
	}

	private void setUpBalances(View view) {
		initBalanceCard(view);
		balancesViewModel.getSelectedChannels().observe(getViewLifecycleOwner(), channels -> updateServices(channels, view));
	}

	private void initBalanceCard(View view) {
		StaxCardView balanceCard = view.findViewById(R.id.balance_card);
		balanceCard.backButton.setVisibility(GONE);
		balanceCard.setActivated(false);
		balanceCard.backButton.setOnClickListener(v -> {
			balanceCard.backButton.setActivated(!balanceCard.backButton.isActivated());
			balanceAdapter.updateShowBalance();
		});

		RecyclerView recyclerView = view.findViewById(R.id.balances_recyclerView);
		recyclerView.setLayoutManager(UIHelper.setMainLinearManagers(getContext()));
		recyclerView.setHasFixedSize(true);
	}

	private void updateServices(List<Channel> channels, View view) {
		RecyclerView recyclerView = view.findViewById(R.id.balances_recyclerView);
		balanceAdapter = new BalanceAdapter(channels, (MainActivity) getActivity());
		recyclerView.setAdapter(balanceAdapter);
		recyclerView.setVisibility(channels != null && channels.size() > 0 ? VISIBLE : GONE);

		((StaxCardView) view.findViewById(R.id.balance_card)).backButton.setVisibility(channels != null && channels.size() > 0  ? VISIBLE : GONE);
		channelDropdown.toggleLink(channels != null && channels.size() > 0);
	}

	private void setUpChannelDropdown() {
		channelDropdownViewModel.getChannels().observe(getViewLifecycleOwner(), channels -> channelDropdown.updateChannels(channels));
		channelDropdownViewModel.getSimChannels().observe(getViewLifecycleOwner(), channels -> channelDropdown.updateChannels(channels));
	}

	private void refreshBalances(View v) {
		if (channelDropdown.getHighlighted() != null) {
			balancesViewModel.getActions().observe(getViewLifecycleOwner(), actions -> {
				balancesViewModel.setAllRunning(v.getContext());
			});
			channelDropdownViewModel.setChannelSelected(channelDropdown.getHighlighted());
		} else
			balancesViewModel.setAllRunning(v.getContext());
	}

	private void setUpFuture(View root) {
		futureViewModel.getScheduled().observe(getViewLifecycleOwner(), schedules -> {
			RecyclerView recyclerView = root.findViewById(R.id.scheduled_recyclerView);
			recyclerView.setLayoutManager(UIHelper.setMainLinearManagers(getContext()));
			recyclerView.setAdapter(new ScheduledAdapter(schedules, this));
			setFutureVisible(root, schedules, futureViewModel.getRequests().getValue());
		});

		futureViewModel.getRequests().observe(getViewLifecycleOwner(), requests -> {
			RecyclerView rv = root.findViewById(R.id.requests_recyclerView);
			rv.setLayoutManager(UIHelper.setMainLinearManagers(getContext()));
			rv.setAdapter(new RequestsAdapter(requests, this));
			setFutureVisible(root, futureViewModel.getScheduled().getValue(), requests);
		});
	}

	private void setFutureVisible(View root, List<Schedule> schedules, List<Request> requests) {
		boolean visible = (schedules != null && schedules.size() > 0) || (requests != null && requests.size() > 0);
		root.findViewById(R.id.scheduled_card).setVisibility(visible ? VISIBLE : GONE);
	}

	private void setUpHistory(View view) {
		RecyclerView rv = view.findViewById(R.id.transaction_history_recyclerView);
		rv.setLayoutManager(UIHelper.setMainLinearManagers(getContext()));

		transactionsViewModel.getStaxTransactions().observe(getViewLifecycleOwner(), staxTransactions -> {
			rv.setAdapter(new TransactionHistoryAdapter(staxTransactions, BalancesFragment.this));
			view.findViewById(R.id.no_history).setVisibility(staxTransactions.size() > 0 ? GONE : VISIBLE);
		});
	}

	@Override
	public void viewTransactionDetail(String uuid) {
		Bundle bundle = new Bundle();
		bundle.putString(TransactionContract.COLUMN_UUID, uuid);
		NavHostFragment.findNavController(this).navigate(R.id.transactionDetailsFragment, bundle);
	}

	@Override
	public void viewScheduledDetail(int id) {
		Bundle bundle = new Bundle();
		bundle.putInt("id", id);
		NavHostFragment.findNavController(this).navigate(R.id.scheduleDetailsFragment, bundle);
	}

	@Override
	public void viewRequestDetail(int id) {
		Bundle bundle = new Bundle();
		bundle.putInt("id", id);
		NavHostFragment.findNavController(this).navigate(R.id.requestDetailsFragment, bundle);
	}
}
