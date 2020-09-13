package com.hover.stax.home.detailsPages.transaction;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.hover.stax.ApplicationInstance;
import com.hover.stax.R;
import com.hover.stax.models.StaxDate;
import com.hover.stax.utils.UIHelper;

public class TransactionDetailsFragment extends Fragment {
	private Bundle bundle;

	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		bundle = getArguments();
		return inflater.inflate(R.layout.transaction_details_layout, container, false);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		RecyclerView messagesRecyclerView = view.findViewById(R.id.transac_messages_recyclerView);
		messagesRecyclerView.setLayoutManager(UIHelper.setMainLinearManagers(ApplicationInstance.getContext()));
		messagesRecyclerView.setHasFixedSize(true);

		view.findViewById(R.id.backText).setOnClickListener(view2 -> {
			if (getActivity() != null)
				getActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
		});

		TextView amountText = view.findViewById(R.id.details_amount);
		TextView dateText = view.findViewById(R.id.details_date);
		TextView transactionNumberText = view.findViewById(R.id.details_transactionNumber);
		TextView networkName = view.findViewById(R.id.details_network);


		TransactionDetailsViewModel transactionDetailsViewModel = new ViewModelProvider(this).get(TransactionDetailsViewModel.class);
		if (bundle != null) {
			String transUUID = bundle.getString("id");
			transactionDetailsViewModel.getMessagesModels(transUUID);

			transactionDetailsViewModel.loadStaxTransaction().observe(getViewLifecycleOwner(), staxTransaction -> {
				if (staxTransaction != null) {
					amountText.setText(staxTransaction.getAmount());
					StaxDate staxDate = staxTransaction.getStaxDate();
					if (staxDate != null)
						dateText.setText(staxDate.getMonth() + "/" + staxDate.getDayOfMonth() + "/" + staxDate.getYear());
					transactionNumberText.setText(staxTransaction.getUuid());
					networkName.setText(staxTransaction.getNetworkName());

				}
			});

			transactionDetailsViewModel.loadMessagesModelObs().observe(getViewLifecycleOwner(), model -> {
				if (model != null)
					messagesRecyclerView.setAdapter(new TransactionMessagesRecyclerAdapter(model));
			});
		}

	}
}
