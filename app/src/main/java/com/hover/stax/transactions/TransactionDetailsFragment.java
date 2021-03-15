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
import com.hover.sdk.transactions.TransactionContract;
import com.hover.stax.R;
import com.hover.stax.actions.Action;
import com.hover.stax.utils.Constants;
import com.hover.stax.utils.DateUtils;
import com.hover.stax.utils.UIHelper;
import com.hover.stax.utils.Utils;
import com.hover.stax.views.Stax2LineItem;
import com.hover.stax.views.StaxCardView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

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
		setHeaderAndNormalActionPendingCardsObserver(view);
		setActionForNetworkNameAndBountyActionPendingCardObserver(view);
		setContactObserver(view);
		setUSSDMessagesRecyclerView(view);
		setSmsMessagesRecyclerView(view);
		setTransactionUUID();
	}

	@SuppressLint("SetTextI18n")
	private void setHeaderTexts(View view, StaxTransaction transaction) {
		((TextView) view.findViewById(R.id.title)).setText(transaction.description);
		((TextView) view.findViewById(R.id.details_amount)).setText((transaction.transaction_type.equals(Action.RECEIVE) ? "" : "-") + Utils.formatAmount(transaction.amount));
		((TextView) view.findViewById(R.id.details_date)).setText(DateUtils.humanFriendlyDate(transaction.initiated_at));

		if (transaction.confirm_code != null && !transaction.confirm_code.isEmpty())
			((TextView) view.findViewById(R.id.details_transactionNumber)).setText(transaction.confirm_code);
		else
			((TextView) view.findViewById(R.id.details_transactionNumber)).setText(transaction.uuid);
	}


	private void setActionForNetworkNameAndBountyActionPendingCardObserver(View view) {
		viewModel.getAction().observe(getViewLifecycleOwner(), action -> {
			if (action != null) {
				((TextView) view.findViewById(R.id.details_network)).setText(action.network_name);
				if(action.bounty_amount> 0) {
					StaxCardView pendingCard =  view.findViewById(R.id.pending_notify_in_details);
					pendingCard.setVisibility(View.VISIBLE);
					TextView pendingStatusText = view.findViewById(R.id.transaction_Detail_pendingText);
					setPendingCardBgAndText(action.bounty_is_open == 0, pendingCard, pendingStatusText);
				}
			}
		});
	}

	private void setNormalActionPendingStateCard(View view, StaxTransaction transaction) {
		if(!transaction.is_action_bounty) {
			view.findViewById(R.id.pending_notify_in_details)
					.setVisibility(transaction.status.equals(Constants.PENDING) ? View.VISIBLE : View.GONE);
		}
	}
	private void setHeaderAndNormalActionPendingCardsObserver(View view) {
		viewModel.getTransaction().observe(getViewLifecycleOwner(), transaction -> {
			if (transaction != null) {
				setHeaderTexts(view, transaction);
				setNormalActionPendingStateCard(view, transaction);
			}
		});
	}

	@SuppressLint("ResourceAsColor")
	private void setPendingCardBgAndText(boolean isFlowDone, StaxCardView pendingCard, TextView pendingStatusText) {
		if(isFlowDone){
			pendingCard.setBackgroundColor(R.color.muted_green);
			pendingCard.setBackDrawableNonClickable(R.drawable.ic_check);
			UIHelper.setTextWithDrawable(pendingStatusText,
					getString(R.string.flow_done_desc),
					0,
					View.VISIBLE);
		}
		else {
			pendingCard.setBackgroundColor(R.color.pending_brown);
			pendingCard.setBackDrawableNonClickable(R.drawable.ic_warning);
			UIHelper.setTextWithDrawable(pendingStatusText,
					getString(R.string.bounty_flow_pending_dialog_msg),
					0,
					View.VISIBLE);
		}
	}

	private void setContactObserver(View view){
		viewModel.getContact().observe(getViewLifecycleOwner(), contact -> {
			if (contact != null)
				((Stax2LineItem) view.findViewById(R.id.details_recipient)).setContact(contact);
		});
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
	private void setTransactionUUID() {
		viewModel.setTransaction(getArguments().getString(TransactionContract.COLUMN_UUID));
	}

}
