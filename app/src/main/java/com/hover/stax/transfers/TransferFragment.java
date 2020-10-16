package com.hover.stax.transfers;

import android.content.Intent;
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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputLayout;
import com.hover.stax.R;
import com.hover.stax.actions.Action;
import com.hover.stax.channels.Channel;
import com.hover.stax.channels.ChannelsActivity;
import com.hover.stax.database.Constants;
import com.hover.stax.utils.StagedFragment;
import com.hover.stax.utils.Utils;

import java.util.List;

public class TransferFragment extends StagedFragment {
	private static final String TAG = "TransferFragment";

	private TransferViewModel transferViewModel;

	private TextInputLayout recipientEntry, amountEntry;
	private EditText amountInput, recipientInput, noteInput;
	private RadioGroup fromRadioGroup;
	private AutoCompleteTextView networkDropdown;
	private ImageButton contactButton;

	private TextView amountValue, fromValue, toNetworkValue, recipientValue, noteValue;

	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		stagedViewModel = new ViewModelProvider(requireActivity()).get(TransferViewModel.class);
		transferViewModel = (TransferViewModel) stagedViewModel;
		View root = inflater.inflate(R.layout.fragment_transfer, container, false);
		init(root);
		return root;
	}

	protected void init(View root) {
		((TextView) root.findViewById(R.id.title)).setText(
				getString(transferViewModel.getType().equals(Action.AIRTIME) ? R.string.buy_airtime : R.string.transfer));
		amountValue = root.findViewById(R.id.amountValue);
		fromValue = root.findViewById(R.id.fromValue);
		toNetworkValue = root.findViewById(R.id.toNetworkValue);
		recipientValue = root.findViewById(R.id.recipientValue);
		noteValue = root.findViewById(R.id.reasonValue);

		amountEntry = root.findViewById(R.id.amountEntry);
		amountInput = root.findViewById(R.id.amount_input);
		fromRadioGroup = root.findViewById(R.id.fromRadioGroup);
		networkDropdown = root.findViewById(R.id.networkDropdown);
		recipientEntry = root.findViewById(R.id.recipientEntry);
		recipientInput = root.findViewById(R.id.recipient_input);
		contactButton = root.findViewById(R.id.contact_button);
		noteInput = root.findViewById(R.id.note_input);

		super.init(root);
	}

	protected void startObservers(View root) {
		super.startObservers(root);

		transferViewModel.getStage().observe(getViewLifecycleOwner(), stage -> {
			switch ((TransferStage) stage) {
				case AMOUNT: amountInput.requestFocus(); break;
				case RECIPIENT: recipientInput.requestFocus(); break;
				case NOTE: noteInput.requestFocus(); break;
			}
		});

		transferViewModel.getAmount().observe(getViewLifecycleOwner(), amount -> amountValue.setText(Utils.formatAmount(amount)));
		transferViewModel.getAmountError().observe(getViewLifecycleOwner(), amountError -> {
			amountEntry.setError((amountError != null ? getString(amountError) : null));
			amountEntry.setErrorIconDrawable(0);
		});

		transferViewModel.getRecipient().observe(getViewLifecycleOwner(), recipient -> recipientValue.setText(recipient));
		transferViewModel.getRecipientError().observe(getViewLifecycleOwner(), recipientError -> {
			recipientEntry.setError((recipientError != null ? getString(recipientError) : null));
			recipientEntry.setErrorIconDrawable(0);
		});

		transferViewModel.getNote().observe(getViewLifecycleOwner(), reason -> noteValue.setText(reason));

		transferViewModel.getActions().observe(getViewLifecycleOwner(), actions -> {
			if (actions == null || actions.size() == 0) return;
			ArrayAdapter<Action> adapter = new ArrayAdapter<>(requireActivity(), R.layout.stax_spinner_item, actions);
			networkDropdown.setAdapter(adapter);
			networkDropdown.setText(networkDropdown.getAdapter().getItem(0).toString(), false);
		});

		transferViewModel.getActiveAction().observe(getViewLifecycleOwner(), action ->
		{
			if (action != null) toNetworkValue.setText(action.toString());
		});

		transferViewModel.getActiveChannel().observe(getViewLifecycleOwner(), c -> {
			if (c != null) {
				fromValue.setText(c.name);
				fromRadioGroup.check(c.id);
			}
		});

		transferViewModel.getSelectedChannels().observe(getViewLifecycleOwner(), channels -> {
			if (channels != null) createChannelSelector(channels);
		});
	}

	private void createChannelSelector(List<Channel> channels) {
		fromRadioGroup.removeAllViews();

		for (Channel c : channels) {
			RadioButton radioButton = (RadioButton) LayoutInflater.from(getContext()).inflate(R.layout.stax_radio_button, null);
			radioButton.setText(c.name);
			radioButton.setId(c.id);
			if (transferViewModel.getActiveChannel().getValue() != null && transferViewModel.getActiveChannel().getValue().id == c.id)
				radioButton.setChecked(true);
			fromRadioGroup.addView(radioButton);
		}
	}

	protected void startListeners(View root) {
		super.startListeners(root);
		fromRadioGroup.setOnCheckedChangeListener((group, checkedId) -> transferViewModel.setActiveChannel(checkedId));
		root.findViewById(R.id.add_new_account).setOnClickListener(view -> startActivity(new Intent(getActivity(), ChannelsActivity.class)));

		networkDropdown.setOnItemClickListener((adapterView, view, pos, id) -> {
			Action action = (Action) adapterView.getItemAtPosition(pos);
			transferViewModel.setActiveAction(action);
			setRecipientHint(action);
			toNetworkValue.setText(action.toString());
		});

		amountInput.addTextChangedListener(amountWatcher);
		recipientInput.addTextChangedListener(recipientWatcher);
		contactButton.setOnClickListener(view -> contactPicker(Constants.GET_CONTACT, view.getContext()));
		noteInput.addTextChangedListener(noteWatcher);
	}

	private void setRecipientHint(Action action) {
		if (action.getRequiredParams().contains(Action.ACCOUNT_KEY)) {
			recipientEntry.setHint(getString(R.string.recipient_account));
		} else {
			recipientEntry.setHint(getString(R.string.recipient_phone));
		}
	}

	protected void onContactSelected(int requestCode, StaxContactModel contact) {
		recipientInput.setText(Utils.normalizePhoneNumber(contact.getPhoneNumber(), transferViewModel.getActiveChannel().getValue().countryAlpha2));
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