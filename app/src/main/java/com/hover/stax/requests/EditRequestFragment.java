package com.hover.stax.requests;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.RecyclerView;

import com.hover.stax.R;
import com.hover.stax.channels.Channel;
import com.hover.stax.transfers.StaxContactModel;
import com.hover.stax.utils.EditStagedFragment;
import com.hover.stax.utils.UIHelper;

public class EditRequestFragment extends EditStagedFragment implements RecipientAdapter.UpdateListener {

	protected NewRequestViewModel requestViewModel;

	private RecyclerView recipientInputList;
	private EditText amountInput, receivingAccountNumberInput, noteInput;
	private AutoCompleteTextView channelDropdown;

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		stagedViewModel = new ViewModelProvider(requireActivity()).get(NewRequestViewModel.class);
		requestViewModel = (NewRequestViewModel) stagedViewModel;
		View view = inflater.inflate(R.layout.fragment_edit_request, container, false);
		init(view);
		return view;
	}

	@Override
	protected void init(View view) {
		recipientInputList = view.findViewById(R.id.recipient_list);
		recipientInputList.setLayoutManager(UIHelper.setMainLinearManagers(getContext()));
		RecipientAdapter recipientAdapter = new RecipientAdapter(requestViewModel.getRecipients().getValue(), this);
		recipientInputList.setAdapter(recipientAdapter);

		amountInput = view.findViewById(R.id.amount_input);
		amountInput.setText(requestViewModel.getAmount().getValue());
		channelDropdown = view.findViewById(R.id.channelDropdown);
		if (requestViewModel.getActiveChannel().getValue() != null) channelDropdown.setText(requestViewModel.getActiveChannel().getValue().toString());
		receivingAccountNumberInput = view.findViewById(R.id.accountNumber_input);
		receivingAccountNumberInput.setText(requestViewModel.getReceivingAccountNumber().getValue());
		noteInput = view.findViewById(R.id.note_input);
		noteInput.setText(requestViewModel.getNote().getValue());

		super.init(view);
	}

	protected void save() {
		requestViewModel.resetRecipients();
		for (int c = 0; c < recipientInputList.getChildCount(); c++)
			requestViewModel.addRecipient(
				((TextView) recipientInputList.getChildAt(c).findViewById(R.id.recipient_input)).getText().toString());
		if (!amountInput.getText().toString().isEmpty())
			requestViewModel.setAmount(amountInput.getText().toString());
		if (!receivingAccountNumberInput.getText().toString().isEmpty())
			requestViewModel.setReceivingAccountNumber(receivingAccountNumberInput.getText().toString());
		if (!noteInput.getText().toString().isEmpty())
			requestViewModel.setNote(noteInput.getText().toString());
		NavHostFragment.findNavController(this).navigate(R.id.navigation_new);
	}

	@Override
	protected void startObservers(View root) {
		super.startObservers(root);

		requestViewModel.getSelectedChannels().observe(getViewLifecycleOwner(), channels -> {
			if (channels == null || channels.size() == 0) return;
			ArrayAdapter<Channel> adapter = new ArrayAdapter<>(requireActivity(), R.layout.stax_spinner_item, channels);
			channelDropdown.setAdapter(adapter);
			channelDropdown.setText(channelDropdown.getAdapter().getItem(0).toString(), false);
		});
		requestViewModel.getActiveChannel().observe(getViewLifecycleOwner(), c -> {
			channelDropdown.setText(c.toString(), false);
		});
	}

	@Override
	protected void startListeners(View root) {
		super.startListeners(root);
		channelDropdown.setOnItemClickListener((adapterView, view, pos, id) -> {
			Channel channel = (Channel) adapterView.getItemAtPosition(pos);
			requestViewModel.setActiveChannel(channel.id);
		});
	}

	@Override
	public void onUpdate(int pos, String recipient) { }

	@Override
	public void onClickContact(int index, Context c) { contactPicker(index, c); }

	protected void onContactSelected(int requestCode, StaxContactModel contact) {
		((TextView) recipientInputList.getChildAt(requestCode).findViewById(R.id.recipient_input)).setText(contact.getPhoneNumber());
	}
}
