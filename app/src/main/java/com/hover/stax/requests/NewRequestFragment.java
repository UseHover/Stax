package com.hover.stax.requests;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.hover.stax.R;
import com.hover.stax.channels.Channel;
import com.hover.stax.channels.ChannelDropdownViewModel;
import com.hover.stax.contacts.ContactInput;
import com.hover.stax.contacts.StaxContact;
import com.hover.stax.databinding.FragmentRequestBinding;

import com.hover.stax.transfers.AbstractFormFragment;
import com.hover.stax.pushNotification.PushNotificationTopicsInterface;

import com.hover.stax.utils.UIHelper;
import com.hover.stax.utils.Utils;
import com.hover.stax.views.AbstractStatefulInput;
import com.hover.stax.views.Stax2LineItem;
import com.hover.stax.views.StaxCardView;
import com.hover.stax.views.StaxTextInputLayout;

public class NewRequestFragment extends AbstractFormFragment implements RecipientAdapter.UpdateListener, PushNotificationTopicsInterface {

    protected NewRequestViewModel requestViewModel;

    private StaxTextInputLayout amountInput, requesterNumberInput, noteInput;
    private RecyclerView recipientInputList;
    private TextView addRecipientBtn;

    private LinearLayout recipientValueList;
    protected Stax2LineItem accountValue;
    private StaxCardView shareCard;

    private RecipientAdapter recipientAdapter;
    private int recipientCount = 0;

    private FragmentRequestBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        channelDropdownViewModel = new ViewModelProvider(requireActivity()).get(ChannelDropdownViewModel.class);
        abstractFormViewModel = new ViewModelProvider(requireActivity()).get(NewRequestViewModel.class);
        requestViewModel = (NewRequestViewModel) abstractFormViewModel;

        binding = FragmentRequestBinding.inflate(inflater, container, false);

        init(binding.getRoot());
        startObservers(binding.getRoot());
        startListeners();
        return binding.getRoot();
    }

    @Override
    protected void init(View view) {
        amountInput = binding.editCard.cardAmount.amountInput;
        recipientInputList = binding.editCard.cardRequestee.recipientList;
        addRecipientBtn = binding.editCard.cardRequestee.addRecipientButton;
        requesterNumberInput = binding.editCard.cardRequester.accountNumberInput;
        noteInput = binding.editCard.transferNote.noteInput;

        recipientValueList = binding.summaryCard.requesteeValueList;
        accountValue = binding.summaryCard.accountValue;
        shareCard = binding.shareCard.getRoot();

        amountInput.setText(requestViewModel.getAmount().getValue());
        recipientInputList.setLayoutManager(UIHelper.setMainLinearManagers(getContext()));
        recipientAdapter = new RecipientAdapter(requestViewModel.getRequestees().getValue(), requestViewModel.getRecentContacts().getValue(), this);
        recipientInputList.setAdapter(recipientAdapter);
        requesterNumberInput.setText(requestViewModel.getRequesterNumber().getValue());
        noteInput.setText(requestViewModel.getNote().getValue());

        super.init(view);
    }

    @Override
    protected void startObservers(View root) {
        super.startObservers(root);
        channelDropdownViewModel.getActiveChannel().observe(getViewLifecycleOwner(), channel -> {
            requestViewModel.setActiveChannel(channel);
            accountValue.setTitle(channel.toString());
        });

        requestViewModel.getActiveChannel().observe(getViewLifecycleOwner(), this::updateAcctNo);

        requestViewModel.getRequestees().observe(getViewLifecycleOwner(), recipients -> {
            if (recipients == null || recipients.size() == 0) return;
            recipientValueList.removeAllViews();
            for (StaxContact recipient : recipients) {
                Stax2LineItem ssi2l = new Stax2LineItem(getContext(), null);
                ssi2l.setContact(recipient);
                recipientValueList.addView(ssi2l);
            }

            if (recipients.size() == recipientCount) return;
            recipientCount = recipients.size();
            recipientAdapter.update(recipients);
        });
        requestViewModel.getRecentContacts().observe(getViewLifecycleOwner(), contacts -> {
            recipientAdapter.updateContactList(contacts);
        });

        requestViewModel.getAmount().observe(getViewLifecycleOwner(), amount -> {
            binding.summaryCard.amountRow.setVisibility(requestViewModel.validAmount() ? View.VISIBLE : View.GONE);
            binding.summaryCard.amountValue.setText(Utils.formatAmount(amount));
        });

        requestViewModel.getRequesterNumber().observe(getViewLifecycleOwner(), accountNumber -> {
            accountValue.setSubtitle(accountNumber);
        });

        requestViewModel.getNote().observe(getViewLifecycleOwner(), note -> {
            binding.summaryCard.noteRow.setVisibility(requestViewModel.validNote() ? View.VISIBLE : View.GONE);
            binding.summaryCard.noteValue.setText(note);
        });


        requestViewModel.getIsEditing().observe(getViewLifecycleOwner(), this::showEdit);
    }

    protected void showEdit(boolean isEditing) {
        super.showEdit(isEditing);
        if (!isEditing) requestViewModel.createRequest();
        shareCard.setVisibility(isEditing ? View.GONE : View.VISIBLE);
        fab.setVisibility(isEditing ? View.VISIBLE : View.GONE);
    }

    protected void updateAcctNo(Channel c) {
        if (c != null)
            requesterNumberInput.setText(c.accountNo);
    }

    protected void startListeners() {
        amountInput.addTextChangedListener(amountWatcher);
        addRecipientBtn.setOnClickListener(v -> requestViewModel.addRecipient(new StaxContact("")));
        requesterNumberInput.addTextChangedListener(receivingAccountNumberWatcher);
        requesterNumberInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus)
                requesterNumberInput.setState(null,
                        requestViewModel.requesterAcctNoError() == null ? AbstractStatefulInput.SUCCESS : AbstractStatefulInput.NONE);
        });
        noteInput.addTextChangedListener(noteWatcher);

        fab.setOnClickListener(this::fabClicked);
    }

    @Override
    public void onUpdate(int pos, StaxContact recipient) {
        requestViewModel.onUpdate(pos, recipient);
    }

    @Override
    public void onClickContact(int index, Context c) {
        contactPicker(index, c);
    }

    protected void onContactSelected(int requestCode, StaxContact contact) {
        requestViewModel.onUpdate(requestCode, contact);
        recipientAdapter.notifyDataSetChanged();
    }

    private final TextWatcher amountWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void afterTextChanged(Editable editable) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            requestViewModel.setAmount(charSequence.toString());
        }
    };

    private final TextWatcher receivingAccountNumberWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void afterTextChanged(Editable s) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            requestViewModel.setRequesterNumber(s.toString());
        }
    };

    private final TextWatcher noteWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void afterTextChanged(Editable editable) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            requestViewModel.setNote(charSequence.toString());
        }
    };

    private void fabClicked(View v) {
        requestViewModel.removeInvalidRequestees();

        if (requestViewModel.getIsEditing().getValue() && validates()) {
            updatePushNotifGroupStatus();
            requestViewModel.setEditing(false);
        } else
            UIHelper.flashMessage(requireActivity(), getString(R.string.toast_pleasefix));
    }
  
  	private void updatePushNotifGroupStatus() {
		joinRequestMoneyGroup(requireContext());
		leaveNoUsageGroup(requireContext());
		leaveNoRequestMoneyGroup(requireContext());
	}

    private boolean validates() {
        String channelError = channelDropdownViewModel.errorCheck();
        channelDropdown.setState(channelError, channelError == null ? AbstractStatefulInput.SUCCESS : AbstractStatefulInput.ERROR);
        String requesterAcctNoError = requestViewModel.requesterAcctNoError();
        requesterNumberInput.setState(requesterAcctNoError, requesterAcctNoError == null ? AbstractStatefulInput.SUCCESS : AbstractStatefulInput.ERROR);
        String recipientError = requestViewModel.requesteeErrors();
        ((ContactInput) recipientInputList.getChildAt(0)).setState(recipientError, recipientError == null ? AbstractStatefulInput.SUCCESS : AbstractStatefulInput.ERROR);
        return channelError == null && requesterAcctNoError == null && recipientError == null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        binding = null;
    }
}
