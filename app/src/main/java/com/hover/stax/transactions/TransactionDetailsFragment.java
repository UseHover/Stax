package com.hover.stax.transactions;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.hover.sdk.actions.HoverAction;
import com.hover.sdk.transactions.Transaction;
import com.hover.sdk.transactions.TransactionContract;
import com.hover.stax.R;
import com.hover.stax.bounties.BountyActivity;
import com.hover.stax.contacts.StaxContact;
import com.hover.stax.databinding.FragmentTransactionBinding;
import com.hover.stax.navigation.NavigationInterface;
import com.hover.stax.utils.DateUtils;
import com.hover.stax.utils.UIHelper;
import com.hover.stax.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

public class TransactionDetailsFragment extends Fragment implements NavigationInterface {

    final public static String TAG = "TransDetailsFragment";
    final public static String SHOW_BOUNTY_SUBMIT = "bounty_submit_button";

    private TransactionDetailsViewModel viewModel;

    private FragmentTransactionBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(requireActivity()).get(TransactionDetailsViewModel.class);
        JSONObject data = new JSONObject();
        try {
            assert getArguments() != null;
            data.put("uuid", getArguments().getString(TransactionContract.COLUMN_UUID));
        } catch (JSONException e) {
            Utils.logErrorAndReportToFirebase(TAG, e.getMessage(), e);
        }

        Utils.logAnalyticsEvent(getString(R.string.visit_screen, getString(R.string.visit_transaction)), data, requireContext());

        binding = FragmentTransactionBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        startObservers();
        setUssdSessionMessagesRecyclerView();
        viewModel.setTransaction(getArguments().getString(TransactionContract.COLUMN_UUID));
    }

    private void setUssdSessionMessagesRecyclerView() {
        setUSSDMessagesRecyclerView();
        setSmsMessagesRecyclerView();
    }

    private void startObservers() {
        viewModel.getTransaction().observe(getViewLifecycleOwner(), this::showTransaction);
        viewModel.getAction().observe(getViewLifecycleOwner(), this::showActionDetails);
        viewModel.getContact().observe(getViewLifecycleOwner(), this::updateRecipient);
    }

    private void setupRetryBountyButton() {
        RelativeLayout bountyButtonsLayout = binding.retrySubmit.bountyRetryButtonLayoutId;
        AppCompatButton retryButton = binding.retrySubmit.btnRetry;

        bountyButtonsLayout.setVisibility(View.VISIBLE);
        retryButton.setOnClickListener(this::retryBountyClicked);
    }

    private void showTransaction(StaxTransaction transaction) {
        if (transaction != null) {
            if (transaction.isRecorded()) setupRetryBountyButton();
            updateDetails(transaction);
            showNotificationCard(transaction.isRecorded() || transaction.status.equals(Transaction.PENDING));
            if (transaction.isRecorded() && viewModel.getAction().getValue() != null)
                updateNotificationCard(viewModel.getAction().getValue());
        }
    }

    @SuppressLint("SetTextI18n")
    private void updateDetails(StaxTransaction transaction) {
        binding.transactionDetailsCard.setTitle(transaction.description);
        binding.detailsRecipientLabel.setText(transaction.transaction_type.equals(HoverAction.RECEIVE) ? R.string.sender_label : R.string.recipient_label);
        binding.detailsAmount.setText(transaction.getDisplayAmount());
        binding.detailsDate.setText(DateUtils.humanFriendlyDate(transaction.initiated_at));

        if (transaction.confirm_code != null && !transaction.confirm_code.isEmpty())
            binding.detailsTransactionNumber.setText(transaction.confirm_code);
        else
            binding.detailsTransactionNumber.setText(transaction.uuid);

        if (transaction.isRecorded()) hideDetails();
    }

    private void hideDetails() {
        binding.amountRow.setVisibility(View.GONE);
        binding.recipientRow.setVisibility(View.GONE);
        binding.recipAccountRow.setVisibility(View.GONE);
    }

    private void showActionDetails(HoverAction action) {
        if (action != null) {
            binding.detailsNetwork.setText(action.from_institution_name);
            if (viewModel.getTransaction().getValue() != null && viewModel.getTransaction().getValue().isRecorded())
                updateNotificationCard(action);
        }
    }

    private void showNotificationCard(boolean show) {
        binding.notificationCard.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @SuppressLint("ResourceAsColor")
    private void updateNotificationCard(HoverAction action) {
        binding.notificationCard.setBackgroundColor(action.bounty_is_open ? R.color.pending_brown : R.color.muted_green);
        binding.notificationCard.setTitle(R.string.checking_your_flow);
        binding.notificationCard.setIcon(action.bounty_is_open ? R.drawable.ic_warning : R.drawable.ic_check);
        binding.notificationDetail.setText(Html.fromHtml(action.bounty_is_open ? getString(R.string.bounty_flow_pending_dialog_msg) : getString(R.string.flow_done_desc)));
    }

    private void updateRecipient(StaxContact contact) {
        if (contact != null)
            binding.detailsRecipient.setContact(contact);
    }

    private void setUSSDMessagesRecyclerView() {
        RecyclerView messagesView = binding.convoRecyclerView;
        messagesView.setLayoutManager(UIHelper.setMainLinearManagers(requireActivity()));
        messagesView.setHasFixedSize(true);

        viewModel.getMessages().observe(getViewLifecycleOwner(), ussdCallResponses -> {
            if (ussdCallResponses != null)
                messagesView.setAdapter(new MessagesAdapter(ussdCallResponses));
        });
    }

    private void setSmsMessagesRecyclerView() {
        RecyclerView smsView = binding.smsRecyclerView;
        smsView.setLayoutManager(UIHelper.setMainLinearManagers(requireActivity()));
        smsView.setHasFixedSize(true);
        viewModel.getSms().observe(getViewLifecycleOwner(), smses -> {
            if (smses != null) smsView.setAdapter(new MessagesAdapter(smses));
        });
    }

    private void retryBountyClicked(View v) {
        if (viewModel.getTransaction().getValue() != null)
            ((BountyActivity) requireActivity()).retryCall(viewModel.getTransaction().getValue().action_id);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        binding = null;
    }
}
