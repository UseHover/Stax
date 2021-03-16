package com.hover.stax.channels;

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
import com.hover.stax.R;
import com.hover.stax.home.MainActivity;
import com.hover.stax.transactions.TransactionHistoryAdapter;
import com.hover.stax.utils.UIHelper;
import com.hover.stax.utils.Utils;
import com.hover.stax.utils.customSwipeRefresh.CustomSwipeRefreshLayout;

public class ChannelDetailFragment extends Fragment implements TransactionHistoryAdapter.SelectListener, CustomSwipeRefreshLayout.OnRefreshListener {
	private RecyclerView transactionHistoryRecyclerView;
	private ChannelDetailViewModel viewModel;

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		viewModel = new ViewModelProvider(requireActivity()).get(ChannelDetailViewModel.class);
		Amplitude.getInstance().logEvent(getString(R.string.visit_screen, getString(R.string.visit_channel)));
		return inflater.inflate(R.layout.fragment_channel, container, false);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		transactionHistoryRecyclerView = view.findViewById(R.id.transaction_history_recyclerView);
		setupPullRefresh(view);

		viewModel.getChannel().observe(getViewLifecycleOwner(), channel -> {
			((TextView) view.findViewById(R.id.title)).setText(channel.name);
			((TextView) view.findViewById(R.id.fees_description)).setText(getString(R.string.fees_label, channel.name));
			((TextView) view.findViewById(R.id.details_balance)).setText(channel.latestBalance);
		});

		viewModel.getStaxTransactions().observe(getViewLifecycleOwner(), staxTransactions -> {
			view.findViewById(R.id.no_history).setVisibility(staxTransactions == null || staxTransactions.size() == 0 ? View.VISIBLE : View.GONE);
			transactionHistoryRecyclerView.setLayoutManager(UIHelper.setMainLinearManagers(getContext()));
			transactionHistoryRecyclerView.setAdapter(new TransactionHistoryAdapter(staxTransactions, this));
		});

		viewModel.getSpentThisMonth().observe(getViewLifecycleOwner(), sum -> {
			((TextView) view.findViewById(R.id.details_money_out)).setText(Utils.formatAmount(sum != null ? sum : 0));
		});

		viewModel.getFeesThisYear().observe(getViewLifecycleOwner(), sum -> {
			((TextView) view.findViewById(R.id.details_fees)).setText(Utils.formatAmount(sum != null ? sum : 0));
		});
		viewModel.setChannel(getArguments().getInt(TransactionContract.COLUMN_CHANNEL_ID));
	}

	private void setupPullRefresh(View view) {
		CustomSwipeRefreshLayout swipeRefreshLayout = view.findViewById(R.id.swipelayout);
		if (swipeRefreshLayout != null) {
			swipeRefreshLayout.setRefreshCompleteTimeout(1000);
			swipeRefreshLayout.enableTopProgressBar(false);
			swipeRefreshLayout.setOnRefreshListener(this);
		}
	}

	@Override
	public void viewTransactionDetail(String uuid) {
		Bundle bundle = new Bundle();
		bundle.putString(TransactionContract.COLUMN_UUID, uuid);
		NavHostFragment.findNavController(this).navigate(R.id.transactionDetailsFragment, bundle);
	}

	@Override
	public void onRefresh() {
		if (getActivity() != null && viewModel.getChannel().getValue() != null)
			((MainActivity) getActivity()).onTapRefresh(viewModel.getChannel().getValue().id);
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
	}
}
