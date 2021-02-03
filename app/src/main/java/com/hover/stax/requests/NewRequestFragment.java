package com.hover.stax.requests;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputLayout;
import com.hover.stax.R;
import com.hover.stax.channels.Channel;
import com.hover.stax.channels.ChannelDropdownViewModel;
import com.hover.stax.contacts.StaxContact;
import com.hover.stax.utils.UIHelper;
import com.hover.stax.utils.Utils;
import com.hover.stax.utils.abstractClasses.StagedFragment;
import com.hover.stax.views.Stax2LineItem;
import com.hover.stax.views.StaxCardView;

public class NewRequestFragment extends StagedFragment implements RecipientAdapter.UpdateListener {

	protected NewRequestViewModel requestViewModel;

	private RecyclerView recipientInputList;

	private EditText amountInput, requesterAccountNo, noteInput;
	private TextView addRecipientBtn;

	private LinearLayout recipientValueList;
	protected Stax2LineItem accountValue;

	private StaxCardView shareCard;

	private RecipientAdapter recipientAdapter;
	private int recipientCount = 0;

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		channelDropdownViewModel = new ViewModelProvider(requireActivity()).get(ChannelDropdownViewModel.class);
		stagedViewModel = new ViewModelProvider(requireActivity()).get(NewRequestViewModel.class);
		requestViewModel = (NewRequestViewModel) stagedViewModel;

		View view = inflater.inflate(R.layout.fragment_request, container, false);
		init(view);
		startObservers(view);
		startListeners();
		return view;
	}

	@Override
	protected void init(View view) {
		recipientValueList = view.findViewById(R.id.requesteeValueList);
		accountValue = view.findViewById(R.id.account_value);

		recipientInputList = view.findViewById(R.id.recipient_list);
		amountInput = view.findViewById(R.id.amount_input);
		amountInput.setText(requestViewModel.getAmount().getValue());
		requesterAccountNo = view.findViewById(R.id.accountNumber_input);
		requesterAccountNo.setText(requestViewModel.getRequesterNumber().getValue());

		noteInput = view.findViewById(R.id.note_input);
		noteInput.setText(requestViewModel.getNote().getValue());
		addRecipientBtn = view.findViewById(R.id.add_recipient_button);

		recipientInputList.setLayoutManager(UIHelper.setMainLinearManagers(getContext()));
		recipientAdapter = new RecipientAdapter(requestViewModel.getRequestees().getValue(), requestViewModel.getRecentContacts().getValue(), this);
		recipientInputList.setAdapter(recipientAdapter);

		shareCard = view.findViewById(R.id.shareCard);

		super.init(view);
	}

	protected void startObservers(View root) {
		channelDropdownViewModel.getActiveChannel().observe(getViewLifecycleOwner(), channel -> {
			requestViewModel.setActiveChannel(channel);
			accountValue.setTitle(channel.toString());
		});

		requestViewModel.getActiveChannel().observe(getViewLifecycleOwner(), channel -> Log.e("RequestFrag", "Got active channel " + channel));

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

		requestViewModel.getAmountError().observe(getViewLifecycleOwner(), error -> {
			amountInput.setError((error != null ? getString(error) : null));
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

		requestViewModel.getAmount().observe(getViewLifecycleOwner(), amount -> {
			root.findViewById(R.id.amountRow).setVisibility(requestViewModel.validAmount() ? View.VISIBLE : View.GONE);
			((TextView) root.findViewById(R.id.amountValue)).setText(Utils.formatAmount(amount));
		});
		requestViewModel.getRequesterNumber().observe(getViewLifecycleOwner(), accountNumber -> accountValue.setSubtitle(accountNumber));
		requestViewModel.getNote().observe(getViewLifecycleOwner(), note -> {
			root.findViewById(R.id.noteRow).setVisibility(requestViewModel.validNote() ? View.VISIBLE : View.GONE);
			((TextView) root.findViewById(R.id.noteValue)).setText(note);
		});

		requestViewModel.getIsEditing().observe(getViewLifecycleOwner(), this::showEdit);
	}

	protected void showEdit(boolean isEditing) {
		super.showEdit(isEditing);
		if (!isEditing) requestViewModel.createRequest();
		shareCard.setVisibility(isEditing ? View.GONE : View.VISIBLE);
		fab.setVisibility(isEditing ? View.VISIBLE : View.GONE);
	}

	protected void onActiveChannelChange(Channel c) {
		if (c != null && c.accountNo != null && !c.accountNo.isEmpty())
			requesterAccountNo.setText(c.accountNo);
	}

	protected void startListeners() {
		addRecipientBtn.setOnClickListener(v -> requestViewModel.addRecipient(new StaxContact("")));
		amountInput.addTextChangedListener(amountWatcher);
		requesterAccountNo.addTextChangedListener(receivingAccountNumberWatcher);
		noteInput.addTextChangedListener(noteWatcher);

		fab.setOnClickListener(v -> fabClicked(v));
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

	private void fabClicked(View v) {
		requestViewModel.removeInvalidRequestees();
		if (requestViewModel.getIsEditing().getValue() && (!channelDropdownViewModel.validates() || !requestViewModel.validates()))
			UIHelper.flashMessage(getContext(), getString(R.string.toast_pleasefix));
		else
			requestViewModel.setEditing(false);
	}
}
