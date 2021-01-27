package com.hover.stax.transfers;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputLayout;
import com.hover.stax.R;
import com.hover.stax.actions.Action;
import com.hover.stax.contacts.StaxContact;
import com.hover.stax.contacts.StaxContactArrayAdapter;
import com.hover.stax.database.Constants;
import com.hover.stax.utils.StagedFragment;
import com.hover.stax.utils.UIHelper;
import com.hover.stax.utils.Utils;
import com.hover.stax.views.Stax2LineItem;
import com.hover.stax.views.StaxDialog;

public class TransferFragment extends StagedFragment {
	private static final String TAG = "TransferFragment";

	private TransferViewModel transferViewModel;

	private RelativeLayout recipientEntry;
	private TextInputLayout recipientLabel, amountEntry;
	private EditText amountInput, noteInput;
	private RadioGroup actionRadioGroup;
	private AutoCompleteTextView recipientAutocomplete;
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
		actionRadioGroup = root.findViewById(R.id.networkRadioGroup);
		recipientEntry = root.findViewById(R.id.recipientEntry);
		recipientLabel = root.findViewById(R.id.recipientLabel);
		recipientAutocomplete = root.findViewById(R.id.recipient_autocomplete);
		contactButton = root.findViewById(R.id.contact_button);
		noteInput = root.findViewById(R.id.note_input);
		noteInput.setText(transferViewModel.getNote().getValue());

		super.init(root);
	}

	private void setTitle(View root) {
		TextView summaryCardTitle = root.findViewById(R.id.summaryCard).findViewById(R.id.title);
		TextView formCardTitle = root.findViewById(R.id.transactionFormCard).findViewById(R.id.title);
		if(summaryCardTitle !=null) { summaryCardTitle.setText(getString(transferViewModel.getType().equals(Action.AIRTIME) ? R.string.fab_airtime : R.string.fab_transfer)); }
		if(formCardTitle !=null) { formCardTitle.setText(getString(transferViewModel.getType().equals(Action.AIRTIME) ? R.string.fab_airtime : R.string.fab_transfer)); }
	}

	protected void startObservers(View root) {
		super.startObservers(root);

		root.findViewById(R.id.mainLayout).requestFocus();

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
			if (actions == null || actions.size() == 0) return;
			actionRadioGroup.removeAllViews();
			root.findViewById(R.id.networkLabel).setVisibility(View.VISIBLE);
			for (int i = 0; i < actions.size(); i++){
				Action a =  actions.get(i);
				a.context = getContext();
				RadioButton radioButton = (RadioButton) LayoutInflater.from(getContext()).inflate(R.layout.stax_radio_button, null);
				radioButton.setText(a.getLabel(transferViewModel.getType().equals(Action.AIRTIME) ? getContext() : null));
				radioButton.setId(i);
				radioButton.setChecked(i==0);
				actionRadioGroup.addView(radioButton);
			}

		});

		transferViewModel.getActiveAction().observe(getViewLifecycleOwner(), action -> {
			if (action != null) {
				accountValue.setSubtitle(action.getNetworkSubtitle(getContext()));
				root.findViewById(R.id.recipientEntry).setVisibility(action.requiresRecipient() ? View.VISIBLE : View.GONE);
				if (!action.requiresRecipient()) recipientValue.setContent(action.getLabel(getContext()), "");
				else if (transferViewModel.getContact().getValue() != null)
					recipientValue.setContact(transferViewModel.getContact().getValue(), transferViewModel.getRequest().getValue() != null);
			}
		});
	}

	protected void startListeners(View root) {
		super.startListeners(root);

		actionRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
			Action action = transferViewModel.getActions().getValue().get(checkedId);
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