package com.hover.stax.requests;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.RecyclerView;

import com.hover.stax.R;
import com.hover.stax.transfers.StaxContactModel;
import com.hover.stax.utils.DateUtils;
import com.hover.stax.utils.StagedFragment;
import com.hover.stax.utils.UIHelper;

public class EditRequestFragment extends StagedFragment implements RecipientAdapter.UpdateListener {

	protected NewRequestViewModel requestViewModel;

	private RecyclerView recipientInputList;
	private RecipientAdapter recipientAdapter;
	private EditText amountInput, noteInput;

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
		recipientAdapter = new RecipientAdapter(requestViewModel.getRecipients().getValue(), this);
		recipientInputList.setAdapter(recipientAdapter);

		amountInput = view.findViewById(R.id.amount_input);
		amountInput.setText(requestViewModel.getAmount().getValue());
		noteInput = view.findViewById(R.id.note_input);
		noteInput.setText(requestViewModel.getNote().getValue());

		view.findViewById(R.id.dateEntry).setVisibility(stagedViewModel.getFutureDate().getValue() == null ? View.GONE : View.VISIBLE);
		view.findViewById(R.id.repeatInputs).setVisibility(stagedViewModel.repeatSaved().getValue() == null || !stagedViewModel.repeatSaved().getValue() ? View.GONE : View.VISIBLE);

		super.init(view);
	}

	@Override
	protected void startObservers(View root) {
		stagedViewModel.getFutureDate().observe(getViewLifecycleOwner(), futureDate -> {
			((TextView) root.findViewById(R.id.dateInput)).setText(futureDate == null ? "" : DateUtils.humanFriendlyDate(futureDate));
		});

		stagedViewModel.getFrequency().observe(getViewLifecycleOwner(), frequency -> {
			((TextView) root.findViewById(R.id.repeat_times_input)).setText(null);
		});
		stagedViewModel.getEndDate().observe(getViewLifecycleOwner(), endDate -> {
			((TextView) root.findViewById(R.id.endDateInput)).setText(endDate == null ? "" : DateUtils.humanFriendlyDate(endDate));
		});
		stagedViewModel.getRepeatTimes().observe(getViewLifecycleOwner(), repeatTimes -> {
			if (repeatTimes != null && !repeatTimes.toString().equals(((EditText) root.findViewById(R.id.repeat_times_input)).getText().toString()))
				((EditText) root.findViewById(R.id.repeat_times_input)).setText(repeatTimes.toString());
		});
	}

	@Override
	protected void startListeners(View root) {
		dateDetailListeners(root);
		root.findViewById(R.id.save_edits_btn).setOnClickListener(v -> save());
	}

	private void save() {
		requestViewModel.resetRecipients();
		for (int c = 0; c < recipientInputList.getChildCount(); c++)
			requestViewModel.addRecipient(
				((TextView) recipientInputList.getChildAt(c).findViewById(R.id.recipient_input)).getText().toString());
		if (!amountInput.getText().toString().isEmpty())
			requestViewModel.setAmount(amountInput.getText().toString());
		if (!noteInput.getText().toString().isEmpty())
			requestViewModel.setNote(noteInput.getText().toString());
		NavHostFragment.findNavController(this).navigate(R.id.navigation_new);
	}

	@Override
	public void onUpdate(int pos, String recipient) { }

	@Override
	public void onClickContact(int index, Context c) { contactPicker(index, c); }

	protected void onContactSelected(int requestCode, StaxContactModel contact) {
		((TextView) recipientInputList.getChildAt(requestCode).findViewById(R.id.recipient_input)).setText(contact.getPhoneNumber());
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		requestViewModel.setEditing(true);
	}
}
