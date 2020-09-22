package com.hover.stax.channels;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.RecyclerView;

import com.amplitude.api.Amplitude;
import com.hover.sdk.transactions.TransactionContract;
import com.hover.stax.R;
import com.hover.stax.transactions.TransactionHistoryAdapter;
import com.hover.stax.utils.UIHelper;
import com.hover.stax.utils.Utils;

public class ChannelDetailFragment extends Fragment implements TransactionHistoryAdapter.SelectListener {
	private RecyclerView transactionHistoryRecyclerView;
	private ChannelDetailViewModel viewModel;

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		viewModel = new ViewModelProvider(requireActivity()).get(ChannelDetailViewModel.class);
		Amplitude.getInstance().logEvent(getString(R.string.visit_screen, getString(R.string.nav_account_detail)));
		return inflater.inflate(R.layout.channels_details_layout, container, false);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		((AppCompatActivity) getActivity()).setSupportActionBar(view.findViewById(R.id.toolbar));
		transactionHistoryRecyclerView = view.findViewById(R.id.transaction_history_recyclerView);

		viewModel.getChannel().observe(getViewLifecycleOwner(), channel -> {
			((TextView) view.findViewById(R.id.channel_name)).setText(channel.name);
			((TextView) view.findViewById(R.id.details_balance)).setText(channel.latestBalance);
		});

		viewModel.getStaxTransactions().observe(getViewLifecycleOwner(), staxTransactions -> {
			transactionHistoryRecyclerView.setLayoutManager(UIHelper.setMainLinearManagers(getContext()));
			transactionHistoryRecyclerView.setAdapter(new TransactionHistoryAdapter(staxTransactions, this));
		});

		viewModel.getSpentThisMonth().observe(getViewLifecycleOwner(), thisMonth -> {
			((TextView) view.findViewById(R.id.spentThisMonthContent)).setText(Utils.formatAmount(thisMonth != null ? thisMonth : 0));
		});

		viewModel.setChannel(getArguments().getInt(TransactionContract.COLUMN_CHANNEL_ID));
	}

	@Override
	public void onTap(String uuid) {
		Bundle bundle = new Bundle();
		bundle.putString(TransactionContract.COLUMN_UUID, uuid);
		NavHostFragment.findNavController(this).navigate(R.id.transactionDetailsFragment, bundle);
	}
}
