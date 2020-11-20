package com.hover.stax.transfers;

import android.os.Bundle;
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
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.textfield.TextInputLayout;
import com.hover.stax.R;
import com.hover.stax.actions.Action;
import com.hover.stax.channels.Channel;
import com.hover.stax.contacts.StaxContact;
import com.hover.stax.database.Constants;
import com.hover.stax.utils.EditStagedFragment;


public class EditTransferFragment extends EditStagedFragment {

	private RelativeLayout recipientEntry;
	protected TransferViewModel transferViewModel;
	private TextInputLayout actionEntry;
	private EditText amountInput, noteInput;
	private ImageButton contactButton;
	private AutoCompleteTextView channelDropdown, actionDropdown, recipientAutocomplete;

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		stagedViewModel = new ViewModelProvider(requireActivity()).get(TransferViewModel.class);
		transferViewModel = (TransferViewModel) stagedViewModel;
		View view = inflater.inflate(R.layout.fragment_edit_transfer, container, false);
		init(view);
		return view;
	}

	@Override
	protected void init(View view) {
		setTitle(view);
		amountInput = view.findViewById(R.id.amount_input);
		amountInput.setText(transferViewModel.getAmount().getValue());
		channelDropdown = view.findViewById(R.id.channelDropdown);
		if (transferViewModel.getActiveChannel().getValue() != null)
			channelDropdown.setText(transferViewModel.getActiveChannel().getValue().toString());
		actionEntry = view.findViewById(R.id.networkEntry);
		actionDropdown = view.findViewById(R.id.networkDropdown);
		if (transferViewModel.getActiveAction().getValue() != null)
			actionDropdown.setText(transferViewModel.getActiveAction().getValue().toString());
		recipientEntry = view.findViewById(R.id.recipientEntry);
		recipientAutocomplete = view.findViewById(R.id.recipient_autocomplete);
		if (transferViewModel.getContact().getValue() != null)
			recipientAutocomplete.setText(transferViewModel.getContact().getValue().toString());
		contactButton = view.findViewById(R.id.contact_button);
		noteInput = view.findViewById(R.id.note_input);
		noteInput.setText(transferViewModel.getNote().getValue());

		super.init(view);
	}

	private void setTitle(View root) {
		((TextView) root.findViewById(R.id.summaryCard).findViewById(R.id.title)).setText(
			getString(transferViewModel.getType().equals(Action.AIRTIME) ? R.string.editairtime_cardhead : R.string.edittransfer_cardhead));
	}

	protected void startObservers(View root) {
		super.startObservers(root);

		transferViewModel.getActions().observe(getViewLifecycleOwner(), actions -> {
			if (actions == null || actions.size() == 0) return;
			ArrayAdapter<Action> adapter = new ArrayAdapter<>(requireActivity(), R.layout.stax_spinner_item, actions);
			actionDropdown.setAdapter(adapter);
			String current = transferViewModel.getActiveAction().getValue() != null ? transferViewModel.getActiveAction().getValue().toString() : actionDropdown.getAdapter().getItem(0).toString();
			actionDropdown.setText(current, false);
		});
		transferViewModel.getActiveAction().observe(getViewLifecycleOwner(), action -> {
			actionDropdown.setText(action.toString(), false);
			recipientEntry.setVisibility(action.requiresRecipient() ? View.VISIBLE : View.GONE);
		});

		transferViewModel.getSelectedChannels().observe(getViewLifecycleOwner(), channels -> {
			if (channels == null || channels.size() == 0) return;
			ArrayAdapter<Channel> adapter = new ArrayAdapter<>(requireActivity(), R.layout.stax_spinner_item, channels);
			channelDropdown.setAdapter(adapter);
			channelDropdown.setText(channelDropdown.getAdapter().getItem(0).toString(), false);
		});
		transferViewModel.getActiveChannel().observe(getViewLifecycleOwner(), c -> {
			channelDropdown.setText(c.toString(), false);
		});
	}

	protected void startListeners(View root) {
		super.startListeners(root);
		channelDropdown.setOnItemClickListener((adapterView, view, pos, id) -> {
			Channel channel = (Channel) adapterView.getItemAtPosition(pos);
			transferViewModel.setActiveChannel(channel.id);
		});

		actionDropdown.setOnItemClickListener((adapterView, view, pos, id) -> {
			Action action = (Action) adapterView.getItemAtPosition(pos);
			transferViewModel.setActiveAction(action);
		});

		contactButton.setOnClickListener(view -> contactPicker(Constants.GET_CONTACT, view.getContext()));
	}

	protected void save() {
		if (!amountInput.getText().toString().isEmpty())
			transferViewModel.setAmount(amountInput.getText().toString());
		if (!channelDropdown.getText().toString().isEmpty())
			transferViewModel.setActiveChannel(channelDropdown.getText().toString());
		if (!actionDropdown.getText().toString().isEmpty())
			transferViewModel.setActiveAction(actionDropdown.getText().toString());
		if (!recipientAutocomplete.getText().toString().isEmpty() && !recipientAutocomplete.getText().toString().equals(transferViewModel.getContact().toString()))
			transferViewModel.setRecipient(recipientAutocomplete.getText().toString());
		if (!noteInput.getText().toString().isEmpty())
			transferViewModel.setNote(noteInput.getText().toString());
		NavHostFragment.findNavController(this).navigate(R.id.navigation_new);
	}

	protected void onContactSelected(int requestCode, StaxContact contact) {
		transferViewModel.setContact(contact);
		recipientAutocomplete.setText(contact.toString());
	}
}
