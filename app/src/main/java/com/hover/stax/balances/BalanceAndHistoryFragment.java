package com.hover.stax.balances;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.RecyclerView;

import com.amplitude.api.Amplitude;
import com.google.android.material.textfield.TextInputLayout;
import com.hover.sdk.transactions.TransactionContract;
import com.hover.stax.R;
import com.hover.stax.channels.Channel;
import com.hover.stax.channels.ChannelDropdownAdapter;
import com.hover.stax.channels.ChannelListViewModel;
import com.hover.stax.home.MainActivity;
import com.hover.stax.requests.Request;
import com.hover.stax.schedules.Schedule;
import com.hover.stax.transactions.TransactionHistoryAdapter;
import com.hover.stax.transactions.TransactionHistoryViewModel;
import com.hover.stax.utils.DateUtils;
import com.hover.stax.utils.UIHelper;
import com.hover.stax.views.StaxCardView;

import java.util.List;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class BalanceAndHistoryFragment extends Fragment implements TransactionHistoryAdapter.SelectListener, ScheduledAdapter.SelectListener, RequestsAdapter.SelectListener {
	final public static String TAG = "BalanceFragment";

	private BalancesViewModel balancesViewModel;
	private FutureViewModel futureViewModel;
	private TransactionHistoryViewModel transactionsViewModel;
	private BalanceAdapter balanceAdapter;
	private ChannelListViewModel channelViewModel;
	private AutoCompleteTextView channelDropdown;
	private TextView addAccountText;
	private TextInputLayout linkAccountLayout;

	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Amplitude.getInstance().logEvent(getString(R.string.visit_screen, getString(R.string.visit_balance_and_history)));
		balancesViewModel = new ViewModelProvider(requireActivity()).get(BalancesViewModel.class);
		channelViewModel = new ViewModelProvider(requireActivity()).get(ChannelListViewModel.class);
		futureViewModel = new ViewModelProvider(requireActivity()).get(FutureViewModel.class);
		transactionsViewModel = new ViewModelProvider(requireActivity()).get(TransactionHistoryViewModel.class);
		return inflater.inflate(R.layout.fragment_balance_history, container, false);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		addAccountText = view.findViewById(R.id.add_new_account);
		linkAccountLayout = view.findViewById(R.id.linkAccountLayout);

		setUpBalances(view);
		setUpAddAccount(view);
		setUpFuture(view);
		setUpHistory(view);
		setupRefreshBalance(view);
	}

	private void setUpBalances(View view) {
		initBalanceCard(view);
		balancesViewModel.getSelectedChannels().observe(getViewLifecycleOwner(), channels -> updateServices(channels, view));
		balancesViewModel.getBalanceError().observe(getViewLifecycleOwner(), show -> showError(show));
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
		toggleAddAccountInput(channels != null && channels.size() > 0);
	}

	void toggleAddAccountInput(boolean showLink) {
		addAccountText.setVisibility(showLink ? VISIBLE : GONE);
		linkAccountLayout.setVisibility(showLink ? GONE : VISIBLE);
		addAccountText.setOnClickListener(v -> toggleAddAccountInput(false));
	}

	private void showError(boolean show) {
		linkAccountLayout.setError(show ? getString(R.string.refresh_balance_error) : null);
		linkAccountLayout.setErrorIconDrawable(show ? R.drawable.ic_error_warning_24dp : 0);
		if (show)
			channelDropdown.setText(getString(R.string.link_an_account), false);
	}

	private void setUpAddAccount(View view) {
		channelDropdown = view.findViewById(R.id.channelDropdown);
		channelViewModel.getChannels().observe(getViewLifecycleOwner(), channels -> {
			if (channels != null && channels.size() > 0 && (channelViewModel.getSimChannels().getValue() == null || channelViewModel.getSimChannels().getValue().size() == 0))
				updateChannelDropdown(channels);
		});
		channelViewModel.getSimChannels().observe(getViewLifecycleOwner(), this::updateChannelDropdown);
	}

	private void updateChannelDropdown(List<Channel> channels) {
		if (channels == null || channels.size() == 0 || getContext() == null) return;
		channelDropdown.setText("");
		ChannelDropdownAdapter channelDropdownAdapter = new ChannelDropdownAdapter(channels,  false, getContext());
		channelDropdown.setAdapter(channelDropdownAdapter);
		channelDropdown.setOnItemClickListener((adapterView, view2, pos, id) -> {
			balancesViewModel.highlightChannel((Channel) adapterView.getItemAtPosition(pos));
		});
	}

	private void setupRefreshBalance(View view) {
		view.findViewById(R.id.refresh_accounts_btn).setOnClickListener(v -> {
			if (balancesViewModel.getHighlightedChannel() != null) {
				balancesViewModel.getBalanceActions().observe(getViewLifecycleOwner(), actions -> {
					balancesViewModel.setAllRunning(v.getContext());
				});
				balancesViewModel.selectChannel(v.getContext());
			} else
				balancesViewModel.setAllRunning(v.getContext());
		});
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
			rv.setAdapter(new TransactionHistoryAdapter(staxTransactions, BalanceAndHistoryFragment.this));
			view.findViewById(R.id.no_history).setVisibility(staxTransactions.size() > 0 ? GONE : VISIBLE);
		});
	}

	private void setMeta(View view, List<Channel> channels) {
		long mostRecentTimestamp = 0;
		for (Channel c : channels) {
			if (c.latestBalanceTimestamp != null && c.latestBalanceTimestamp > mostRecentTimestamp)
				mostRecentTimestamp = c.latestBalanceTimestamp;
		}
		((TextView) view.findViewById(R.id.homeTimeAgo)).setText(mostRecentTimestamp > 0 ? DateUtils.timeAgo(view.getContext(), mostRecentTimestamp) : "Refresh");
		view.findViewById(R.id.homeTimeAgo).setVisibility(channels.size() > 0 ? VISIBLE : GONE);
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
