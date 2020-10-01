package com.hover.stax.home;

import android.content.Intent;
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
import com.hover.sdk.transactions.TransactionContract;
import com.hover.stax.ApplicationInstance;
import com.hover.stax.R;
import com.hover.stax.channels.Channel;
import com.hover.stax.channels.ChannelsActivity;
import com.hover.stax.scheduled.ScheduledAdapter;
import com.hover.stax.scheduled.ScheduledViewModel;
import com.hover.stax.transactions.TransactionHistoryAdapter;
import com.hover.stax.transactions.TransactionHistoryViewModel;
import com.hover.stax.utils.DateUtils;
import com.hover.stax.utils.UIHelper;

import java.util.List;

public class HomeFragment extends Fragment implements TransactionHistoryAdapter.SelectListener, ScheduledAdapter.SelectListener {
	final public static String TAG = "HomeFragment";

	private BalancesViewModel balancesViewModel;
	private ScheduledViewModel futureViewModel;
	private TransactionHistoryViewModel transactionsViewModel;
	private RecyclerView recyclerView, transactionHistoryRecyclerView;

	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Amplitude.getInstance().logEvent(getString(R.string.visit_screen, getString(R.string.visit_home)));
		balancesViewModel = new ViewModelProvider(requireActivity()).get(BalancesViewModel.class);
		futureViewModel = new ViewModelProvider(requireActivity()).get(ScheduledViewModel.class);
		transactionsViewModel = new ViewModelProvider(requireActivity()).get(TransactionHistoryViewModel.class);
		return inflater.inflate(R.layout.fragment_home, container, false);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		setUpBalances(view);
		setUpFuture(view);
		setUpHistory(view);
	}

	private void setUpBalances(View view) {
		recyclerView = view.findViewById(R.id.balances_recyclerView);
		recyclerView.setLayoutManager(UIHelper.setMainLinearManagers(getContext()));
		recyclerView.setHasFixedSize(true);

		balancesViewModel.getSelectedChannels().observe(getViewLifecycleOwner(), channels -> {
			recyclerView.setAdapter(new BalanceAdapter(channels, (MainActivity) getActivity()));
			setMeta(view, channels);
		});

		view.findViewById(R.id.balances_header).setOnClickListener(v -> {
			Amplitude.getInstance().logEvent(getString(R.string.click_add_account));
			requireActivity().startActivityForResult(new Intent(getActivity(), ChannelsActivity.class), MainActivity.ADD_SERVICE);
		});
	}

	private void setUpFuture(View view) {
		recyclerView = view.findViewById(R.id.scheduled_recyclerView);
		recyclerView.setLayoutManager(UIHelper.setMainLinearManagers(getContext()));
		recyclerView.setHasFixedSize(true);

		futureViewModel.getScheduled().observe(getViewLifecycleOwner(), schedules -> {
			recyclerView.setAdapter(new ScheduledAdapter(schedules, this));
			view.findViewById(R.id.scheduled_card).setVisibility(schedules != null && schedules.size() > 0 ? View.VISIBLE : View.GONE);
		});
	}

	private void setUpHistory(View view) {
		transactionHistoryRecyclerView = view.findViewById(R.id.transaction_history_recyclerView);
		transactionHistoryRecyclerView.setLayoutManager(UIHelper.setMainLinearManagers(getContext()));

		transactionsViewModel.getStaxTransactions().observe(getViewLifecycleOwner(), staxTransactions -> {
			transactionHistoryRecyclerView.setAdapter(new TransactionHistoryAdapter(staxTransactions, HomeFragment.this));
			view.findViewById(R.id.no_history).setVisibility(staxTransactions.size() > 0 ? View.GONE : View.VISIBLE);
		});
	}

	private void setMeta(View view, List<Channel> channels) {
		TextView homeTimeAgo = view.findViewById(R.id.homeTimeAgo);
		long mostRecentTimestamp = 0;
		for (Channel c : channels) {
			if (c.latestBalanceTimestamp != null && c.latestBalanceTimestamp > mostRecentTimestamp)
				mostRecentTimestamp = c.latestBalanceTimestamp;
		}
		homeTimeAgo.setText(mostRecentTimestamp > 0 ? DateUtils.timeAgo(ApplicationInstance.getContext(), mostRecentTimestamp) : "Refresh");
		view.findViewById(R.id.homeTimeAgo).setVisibility(channels.size() > 0 ? View.VISIBLE : View.GONE);
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
		NavHostFragment.findNavController(this).navigate(R.id.transactionDetailsFragment, bundle);
	}
}
