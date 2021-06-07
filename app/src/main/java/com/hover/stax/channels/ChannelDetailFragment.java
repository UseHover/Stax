package com.hover.stax.channels;

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

import com.hover.sdk.transactions.TransactionContract;
import com.hover.stax.R;
import com.hover.stax.balances.FutureViewModel;
import com.hover.stax.balances.RequestsAdapter;
import com.hover.stax.balances.ScheduledAdapter;
import com.hover.stax.databinding.FragmentChannelBinding;
import com.hover.stax.home.MainActivity;
import com.hover.stax.navigation.NavigationInterface;
import com.hover.stax.requests.Request;
import com.hover.stax.schedules.Schedule;
import com.hover.stax.transactions.TransactionHistoryAdapter;
import com.hover.stax.utils.UIHelper;
import com.hover.stax.utils.Utils;

import java.util.List;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class ChannelDetailFragment extends Fragment implements
        TransactionHistoryAdapter.SelectListener,
        ScheduledAdapter.SelectListener,
        RequestsAdapter.SelectListener,
        NavigationInterface {

    private ChannelDetailViewModel viewModel;
    private FutureViewModel futureViewModel;
    private FragmentChannelBinding binding;
    private RequestsAdapter requestsAdapter;
    private ScheduledAdapter scheduledAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Utils.logAnalyticsEvent(getString(R.string.visit_screen, getString(R.string.visit_channel)), requireContext());
        viewModel = new ViewModelProvider(requireActivity()).get(ChannelDetailViewModel.class);
        futureViewModel = new ViewModelProvider(requireActivity()).get(FutureViewModel.class);
        binding = FragmentChannelBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.refreshBalanceBtn.setOnClickListener(view1 -> onRefresh());
        initRecyclerViews();
        setupObservers();

        assert getArguments() != null;
        viewModel.setChannel(getArguments().getInt(TransactionContract.COLUMN_CHANNEL_ID));

    }

    private void setupObservers() {
        viewModel.getChannel().observe(getViewLifecycleOwner(), channel -> {
            binding.staxCardView.setTitle(channel.name);
            binding.feesDescription.setText(getString(R.string.fees_label, channel.name));
            binding.detailsBalance.setText(channel.latestBalance);

            setUpFuture(channel);
        });

        RecyclerView transactionHistoryRecyclerView = binding.homeCardTransactions.transactionHistoryRecyclerView;
        viewModel.getStaxTransactions().observe(getViewLifecycleOwner(), staxTransactions -> {
            binding.homeCardTransactions.noHistory.setVisibility(staxTransactions == null || staxTransactions.size() == 0 ? View.VISIBLE : View.GONE);
            transactionHistoryRecyclerView.setLayoutManager(UIHelper.setMainLinearManagers(getContext()));
            transactionHistoryRecyclerView.setAdapter(new TransactionHistoryAdapter(staxTransactions, this));
        });

        viewModel.getSpentThisMonth().observe(getViewLifecycleOwner(), sum -> {
            binding.detailsMoneyOut.setText(Utils.formatAmount(sum != null ? sum : 0));
        });

        viewModel.getFeesThisYear().observe(getViewLifecycleOwner(), sum -> {
            binding.detailsFees.setText(Utils.formatAmount(sum != null ? sum : 0));
        });
    }

    private void initRecyclerViews() {
        RecyclerView recyclerView = binding.scheduledCard.scheduledRecyclerView;
        recyclerView.setLayoutManager(UIHelper.setMainLinearManagers(getContext()));
        recyclerView.setAdapter(new ScheduledAdapter(null, this));

        RecyclerView rv = binding.scheduledCard.requestsRecyclerView;
        rv.setLayoutManager(UIHelper.setMainLinearManagers(getContext()));
        rv.setAdapter(new RequestsAdapter(null, this));
    }
    private void setUpFuture(Channel channel) {
        futureViewModel.getScheduledByChannel(channel.id).observe(getViewLifecycleOwner(), schedules -> {
            scheduledAdapter.updateData(schedules);
            setFutureVisible(schedules, futureViewModel.getRequests().getValue());
        });

        futureViewModel.getRequestsByChannel(channel.institutionId).observe(getViewLifecycleOwner(), requests -> {
            requestsAdapter.updateData(requests);
            setFutureVisible(futureViewModel.getScheduled().getValue(), requests);
        });
    }

    private void setFutureVisible(List<Schedule> schedules, List<Request> requests) {
        boolean visible = (schedules != null && schedules.size() > 0) || (requests != null && requests.size() > 0);
        binding.scheduledCard.getRoot().setVisibility(visible ? VISIBLE : GONE);
    }

    @Override
    public void viewTransactionDetail(String uuid) {
        Bundle bundle = new Bundle();
        bundle.putString(TransactionContract.COLUMN_UUID, uuid);
        NavHostFragment.findNavController(this).navigate(R.id.transactionDetailsFragment, bundle);
    }

    private void onRefresh() {
        if (getActivity() != null && viewModel.getChannel().getValue() != null)
            ((MainActivity) getActivity()).onTapRefresh(viewModel.getChannel().getValue().id);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void viewScheduledDetail(int id) {
        navigateToScheduleDetailsFragment(id, this);
    }

    @Override
    public void viewRequestDetail(int id) {
        navigateToRequestDetailsFragment(id, this);
    }
}
