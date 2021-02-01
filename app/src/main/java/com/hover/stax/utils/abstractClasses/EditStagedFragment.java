package com.hover.stax.utils.abstractClasses;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hover.stax.R;
import com.hover.stax.contacts.StaxContact;
import com.hover.stax.utils.DateUtils;


public abstract class EditStagedFragment extends StagedFragment {

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		stagedViewModel.setEditing(true);
	}

	@Override
	protected void init(View view) {
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

	protected abstract void onContactSelected(int requestCode, StaxContact contact);

	protected abstract void save();
}
