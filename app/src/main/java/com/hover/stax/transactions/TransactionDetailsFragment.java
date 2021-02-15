package com.hover.stax.transactions;

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

import com.amplitude.api.Amplitude;
import com.hover.sdk.transactions.TransactionContract;
import com.hover.stax.R;
import com.hover.stax.actions.Action;
import com.hover.stax.utils.Constants;
import com.hover.stax.utils.DateUtils;
import com.hover.stax.utils.UIHelper;
import com.hover.stax.utils.Utils;
import com.hover.stax.views.Stax2LineItem;

import org.json.JSONException;
import org.json.JSONObject;

public class TransactionDetailsFragment extends Fragment {
	final public static String TAG = "TransDetailsFragment";

	private TransactionDetailsViewModel viewModel;

	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		viewModel = new ViewModelProvider(requireActivity()).get(TransactionDetailsViewModel.class);
		JSONObject data = new JSONObject();
		try {
			data.put("uuid", getArguments().getString(TransactionContract.COLUMN_UUID));
		} catch (JSONException e) {
		}
		Amplitude.getInstance().logEvent(getString(R.string.visit_screen, getString(R.string.visit_transaction)), data);
		return inflater.inflate(R.layout.fragment_transaction, container, false);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		viewModel.getTransaction().observe(getViewLifecycleOwner(), transaction -> {
			if (transaction != null) {
				((TextView) view.findViewById(R.id.title)).setText(transaction.description);
				((TextView) view.findViewById(R.id.details_amount)).setText((transaction.transaction_type.equals(Action.RECEIVE) ? "" : "-") + Utils.formatAmount(transaction.amount));
				((TextView) view.findViewById(R.id.details_date)).setText(DateUtils.humanFriendlyDate(transaction.initiated_at));

				if (transaction.confirm_code != null && !transaction.confirm_code.isEmpty())
					((TextView) view.findViewById(R.id.details_transactionNumber)).setText(transaction.confirm_code);
				else
					((TextView) view.findViewById(R.id.details_transactionNumber)).setText(transaction.uuid);

				view.findViewById(R.id.pending_notify_in_details).setVisibility(transaction.status.equals(Constants.PENDING) ? View.VISIBLE : View.GONE);
			}
		});

		viewModel.getAction().observe(getViewLifecycleOwner(), action -> {
			if (action != null)
				((TextView) view.findViewById(R.id.details_network)).setText(action.network_name);
		});

		viewModel.getContact().observe(getViewLifecycleOwner(), contact -> {
			if (contact != null)
				((Stax2LineItem) view.findViewById(R.id.details_recipient)).setContact(contact);
		});

		RecyclerView messagesView = view.findViewById(R.id.convo_recyclerView);
		messagesView.setLayoutManager(UIHelper.setMainLinearManagers(view.getContext()));
		messagesView.setHasFixedSize(true);
		viewModel.getMessages().observe(getViewLifecycleOwner(), ussdCallResponses -> {
			if (ussdCallResponses != null)
				messagesView.setAdapter(new MessagesAdapter(ussdCallResponses));
		});

		RecyclerView smsView = view.findViewById(R.id.sms_recyclerView);
		smsView.setLayoutManager(UIHelper.setMainLinearManagers(view.getContext()));
		smsView.setHasFixedSize(true);
		viewModel.getSms().observe(getViewLifecycleOwner(), smses -> {
			if (smses != null) smsView.setAdapter(new MessagesAdapter(smses));
		});

		viewModel.setTransaction(getArguments().getString(TransactionContract.COLUMN_UUID));
	}
}
