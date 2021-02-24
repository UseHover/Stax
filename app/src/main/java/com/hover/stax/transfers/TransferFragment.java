package com.hover.stax.transfers;

import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import com.amplitude.api.Amplitude;
import com.hover.stax.R;
import com.hover.stax.actions.Action;
import com.hover.stax.actions.ActionSelect;
import com.hover.stax.actions.ActionSelectViewModel;
import com.hover.stax.channels.ChannelDropdown;
import com.hover.stax.channels.ChannelDropdownViewModel;
import com.hover.stax.contacts.StaxContact;
import com.hover.stax.contacts.StaxContactArrayAdapter;
import com.hover.stax.utils.Constants;
import com.hover.stax.requests.Request;
import com.hover.stax.utils.UIHelper;
import com.hover.stax.utils.Utils;
import com.hover.stax.views.StaxTextInputLayout;
import com.hover.stax.views.Stax2LineItem;

public class TransferFragment extends AbstractFormFragment implements ActionSelect.HighlightListener {
	private static final String TAG = "TransferFragment";

	private TransferViewModel transferViewModel;
	private ActionSelectViewModel actionSelectViewModel;

	private EditText amountInput, noteInput;
	private ActionSelect actionSelect;
	private StaxTextInputLayout recipientLabel;
	private ChannelDropdown channelDropdown;
	private AutoCompleteTextView recipientAutocomplete;
	private ImageButton contactButton;
	private Stax2LineItem recipientValue;
	private StaxTextInputLayout amountEntry;

	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		channelDropdownViewModel = new ViewModelProvider(requireActivity()).get(ChannelDropdownViewModel.class);
		actionSelectViewModel = new ViewModelProvider(requireActivity()).get(ActionSelectViewModel.class);
		abstractFormViewModel = new ViewModelProvider(requireActivity()).get(TransferViewModel.class);
		transferViewModel = (TransferViewModel) abstractFormViewModel;

		View root = inflater.inflate(R.layout.fragment_transfer, container, false);
		init(root);
		startObservers(root);
		startListeners();
		return root;
	}

	@Override
	protected void init(View root) {
		setTitle(root);

		recipientValue = root.findViewById(R.id.recipientValue);
		amountEntry = root.findViewById(R.id.amountEntry);
		amountInput = amountEntry.findViewById(R.id.amount_input);
		actionSelect = root.findViewById(R.id.action_select);
		recipientLabel = root.findViewById(R.id.recipientLabel);
		channelDropdown = root.findViewById(R.id.channel_dropdown);
		recipientAutocomplete = recipientLabel.findViewById(R.id.recipient_autocomplete);
		contactButton = root.findViewById(R.id.contact_button);
		noteInput = root.findViewById(R.id.note_input);

		amountInput.setText(transferViewModel.getAmount().getValue());
		noteInput.setText(transferViewModel.getNote().getValue());

		amountInput.requestFocus();

		super.init(root);
	}

	private void setTitle(View root) {
		TextView formCardTitle = root.findViewById(R.id.editCard).findViewById(R.id.title);
		TextView summaryCardTitle = root.findViewById(R.id.summaryCard).findViewById(R.id.title);
		if (summaryCardTitle != null) { summaryCardTitle.setText(getString(transferViewModel.getType().equals(Action.AIRTIME) ? R.string.cta_airtime : R.string.cta_transfer)); }
		if (formCardTitle != null) { formCardTitle.setText(getString(transferViewModel.getType().equals(Action.AIRTIME) ? R.string.cta_airtime : R.string.cta_transfer)); }
	}

	@Override
	protected void startObservers(View root) {
		super.startObservers(root);
		actionSelectViewModel.getActiveAction().observe(getViewLifecycleOwner(), action -> {
			Log.e(TAG, "observed active action update");
			((Stax2LineItem) root.findViewById(R.id.account_value)).setSubtitle(action.getNetworkSubtitle(root.getContext()));
			actionSelect.selectRecipientNetwork(action);
			setRecipientHint(action);
		});

		channelDropdownViewModel.getActiveChannel().observe(getViewLifecycleOwner(), channel -> {
			actionSelect.setVisibility(channel == null ? View.GONE : View.VISIBLE);
			((Stax2LineItem) root.findViewById(R.id.account_value)).setTitle(channel.toString());
		});

		channelDropdownViewModel.getChannelActions().observe(getViewLifecycleOwner(), actions -> {
			actionSelectViewModel.setActions(actions);
			actionSelect.updateActions(actions);
		});

		actionSelectViewModel.getActiveActionError().observe(getViewLifecycleOwner(), error -> actionSelect.setError(error));

		transferViewModel.getAmount().observe(getViewLifecycleOwner(), amount -> ((TextView) root.findViewById(R.id.amountValue)).setText(Utils.formatAmount(amount)));
		transferViewModel.getAmountError().observe(getViewLifecycleOwner(), amountError -> {
			amountEntry.setError((amountError != null ? getString(amountError) : null));
		});

		transferViewModel.getRecentContacts().observe(getViewLifecycleOwner(), contacts -> {
			ArrayAdapter<StaxContact> adapter = new StaxContactArrayAdapter(requireActivity(), contacts);
			recipientAutocomplete.setAdapter(adapter);
			if (transferViewModel.getContact().getValue() != null)
				recipientAutocomplete.setText(transferViewModel.getContact().getValue().toString());
		});
		transferViewModel.getContact().observe(getViewLifecycleOwner(), contact -> {
			recipientValue.setContact(contact);
		});

		transferViewModel.getRecipientError().observe(getViewLifecycleOwner(), recipientError -> {
			recipientLabel.setError((recipientError != null ? getString(recipientError) : null));
		});

		transferViewModel.getNote().observe(getViewLifecycleOwner(), note -> {
			root.findViewById(R.id.noteRow).setVisibility((note == null || note.isEmpty()) ? View.GONE : View.VISIBLE);
			((TextView) root.findViewById(R.id.noteValue)).setText(note);
		});

		transferViewModel.getRequest().observe(getViewLifecycleOwner(), request -> { if (request != null) load(request); });
	}

	protected void startListeners() {
		actionSelect.setListener(this);

		recipientAutocomplete.setOnItemClickListener((adapterView, view, pos, id) -> {
			StaxContact contact = (StaxContact) adapterView.getItemAtPosition(pos);
			transferViewModel.setContact(contact);
		});

		amountInput.addTextChangedListener(amountWatcher);
		recipientAutocomplete.addTextChangedListener(recipientWatcher);
		contactButton.setOnClickListener(view -> contactPicker(Constants.GET_CONTACT, view.getContext()));
		noteInput.addTextChangedListener(noteWatcher);

		fab.setOnClickListener(v -> fabClicked(v));
	}

	@Override
	public void highlightAction(Action a) { Log.e(TAG, "updating active action"); actionSelectViewModel.setActiveAction(a); }

	private void fabClicked(View v) {
		if (transferViewModel.getIsEditing().getValue()) {
			if (channelDropdownViewModel.validates() & actionSelectViewModel.validates() & transferViewModel.validates(actionSelectViewModel.getActiveAction().getValue())) {
				transferViewModel.saveContact();
				transferViewModel.setEditing(false);
			} else
				UIHelper.flashMessage(getContext(), getString(R.string.toast_pleasefix));
		} else
			((TransferActivity) getActivity()).submit();
	}

	private void setRecipientHint(Action action) {
		Log.e(TAG, "update hint to " + action + ":" + action.getPronoun(getContext()));
		Log.e(TAG, "requires recipient? " + action.requiresRecipient());
		editCard.findViewById(R.id.recipient_entry).setVisibility(action.requiresRecipient() ? View.VISIBLE : View.GONE);
		summaryCard.findViewById(R.id.recipientRow).setVisibility(action.requiresRecipient() ? View.VISIBLE : View.GONE);
		if (!action.requiresRecipient())
			recipientValue.setContent(getString(R.string.self_choice), "");
		else
			recipientLabel.setHint(action.getRequiredParams().contains(Action.ACCOUNT_KEY) ? getString(R.string.recipientacct_label) : getString(R.string.recipientphone_label));
	}

	protected void onContactSelected(int requestCode, StaxContact contact) {
		transferViewModel.setContact(contact);
		recipientAutocomplete.setText(contact.toString());
	}

	private TextWatcher amountWatcher = new TextWatcher() {
		@Override public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
		@Override public void afterTextChanged(Editable editable) { }
		@Override public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
			transferViewModel.setAmount(charSequence.toString());
		}
	};

	private TextWatcher recipientWatcher = new TextWatcher() {
		@Override public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
		@Override public void afterTextChanged(Editable editable) { }
		@Override public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
			transferViewModel.setRecipient(charSequence.toString());
		}
	};

	private TextWatcher noteWatcher = new TextWatcher() {
		@Override public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
		@Override public void afterTextChanged(Editable editable) { }
		@Override public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
			transferViewModel.setNote(charSequence.toString());
		}
	};

	private void load(Request r) {
		channelDropdownViewModel.setChannelFromRequest(r);
		amountInput.setText(r.amount);
		recipientAutocomplete.setText(r.requester_number);
		transferViewModel.setEditing(r.amount == null || r.amount.isEmpty());
		Amplitude.getInstance().logEvent(getString(R.string.loaded_request_link));
	}
}