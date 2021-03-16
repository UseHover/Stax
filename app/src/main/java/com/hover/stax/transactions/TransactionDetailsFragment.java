package com.hover.stax.transactions;

import android.annotation.SuppressLint;
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
import com.hover.sdk.actions.HoverAction;
import com.hover.sdk.transactions.Transaction;
import com.hover.sdk.transactions.TransactionContract;
import com.hover.stax.R;
import com.hover.stax.contacts.StaxContact;
import com.hover.stax.utils.DateUtils;
import com.hover.stax.utils.UIHelper;
import com.hover.stax.utils.Utils;
import com.hover.stax.views.Stax2LineItem;
import com.hover.stax.views.StaxCardView;

import org.json.JSONException;
import org.json.JSONObject;

public class TransactionDetailsFragment extends Fragment {
	final public static String TAG = "TransDetailsFragment";

	private TransactionDetailsViewModel viewModel;

	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		viewModel = new ViewModelProvider(requireActivity()).get(TransactionDetailsViewModel.class);
		JSONObject data = new JSONObject();
		try {
			assert getArguments() != null;
			data.put("uuid", getArguments().getString(TransactionContract.COLUMN_UUID)); }
		catch (JSONException e) { Utils.logErrorAndReportToFirebase(TAG,e.getMessage(), e); }

		Amplitude.getInstance().logEvent(getString(R.string.visit_screen, getString(R.string.visit_transaction)), data);
		return inflater.inflate(R.layout.fragment_transaction, container, false);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		viewModel.getTransaction().observe(getViewLifecycleOwner(), transaction -> showTransaction(transaction, view));
		viewModel.getAction().observe(getViewLifecycleOwner(), action -> showActionDetails(action, view));
		viewModel.getContact().observe(getViewLifecycleOwner(), contact -> updateRecipient(contact, view));
		setUSSDMessagesRecyclerView(view);
		setSmsMessagesRecyclerView(view);
		viewModel.setTransaction(getArguments().getString(TransactionContract.COLUMN_UUID));
	}

	private void showTransaction(StaxTransaction transaction, View view) {
		if (transaction != null) {
			updateDetails(view, transaction);
			showNotificationCard(transaction.isRecorded() || transaction.status.equals(Transaction.PENDING), view);
			if (transaction.isRecorded() && viewModel.getAction().getValue() != null)
				updateNotificationCard(viewModel.getAction().getValue(), view);
		}
	}

	@SuppressLint("SetTextI18n")
	private void updateDetails(View view, StaxTransaction transaction) {
		((TextView) view.findViewById(R.id.title)).setText(transaction.description);
		((TextView) view.findViewById(R.id.details_amount)).setText((transaction.transaction_type.equals(HoverAction.RECEIVE) ? "" : "-") + Utils.formatAmount(transaction.amount));
		((TextView) view.findViewById(R.id.details_date)).setText(DateUtils.humanFriendlyDate(transaction.initiated_at));

		if (transaction.confirm_code != null && !transaction.confirm_code.isEmpty())
			((TextView) view.findViewById(R.id.details_transactionNumber)).setText(transaction.confirm_code);
		else
			((TextView) view.findViewById(R.id.details_transactionNumber)).setText(transaction.uuid);

		if (transaction.isRecorded()) hideDetails(view);
	}

	private void hideDetails(View view) {
		view.findViewById(R.id.amountRow).setVisibility(View.GONE);
		view.findViewById(R.id.recipientRow).setVisibility(View.GONE);
		view.findViewById(R.id.recipAccountRow).setVisibility(View.GONE);
	}

	private void showActionDetails(HoverAction action, View view) {
		if (action != null) {
			((TextView) view.findViewById(R.id.details_network)).setText(action.network_name);
			if (viewModel.getTransaction().getValue() != null && viewModel.getTransaction().getValue().isRecorded())
				updateNotificationCard(action, view);
		}
	}

	private void showNotificationCard(boolean show, View view) {
			view.findViewById(R.id.notification_card).setVisibility(show ? View.VISIBLE : View.GONE);
	}

	@SuppressLint("ResourceAsColor")
	private void updateNotificationCard(HoverAction action, View view) {
		view.findViewById(R.id.notification_card).setBackgroundColor(action.bounty_is_open ? R.color.pending_brown : R.color.muted_green);
		((StaxCardView) view.findViewById(R.id.notification_card)).setTitle(R.string.checking_your_flow);
		((StaxCardView) view.findViewById(R.id.notification_card)).setIcon(action.bounty_is_open ? R.drawable.ic_warning : R.drawable.ic_check);
		((TextView) view.findViewById(R.id.notification_detail)).setText(action.bounty_is_open ? R.string.bounty_flow_pending_dialog_msg : R.string.flow_done_desc);
	}

	private void updateRecipient(StaxContact contact, View view){
		if (contact != null)
			((Stax2LineItem) view.findViewById(R.id.details_recipient)).setContact(contact);
	}

	private void setUSSDMessagesRecyclerView(View view){
		RecyclerView messagesView = view.findViewById(R.id.convo_recyclerView);
		messagesView.setLayoutManager(UIHelper.setMainLinearManagers(view.getContext()));
		messagesView.setHasFixedSize(true);

		viewModel.getMessages().observe(getViewLifecycleOwner(), ussdCallResponses -> {
			if (ussdCallResponses != null)
				messagesView.setAdapter(new MessagesAdapter(ussdCallResponses));
		});
	}

	private void setSmsMessagesRecyclerView(View view) {
		RecyclerView smsView = view.findViewById(R.id.sms_recyclerView);
		smsView.setLayoutManager(UIHelper.setMainLinearManagers(view.getContext()));
		smsView.setHasFixedSize(true);
		viewModel.getSms().observe(getViewLifecycleOwner(), smses -> {
			if (smses != null) smsView.setAdapter(new MessagesAdapter(smses));
		});
	}
}
