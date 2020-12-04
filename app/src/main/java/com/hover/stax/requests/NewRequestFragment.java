package com.hover.stax.requests;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputLayout;
import com.hover.stax.R;
import com.hover.stax.channels.Channel;
import com.hover.stax.channels.ChannelsActivity;
import com.hover.stax.contacts.StaxContact;
import com.hover.stax.utils.StagedFragment;
import com.hover.stax.utils.UIHelper;
import com.hover.stax.utils.Utils;
import com.hover.stax.views.Stax2LineItem;

import java.util.List;

public class NewRequestFragment extends StagedFragment implements RecipientAdapter.UpdateListener {

	protected NewRequestViewModel requestViewModel;

	private RecyclerView recipientInputList;
	private LinearLayout recipientValueList;
	private TextView amountValue, noteValue;
	private Stax2LineItem requesterAccountValue;
	private EditText amountInput, receivingAccountNumberInput, noteInput;
	private RadioGroup channelRadioGroup;
	private Button addRecipientBtn;


	private RecipientAdapter recipientAdapter;
	private int recipientCount = 0;

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		stagedViewModel = new ViewModelProvider(requireActivity()).get(NewRequestViewModel.class);
		requestViewModel = (NewRequestViewModel) stagedViewModel;
		View view = inflater.inflate(R.layout.fragment_request, container, false);
		init(view);
		return view;
	}

	@Override
	protected void init(View view) {
		recipientValueList = view.findViewById(R.id.requesteeValueList);
		amountValue = view.findViewById(R.id.amountValue);
		noteValue = view.findViewById(R.id.noteValue);

		requesterAccountValue = view.findViewById(R.id.requester_account_value);

		recipientInputList = view.findViewById(R.id.recipient_list);
		amountInput = view.findViewById(R.id.amount_input);
		amountInput.setText(requestViewModel.getAmount().getValue());
		receivingAccountNumberInput = view.findViewById(R.id.accountNumber_input);
		receivingAccountNumberInput.setText(requestViewModel.getRequesterNumber().getValue());
		channelRadioGroup = view.findViewById(R.id.channelRadioGroup);

		noteInput = view.findViewById(R.id.note_input);
		noteInput.setText(requestViewModel.getNote().getValue());
		addRecipientBtn = view.findViewById(R.id.add_recipient_button);

		recipientInputList.setLayoutManager(UIHelper.setMainLinearManagers(getContext()));
		recipientAdapter = new RecipientAdapter(requestViewModel.getRequestees().getValue(), requestViewModel.getRecentContacts().getValue(), this);
		recipientInputList.setAdapter(recipientAdapter);

		super.init(view);
	}

	@Override
	protected void startObservers(View root) {
		super.startObservers(root);
		requestViewModel.getStage().observe(getViewLifecycleOwner(), stage -> {
			switch ((RequestStage) stage) {
				case AMOUNT: amountInput.requestFocus(); break;
				case REQUESTER: receivingAccountNumberInput.requestFocus(); break;
				case NOTE: noteInput.requestFocus(); break;
			}
		});

		requestViewModel.getRequestees().observe(getViewLifecycleOwner(), recipients -> {
			if (recipients == null || recipients.size() == 0) return;
			recipientValueList.removeAllViews();
			for (StaxContact recipient : recipients) {
				Stax2LineItem ssi2l = new Stax2LineItem(getContext(), null);
				ssi2l.setContact(recipient, false);
				recipientValueList.addView(ssi2l);
			}

			if (recipients.size() == recipientCount) return;
			recipientCount = recipients.size();
			recipientAdapter.update(recipients);
		});
		requestViewModel.getRecentContacts().observe(getViewLifecycleOwner(), contacts -> {
			recipientAdapter.updateContactList(contacts);
		});

		requestViewModel.getRequesteeError().observe(getViewLifecycleOwner(), recipientError -> {
			if (recipientInputList.getChildAt(0) == null) return;
			TextInputLayout v = recipientInputList.getChildAt(0).findViewById(R.id.recipientLabel);
			v.setError((recipientError != null ? getString(recipientError) : null));
			v.setErrorIconDrawable(0);
		});

		requestViewModel.getRequesterNumberError().observe(getViewLifecycleOwner(), accountNumberError->{
			TextInputLayout v = root.findViewById(R.id.accountNumberEntry);
			v.setError((accountNumberError != null ? getString(accountNumberError) : null));
			v.setErrorIconDrawable(0);
		});

		requestViewModel.getRequesterAccountError().observe(getViewLifecycleOwner(), accountChoiceError-> {
			if(getContext() !=null && accountChoiceError !=null) UIHelper.flashMessage(getContext(), getString(accountChoiceError));
		});

		requestViewModel.getAmount().observe(getViewLifecycleOwner(), amount -> amountValue.setText(Utils.formatAmount(amount)));

		requestViewModel.getActiveChannel().observe(getViewLifecycleOwner(), c -> {
			if (c != null) {
				requesterAccountValue.setTitle(c.name);
				channelRadioGroup.check(c.id);
			}
		});

		requestViewModel.getSelectedChannels().observe(getViewLifecycleOwner(), channels -> {
			if (channels != null) createChannelSelector(channels);
		});

		requestViewModel.getRequesterNumber().observe(getViewLifecycleOwner(), accountNumber -> {
			requesterAccountValue.setSubtitle(accountNumber);
		});
		requestViewModel.getNote().observe(getViewLifecycleOwner(), note -> noteValue.setText(note));
	}

	@Override
	protected void startListeners(View root) {
		super.startListeners(root);
		addRecipientBtn.setOnClickListener(v -> requestViewModel.addRecipient(new StaxContact("")));
		amountInput.addTextChangedListener(amountWatcher);
		channelRadioGroup.setOnCheckedChangeListener((group, checkedId) -> requestViewModel.setActiveChannel(checkedId));
		root.findViewById(R.id.add_new_account).setOnClickListener(view -> startActivity(new Intent(getActivity(), ChannelsActivity.class)));
		receivingAccountNumberInput.addTextChangedListener(receivingAccountNumberWatcher);
		noteInput.addTextChangedListener(noteWatcher);
	}

	private void createChannelSelector(List<Channel> channels) {
		channelRadioGroup.removeAllViews();

		for (Channel c : channels) {
			RadioButton radioButton = (RadioButton) LayoutInflater.from(getContext()).inflate(R.layout.stax_radio_button, null);
			radioButton.setText(c.name);
			radioButton.setId(c.id);
			if (requestViewModel.getActiveChannel().getValue() != null && requestViewModel.getActiveChannel().getValue().id == c.id)
				radioButton.setChecked(true);
			channelRadioGroup.addView(radioButton);
		}
	}

	@Override
	public void onUpdate(int pos, StaxContact recipient) { requestViewModel.onUpdate(pos, recipient); }

	@Override
	public void onClickContact(int index, Context c) { contactPicker(index, c); }

	protected void onContactSelected(int requestCode, StaxContact contact) {
		requestViewModel.onUpdate(requestCode, contact);
		recipientAdapter.notifyDataSetChanged();
	}

	private TextWatcher amountWatcher = new TextWatcher() {
		@Override public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
		@Override public void afterTextChanged(Editable editable) { }

		@Override public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
			requestViewModel.setAmount(charSequence.toString());
		}
	};

	private TextWatcher receivingAccountNumberWatcher = new TextWatcher() {
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
		@Override
		public void afterTextChanged(Editable s) { }
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			requestViewModel.setRequesterNumber(s.toString());
		}
	};

	private TextWatcher noteWatcher = new TextWatcher() {
		@Override public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
		@Override public void afterTextChanged(Editable editable) { }

		@Override public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
			requestViewModel.setNote(charSequence.toString());
		}
	};

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		requestViewModel.setEditing(false);
	}
}
