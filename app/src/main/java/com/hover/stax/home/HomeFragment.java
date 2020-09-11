package com.hover.stax.home;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import com.amplitude.api.Amplitude;
import com.hover.stax.ApplicationInstance;
import com.hover.stax.R;
import com.hover.stax.channels.Channel;
import com.hover.stax.channels.ChannelsActivity;
import com.hover.stax.utils.DateUtils;
import com.hover.stax.utils.UIHelper;
import com.hover.stax.utils.Utils;

import java.util.List;

public class HomeFragment extends Fragment {

	private HomeViewModel homeViewModel;
	private RecyclerView recyclerView, transactionHistoryRecyclerView;

	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_home, container, false);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		homeViewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);

		transactionHistoryRecyclerView = view.findViewById(R.id.transaction_history_recyclerView);
		view.findViewById(R.id.balances_header).setOnClickListener(v -> {
			Amplitude.getInstance().logEvent(getString(R.string.click_add_account));
			startActivityForResult(new Intent(getActivity(), ChannelsActivity.class), MainActivity.ADD_SERVICE);
		});

		recyclerView = view.findViewById(R.id.balances_recyclerView);
		recyclerView.setLayoutManager(UIHelper.setMainLinearManagers(getContext()));
		recyclerView.setHasFixedSize(true);

		homeViewModel.getSelectedChannels().observe(getViewLifecycleOwner(), channels -> {
			recyclerView.setAdapter(new BalanceAdapter(channels, (MainActivity) getActivity()));
			setMeta(view, channels);
		});

		homeViewModel.getStaxTranssactions().observe(getViewLifecycleOwner(), staxTransactions -> {
			transactionHistoryRecyclerView.setLayoutManager(UIHelper.setMainLinearManagers(getContext()));
			transactionHistoryRecyclerView.setAdapter(new TransactionHistoryAdapter(staxTransactions));
			view.findViewById(R.id.transactionsLabel).setVisibility(staxTransactions.size() > 0 ? View.VISIBLE : View.GONE);
		});

		LocalBroadcastManager.getInstance(requireActivity()).registerReceiver(transactionReceiver, new IntentFilter(Utils.getPackage(getActivity()) + ".TRANSACTION_UPDATE"));
		homeViewModel.updateTransactions();
	}

	private void setMeta(View view, List<Channel> channels) {
		TextView homeTimeAgo = view.findViewById(R.id.homeTimeAgo);
		long mostRecentTimestamp = 0;
		for (Channel c : channels) {
			if (c.latestBalanceTimestamp != null && c.latestBalanceTimestamp > mostRecentTimestamp)
				mostRecentTimestamp = c.latestBalanceTimestamp;
		}
		homeTimeAgo.setText(mostRecentTimestamp > 0 ? DateUtils.timeAgo(ApplicationInstance.getContext(), mostRecentTimestamp) : "Refresh");
		homeTimeAgo.setOnClickListener(view2 -> {
			Amplitude.getInstance().logEvent(getString(R.string.refresh_balance_all));
			((MainActivity) getActivity()).runAllBalances();
		});

		view.findViewById(R.id.homeTimeAgo).setVisibility(channels.size() > 0 ? View.VISIBLE : View.GONE);
		view.findViewById(R.id.homeBalanceDesc).setVisibility(channels.size() > 0 ? View.GONE : View.VISIBLE);

		transactionHistoryRecyclerView.setVisibility(channels.size() > 0 ? View.VISIBLE : View.GONE);
	}

	private final BroadcastReceiver transactionReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(final Context context, final Intent i) {
			if (homeViewModel != null) { homeViewModel.updateTransactions(); }
		}
	};

	@Override
	public void onDestroy() {
		super.onDestroy();
		unregisterTransactionReceiver();
	}

	private void unregisterTransactionReceiver() {
		try { requireActivity().unregisterReceiver(transactionReceiver);
		} catch (Exception ignored) { }
	}
}
