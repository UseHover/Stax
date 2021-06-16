package com.hover.stax.transfers;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import com.hover.sdk.actions.HoverAction;
import com.hover.stax.R;
import com.hover.stax.actions.ActionSelect;
import com.hover.stax.actions.ActionSelectViewModel;
import com.hover.stax.channels.ChannelsViewModel;
import com.hover.stax.contacts.ContactInput;
import com.hover.stax.contacts.StaxContact;
import com.hover.stax.databinding.FragmentTransferBinding;
import com.hover.stax.requests.Request;
import com.hover.stax.utils.Constants;
import com.hover.stax.utils.UIHelper;
import com.hover.stax.utils.Utils;
import com.hover.stax.views.AbstractStatefulInput;
import com.hover.stax.views.Stax2LineItem;
import com.hover.stax.views.StaxTextInputLayout;

import timber.log.Timber;

public class TransferFragment extends AbstractFormFragment implements ActionSelect.HighlightListener {

    private TransferViewModel transferViewModel;
    private ActionSelectViewModel actionSelectViewModel;

    private StaxTextInputLayout amountInput, noteInput;
    private ActionSelect actionSelect;
    private ContactInput contactInput;

    private Stax2LineItem recipientValue;

    private FragmentTransferBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        channelsViewModel = new ViewModelProvider(requireActivity()).get(ChannelsViewModel.class);
        actionSelectViewModel = new ViewModelProvider(requireActivity()).get(ActionSelectViewModel.class);
        abstractFormViewModel = new ViewModelProvider(requireActivity()).get(TransferViewModel.class);
        transferViewModel = (TransferViewModel) abstractFormViewModel;

        binding = FragmentTransferBinding.inflate(inflater, container, false);

        init(binding.getRoot());
        startObservers(binding.getRoot());
        startListeners();
        return binding.getRoot();
    }

    @Override
    protected void init(View root) {
        setTitle();

        amountInput = binding.editCard.amountInput;
        contactInput = binding.editCard.contactSelect;
        actionSelect = binding.editCard.actionSelect;
        noteInput = binding.editCard.noteInput;

        recipientValue = binding.summaryCard.recipientValue;

        amountInput.setText(transferViewModel.getAmount().getValue());
        noteInput.setText(transferViewModel.getNote().getValue());
        amountInput.requestFocus();

        super.init(root);
    }

    private void setTitle(){
        binding.editCard.transferCard.setTitle(getString(transferViewModel.getType().equals(HoverAction.AIRTIME) ? R.string.cta_airtime : R.string.cta_transfer));
        binding.summaryCard.transferSummaryCard.setTitle(getString(transferViewModel.getType().equals(HoverAction.AIRTIME) ? R.string.cta_airtime : R.string.cta_transfer));
    }

    @Override
    protected void startObservers(View root) {
        super.startObservers(root);
        actionSelectViewModel.getActiveAction().observe(getViewLifecycleOwner(), action -> {
            Timber.e("observed active action update");
            binding.summaryCard.accountValue.setSubtitle(action.getNetworkSubtitle(root.getContext()));
            actionSelect.selectRecipientNetwork(action);
            setRecipientHint(action);
        });

        channelsViewModel.getActiveChannel().observe(getViewLifecycleOwner(), channel -> {
            transferViewModel.setRecipientSmartly(transferViewModel.getRequest().getValue(), channel);
            actionSelect.setVisibility(channel == null ? View.GONE : View.VISIBLE);
            binding.summaryCard.accountValue.setTitle(channel.toString());
        });

        channelsViewModel.getChannelActions().observe(getViewLifecycleOwner(), actions -> {
            actionSelectViewModel.setActions(actions);
            actionSelect.updateActions(actions);
        });

        transferViewModel.getAmount().observe(getViewLifecycleOwner(), amount -> binding.summaryCard.amountValue.setText(Utils.formatAmount(amount)));

        transferViewModel.getRecentContacts().observe(getViewLifecycleOwner(), contacts -> {
            contactInput.setRecent(contacts, requireActivity());
            if (transferViewModel.getContact().getValue() != null)
                contactInput.setSelected(transferViewModel.getContact().getValue());
        });
        transferViewModel.getContact().observe(getViewLifecycleOwner(), contact -> recipientValue.setContact(contact));

        transferViewModel.getNote().observe(getViewLifecycleOwner(), note -> {
            binding.summaryCard.noteRow.setVisibility((note == null || note.isEmpty()) ? View.GONE : View.VISIBLE);
            binding.summaryCard.noteValue.setText(note);
        });

        transferViewModel.getRequest().observe(getViewLifecycleOwner(), request -> {
            if (request != null) load(request);
        });
    }

    protected void startListeners() {
        amountInput.addTextChangedListener(amountWatcher);
        amountInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus)
                amountInput.setState(null,
                        transferViewModel.amountErrors() == null ? AbstractStatefulInput.SUCCESS : AbstractStatefulInput.NONE);
        });
        actionSelect.setListener(this);

        contactInput.setOnItemClickListener((adapterView, view, pos, id) -> {
            StaxContact contact = (StaxContact) adapterView.getItemAtPosition(pos);
            transferViewModel.setContact(contact);
        });
        contactInput.addTextChangedListener(recipientWatcher);
        contactInput.setChooseContactListener(view -> contactPicker(Constants.GET_CONTACT, view.getContext()));
        noteInput.addTextChangedListener(noteWatcher);

        fab.setOnClickListener(this::fabClicked);
    }

    @Override
    public void highlightAction(HoverAction a) {
        Timber.e("updating active action");
        actionSelectViewModel.setActiveAction(a);
    }

    private void fabClicked(View v) {
        if (transferViewModel.getIsEditing().getValue()) {
            if (validates()) {
                transferViewModel.saveContact();
                transferViewModel.setEditing(false);
            } else
                UIHelper.flashMessage(requireActivity(), getString(R.string.toast_pleasefix));
        } else
            ((TransferActivity) getActivity()).submit();
    }

    private boolean validates() {
        String amountError = transferViewModel.amountErrors();
        amountInput.setState(amountError, amountError == null ? AbstractStatefulInput.SUCCESS : AbstractStatefulInput.ERROR);
        String channelError = channelsViewModel.errorCheck();
        channelDropdown.setState(channelError, channelError == null ? AbstractStatefulInput.SUCCESS : AbstractStatefulInput.ERROR);
        String actionError = actionSelectViewModel.errorCheck();
        actionSelect.setState(actionError, actionError == null ? AbstractStatefulInput.SUCCESS : AbstractStatefulInput.ERROR);
        String recipientError = transferViewModel.recipientErrors(actionSelectViewModel.getActiveAction().getValue());
        contactInput.setState(recipientError, recipientError == null ? AbstractStatefulInput.SUCCESS : AbstractStatefulInput.ERROR);
        return channelError == null && actionError == null && amountError == null && recipientError == null;
    }

    private void setRecipientHint(HoverAction action) {
        Timber.e("update hint to " + action + ":" + action.getPronoun(getContext()));
        Timber.e("requires recipient? %s", action.requiresRecipient());
        editCard.findViewById(R.id.recipient_entry).setVisibility(action.requiresRecipient() ? View.VISIBLE : View.GONE);
        binding.summaryCard.recipientRow.setVisibility(action.requiresRecipient() ? View.VISIBLE : View.GONE);
        if (!action.requiresRecipient())
            recipientValue.setContent(getString(R.string.self_choice), "");
        else {
			      transferViewModel.forceUpdateContactUI();
            contactInput.setHint(action.getRequiredParams().contains(HoverAction.ACCOUNT_KEY) ? getString(R.string.recipientacct_label) : getString(R.string.recipientphone_label));
        }
    }

    protected void onContactSelected(int requestCode, StaxContact contact) {
        transferViewModel.setContact(contact);
        contactInput.setSelected(contact);
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
            transferViewModel.setAmount(charSequence.toString().replaceAll(",", ""));
        }
    };

    private final TextWatcher recipientWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void afterTextChanged(Editable editable) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            transferViewModel.setRecipient(charSequence.toString());
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
            transferViewModel.setNote(charSequence.toString());
        }
    };

    private void load(Request r) {
        transferViewModel.setRecipientSmartly(r, channelsViewModel.getActiveChannel().getValue());
        channelsViewModel.setChannelFromRequest(r);
        amountInput.setText(r.amount);
        contactInput.setText(r.requester_number, false);
        transferViewModel.setEditing(r.amount == null || r.amount.isEmpty());
        channelDropdown.setState(getString(R.string.channel_request_fieldinfo, String.valueOf(r.requester_institution_id)), AbstractStatefulInput.INFO);
        Utils.logAnalyticsEvent(getString(R.string.loaded_request_link), requireContext());
    }
}