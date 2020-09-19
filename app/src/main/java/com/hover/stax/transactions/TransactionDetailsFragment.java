package com.hover.stax.transactions;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.hover.sdk.transactions.TransactionContract;
import com.hover.stax.ApplicationInstance;
import com.hover.stax.R;
import com.hover.stax.utils.DateUtils;
import com.hover.stax.utils.UIHelper;

public class TransactionDetailsFragment extends Fragment {
	private TransactionDetailsViewModel viewModel;

	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		viewModel = new ViewModelProvider(this).get(TransactionDetailsViewModel.class);
		viewModel.setTransaction(getArguments().getString(TransactionContract.COLUMN_UUID));
		return inflater.inflate(R.layout.transaction_details_layout, container, false);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		((AppCompatActivity) getActivity()).setSupportActionBar(view.findViewById(R.id.toolbar));

		viewModel.getTransaction().observe(getViewLifecycleOwner(), transaction -> {
			if (transaction != null) {
				((TextView) view.findViewById(R.id.details_transactionNumber)).setText(transaction.uuid);
				((TextView) view.findViewById(R.id.details_amount)).setText(transaction.amount);
				((TextView) view.findViewById(R.id.details_date)).setText(DateUtils.humanFriendlyDate(transaction.initiated_at));
			}
		});

		viewModel.getAction().observe(getViewLifecycleOwner(), action -> {
			if (action != null) {
				((TextView) view.findViewById(R.id.details_network)).setText(action.network_name);
			}
		});

		RecyclerView messagesView = view.findViewById(R.id.convo_recyclerView);
		messagesView.setLayoutManager(UIHelper.setMainLinearManagers(ApplicationInstance.getContext()));
		messagesView.setHasFixedSize(true);
		viewModel.getMessages().observe(getViewLifecycleOwner(), ussdCallResponses -> {
			if (ussdCallResponses != null) {
				messagesView.setAdapter(new MessagesAdapter(ussdCallResponses));
			}
		});

		RecyclerView smsView = view.findViewById(R.id.sms_recyclerView);
		smsView.setLayoutManager(UIHelper.setMainLinearManagers(ApplicationInstance.getContext()));
		smsView.setHasFixedSize(true);
		viewModel.getSms().observe(getViewLifecycleOwner(), smses -> {
			if (smses != null) {
				smsView.setAdapter(new MessagesAdapter(smses));
			}
		});
	}
}
