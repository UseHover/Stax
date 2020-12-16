package com.hover.stax.transfers;

import android.content.Intent;
import android.os.Bundle;
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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputLayout;
import com.hover.stax.R;
import com.hover.stax.actions.Action;
import com.hover.stax.channels.Channel;
import com.hover.stax.channels.ChannelsActivity;
import com.hover.stax.contacts.StaxContact;
import com.hover.stax.contacts.StaxContactArrayAdapter;
import com.hover.stax.database.Constants;
import com.hover.stax.utils.StagedFragment;
import com.hover.stax.utils.UIHelper;
import com.hover.stax.utils.Utils;
import com.hover.stax.views.Stax2LineItem;
import com.hover.stax.views.StaxDialog;

import static com.hover.stax.transfers.TransferStage.*;

import java.util.List;

public class TransferFragment extends StagedFragment {
	private static final String TAG = "TransferFragment";

	private TransferViewModel transferViewModel;

	private RelativeLayout recipientEntry;
	private TextInputLayout recipientLabel, amountEntry;
	private EditText amountInput, noteInput;
	private AutoCompleteTextView actionDropdown, recipientAutocomplete;
	private ImageButton contactButton;

	private TextView amountValue, noteValue;
	private Stax2LineItem recipientValue;

	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		stagedViewModel = new ViewModelProvider(requireActivity()).get(TransferViewModel.class);
		transferViewModel = (TransferViewModel) stagedViewModel;
		View root = inflater.inflate(R.layout.fragment_transfer, container, false);
		init(root);
		return root;
	}

	protected void init(View root) {
		setTitle(root);
		amountValue = root.findViewById(R.id.amountValue);
		recipientValue = root.findViewById(R.id.recipientValue);
		noteValue = root.findViewById(R.id.reasonValue);

		amountEntry = root.findViewById(R.id.amountEntry);
		amountInput = root.findViewById(R.id.amount_input);
		amountInput.setText(transferViewModel.getAmount().getValue());
		actionDropdown = root.findViewById(R.id.networkDropdown);
		recipientEntry = root.findViewById(R.id.recipientEntry);
		recipientLabel = root.findViewById(R.id.recipientLabel);
		recipientAutocomplete = root.findViewById(R.id.recipient_autocomplete);
		contactButton = root.findViewById(R.id.contact_button);
		noteInput = root.findViewById(R.id.note_input);
		noteInput.setText(transferViewModel.getNote().getValue());

		super.init(root);
	}

	private void setTitle(View root) {
		TextView tv = root.findViewById(R.id.summaryCard).findViewById(R.id.title);
		if(tv !=null) { tv.setText(getString(transferViewModel.getType().equals(Action.AIRTIME) ? R.string.fab_airtime : R.string.fab_transfer)); }
	}

	protected void startObservers(View root) {
		super.startObservers(root);

		transferViewModel.getStage().observe(getViewLifecycleOwner(), stage -> {
			switch ((TransferStage) stage) {
				case AMOUNT: amountInput.requestFocus(); break;
				case RECIPIENT: recipientAutocomplete.showDropDown(); recipientAutocomplete.requestFocus(); break;
				case NOTE: noteInput.requestFocus(); break;
				default: root.findViewById(R.id.mainLayout).requestFocus();
			}
		});

		transferViewModel.getAmount().observe(getViewLifecycleOwner(), amount -> amountValue.setText(Utils.formatAmount(amount)));
		transferViewModel.getAmountError().observe(getViewLifecycleOwner(), amountError -> {
			amountEntry.setError((amountError != null ? getString(amountError) : null));
			amountEntry.setErrorIconDrawable(0);
		});

		transferViewModel.getRecentContacts().observe(getViewLifecycleOwner(), contacts -> {
			ArrayAdapter<StaxContact> adapter = new StaxContactArrayAdapter(requireActivity(), contacts);
			recipientAutocomplete.setAdapter(adapter);
			if (transferViewModel.getContact().getValue() != null)
				recipientAutocomplete.setText(transferViewModel.getContact().getValue().toString());
		});
		transferViewModel.getContact().observe(getViewLifecycleOwner(), contact -> {
			recipientValue.setContact(contact, transferViewModel.getRequest().getValue() != null && transferViewModel.getRequest().getValue().hasRequesterInfo());
		});

		transferViewModel.getRecipientError().observe(getViewLifecycleOwner(), recipientError -> {
			recipientLabel.setError((recipientError != null ? getString(recipientError) : null));
			recipientLabel.setErrorIconDrawable(0);
		});

		transferViewModel.getPageError().observe(getViewLifecycleOwner(), error -> {
			if (error != null) {
				if ((transferViewModel.isDone()) && getActivity() != null)
					new StaxDialog(getActivity()).setDialogMessage(error).setPosButton(R.string.btn_ok, null).showIt();
				else
					UIHelper.flashMessage(getContext(), getString(error));
			}
		});

		transferViewModel.getNote().observe(getViewLifecycleOwner(), reason -> noteValue.setText(reason));

		transferViewModel.getActions().observe(getViewLifecycleOwner(), actions -> {
			Log.e(TAG, "actions: " + actions.size());
			if (actions == null || actions.size() == 0) return;
			for (Action a: actions) a.context = getContext();
			ArrayAdapter<Action> adapter = new ArrayAdapter<>(requireActivity(), R.layout.stax_spinner_item, actions);
			actionDropdown.setAdapter(adapter);
			actionDropdown.setText(actionDropdown.getAdapter().getItem(0).toString(), false);
		});

		transferViewModel.getActiveAction().observe(getViewLifecycleOwner(), action -> {
			if (action != null) {
				accountValue.setSubtitle(action.getNetworkSubtitle(getContext()));
				if (!action.requiresRecipient())
					recipientValue.setContent(action.getLabel(getContext()), "");
				else if (transferViewModel.getContact().getValue() != null)
					recipientValue.setContact(transferViewModel.getContact().getValue(), transferViewModel.getRequest().getValue() != null);
			}
		});
	}

	protected void startListeners(View root) {
		super.startListeners(root);
		root.findViewById(R.id.add_new_account).setOnClickListener(view -> startActivityForResult(new Intent(getActivity(), ChannelsActivity.class), Constants.ADD_SERVICE));

		actionDropdown.setOnItemClickListener((adapterView, view, pos, id) -> {
			Action action = (Action) adapterView.getItemAtPosition(pos);
			transferViewModel.setActiveAction(action);
			setRecipientHint(action);
		});

		recipientAutocomplete.setOnItemClickListener((adapterView, view, pos, id) -> {
			StaxContact contact = (StaxContact) adapterView.getItemAtPosition(pos);
			transferViewModel.setContact(contact);
		});

		amountInput.addTextChangedListener(amountWatcher);
		recipientAutocomplete.addTextChangedListener(recipientWatcher);
		contactButton.setOnClickListener(view -> contactPicker(Constants.GET_CONTACT, view.getContext()));
		noteInput.addTextChangedListener(noteWatcher);
	}

	private void setRecipientHint(Action action) {
		if (action.getRequiredParams().contains(Action.ACCOUNT_KEY)) {
			recipientLabel.setHint(getString(R.string.recipientacct_label));
		} else {
			recipientLabel.setHint(getString(R.string.recipientphone_label));
		}
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
}