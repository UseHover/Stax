package com.hover.stax.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
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
import com.hover.stax.actions.Action;
import com.hover.stax.channels.Channel;
import com.hover.stax.channels.ChannelDropdownAdapter;
import com.hover.stax.channels.ChannelListViewModel;
import com.hover.stax.requests.Request;
import com.hover.stax.schedules.Schedule;
import com.hover.stax.transactions.TransactionHistoryAdapter;
import com.hover.stax.transactions.TransactionHistoryViewModel;
import com.hover.stax.utils.DateUtils;
import com.hover.stax.utils.UIHelper;
import com.hover.stax.utils.customSwipeRefresh.CustomSwipeRefreshLayout;
import com.hover.stax.views.StaxCardView;

import java.util.List;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class BalanceAndHistoryFragment extends Fragment implements TransactionHistoryAdapter.SelectListener, ScheduledAdapter.SelectListener, RequestsAdapter.SelectListener {
	final public static String TAG = "BalanceAndHistoryFragment";

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
		setUpSimChannels(view);
		setUpFuture(view);
		setUpHistory(view);
		setupRefreshBalance(view);
	}

	void setAddAccountVisibilityStagTeToOnlyText(boolean isOnlyTextVisible) {
		if(isOnlyTextVisible) {
			addAccountText.setVisibility(VISIBLE);
			linkAccountLayout.setVisibility(GONE);
			addAccountText.setOnClickListener(v -> setAddAccountVisibilityStagTeToOnlyText(false));

		}else {
			addAccountText.setVisibility(GONE);
			linkAccountLayout.setVisibility(VISIBLE);
		}
	}


	private void setUpBalances(View view) {
		StaxCardView balanceCard = view.findViewById(R.id.balance_card);
		balanceCard.backButton.setVisibility(GONE);
		balanceCard.setActivated(false);


		RecyclerView recyclerView = view.findViewById(R.id.balances_recyclerView);
		recyclerView.setLayoutManager(UIHelper.setMainLinearManagers(getContext()));
		recyclerView.setHasFixedSize(true);

		balancesViewModel.getSelectedChannels().observe(getViewLifecycleOwner(), channels -> {


			balanceAdapter = new BalanceAdapter(channels, (MainActivity) getActivity());
			recyclerView.setAdapter(balanceAdapter);
			recyclerView.setVisibility(channels != null && channels.size() > 0 ? VISIBLE : GONE);

			balanceCard.backButton.setVisibility(channels != null && channels.size() > 0  ? VISIBLE : GONE);
			balanceCard.backButton.setOnClickListener(v -> {
				balanceCard.backButton.setActivated(!balanceCard.backButton.isActivated());
				balanceAdapter.updateShowBalance();
			});
			setAddAccountVisibilityStagTeToOnlyText(channels !=null && channels.size() > 0);

			//setMeta(view, channels);
		});

		balancesViewModel.getBalanceError().observe(getViewLifecycleOwner(), showError-> {
			if(showError) {
				linkAccountLayout.setError(getString(R.string.refresh_balance_error));
				linkAccountLayout.setErrorIconDrawable(R.drawable.ic_error_warning_24dp);
				channelDropdown.setText(getString(R.string.link_an_account), false);
			}else {
				linkAccountLayout.setError(null);
				linkAccountLayout.setErrorIconDrawable(0);
			}
		});
	}

	private void setUpSimChannels(View view) {
		channelDropdown = view.findViewById(R.id.channelDropdown);
		channelViewModel.getSimChannels().observe(getViewLifecycleOwner(), channels -> {
			if (channels == null || channels.size() == 0 || getContext() == null) return;


			ChannelDropdownAdapter channelDropdownAdapter = new ChannelDropdownAdapter(channels,  false, getContext());
			channelDropdown.setAdapter(channelDropdownAdapter);
			channelDropdown.setOnItemClickListener((adapterView, view2, pos, id) -> {
				Channel channel = (Channel) adapterView.getItemAtPosition(pos);
				balancesViewModel.setChannelSelectedFromSpinner(channel);
			});
		});
	}

	private void setupRefreshBalance(View view) {
		view.findViewById(R.id.refresh_accounts_btn).setOnClickListener(v -> {
			BalanceAdapter.BalanceListener balanceListener = ((MainActivity) getActivity());
			if(balanceListener !=null) {
				balancesViewModel.saveChannelSelectedFromSpinner();
				if(balancesViewModel.validateRun())balanceListener.triggerRefreshAll();
			}
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
