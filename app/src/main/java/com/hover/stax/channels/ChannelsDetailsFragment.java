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
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.RecyclerView;

import com.amplitude.api.Amplitude;
import com.hover.sdk.transactions.TransactionContract;
import com.hover.stax.R;
import com.hover.stax.transactions.TransactionDetailsFragment;
import com.hover.stax.transactions.TransactionHistoryAdapter;
import com.hover.stax.utils.UIHelper;
import com.hover.stax.utils.Utils;

public class ChannelsDetailsFragment extends Fragment implements TransactionHistoryAdapter.SelectListener {
	private RecyclerView transactionHistoryRecyclerView;
	private ChannelsDetailsViewModel viewModel;
	private int channelId;
	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		viewModel = new ViewModelProvider(requireActivity()).get(ChannelsDetailsViewModel.class);
		channelId = getArguments().getInt(TransactionContract.COLUMN_CHANNEL_ID);
		viewModel.setStaxTransactions(channelId);
		Amplitude.getInstance().logEvent(getString(R.string.visit_screen, getString(R.string.nav_account_detail)));
		return inflater.inflate(R.layout.channels_details_layout, container, false);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		((AppCompatActivity) getActivity()).setSupportActionBar(view.findViewById(R.id.toolbar));
		transactionHistoryRecyclerView = view.findViewById(R.id.transaction_history_recyclerView);

		viewModel.getThisMonthSpentLiveData().observe(getViewLifecycleOwner(), thisMonth -> {
			viewModel.getLastMonthSpentLiveData().observe(getViewLifecycleOwner(), lastMonth -> {
				viewModel.getChannel().observe(getViewLifecycleOwner(), channel -> {
					((TextView) view.findViewById(R.id.details_balance)).setText(channel.latestBalance);
					((TextView) view.findViewById(R.id.spentThisMonthContent)).setText(Utils.formatAmountV2(thisMonth));
					((TextView) view.findViewById(R.id.channel_name)).setText(channel.name);

					double spentDiff = 0;
					if(lastMonth !=null) {
						 spentDiff = thisMonth - lastMonth;
					}

					String suffix = getResources().getString(R.string.more_than_last_month);
					if(String.valueOf(spentDiff).contains("-")) suffix = getResources().getString(R.string.less_than_last_month);
					String fullString = Utils.formatAmountV2(spentDiff) +" "+ suffix;
					((TextView) view.findViewById(R.id.spentDifference)).setText(fullString);

				});
			});
		});

			viewModel.getStaxTransactions().observe(getViewLifecycleOwner(), staxTransactions -> {
					transactionHistoryRecyclerView.setLayoutManager(UIHelper.setMainLinearManagers(getContext()));
					transactionHistoryRecyclerView.setAdapter(new TransactionHistoryAdapter(staxTransactions, this, channelId));
			});

	}

	@Override
	public void onTap(String uuid) {
		Bundle bundle = new Bundle();
		bundle.putString(TransactionContract.COLUMN_UUID, uuid);
		NavHostFragment.findNavController(this).navigate(R.id.transactionDetailsFragment, bundle);
	}

	@Override
	public void onTapChannel(int channelId) {
		Bundle bundle = new Bundle();
		bundle.putInt(TransactionContract.COLUMN_CHANNEL_ID, channelId);
		NavHostFragment.findNavController(this).navigate(R.id.channelsDetailsFragment, bundle);
	}

}
