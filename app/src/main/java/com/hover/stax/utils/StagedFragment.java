package com.hover.stax.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.amplitude.api.Amplitude;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.hover.sdk.permissions.PermissionHelper;
import com.hover.stax.R;
import com.hover.stax.transfers.StaxContactModel;

public abstract class StagedFragment extends Fragment {

	protected StagedViewModel stagedViewModel;

	protected MaterialDatePicker<Long> datePicker;
	protected MaterialDatePicker<Long> endDatePicker;
	private AutoCompleteTextView frequencyDropdown;

	protected void init(View root) {
		createDatePickers(root);
		createFrequencyDropdown(root);
		startObservers(root);
		startListeners(root);
	}

	protected void startObservers(View root) {
		stagedViewModel.getIsFuture().observe(getViewLifecycleOwner(), isFuture -> root.findViewById(R.id.dateEntry).setVisibility(isFuture ? View.VISIBLE : View.GONE));
		stagedViewModel.getFutureDate().observe(getViewLifecycleOwner(), futureDate -> {
			((TextView) root.findViewById(R.id.dateInput)).setText(futureDate == null ? "" :DateUtils.humanFriendlyDate(futureDate));
			((TextView) root.findViewById(R.id.dateValue)).setText(futureDate == null ? "" : DateUtils.humanFriendlyDate(futureDate));
			root.findViewById(R.id.dateRow).setVisibility(futureDate == null ? View.GONE : View.VISIBLE);
		});

		stagedViewModel.getIsRepeating().observe(getViewLifecycleOwner(), isRepeating ->
            root.findViewById(R.id.repeatInputs).setVisibility(isRepeating ? View.VISIBLE : View.GONE));
		stagedViewModel.getFrequency().observe(getViewLifecycleOwner(), frequency -> {
			((TextView) root.findViewById(R.id.frequencyValue)).setText(getResources().getStringArray(R.array.frequency_array)[frequency]);
		});
		stagedViewModel.getEndDate().observe(getViewLifecycleOwner(), endDate -> {
			((TextView) root.findViewById(R.id.endDateInput)).setText(endDate == null ? "" : DateUtils.humanFriendlyDate(endDate));
			((TextView) root.findViewById(R.id.endDateValue)).setText(endDate == null ? "" : DateUtils.humanFriendlyDate(endDate));
		});
		stagedViewModel.getRepeatTimes().observe(getViewLifecycleOwner(), repeatTimes -> {
			if (repeatTimes == null) return;
			if (!repeatTimes.toString().equals(((EditText) root.findViewById(R.id.repeat_times_input)).getText().toString()))
				((EditText) root.findViewById(R.id.repeat_times_input)).setText(repeatTimes.toString());
			((TextView) root.findViewById(R.id.repeatTimesValue)).setText(repeatTimes.toString());
		});

		stagedViewModel.repeatSaved().observe(getViewLifecycleOwner(), isSaved -> {
			root.findViewById(R.id.frequencyRow).setVisibility(stagedViewModel.getFrequency().getValue() == null || isSaved == null || !isSaved ? View.GONE : View.VISIBLE);
			root.findViewById(R.id.endDateRow).setVisibility(stagedViewModel.getEndDate().getValue() == null || isSaved == null || !isSaved ? View.GONE : View.VISIBLE);
			root.findViewById(R.id.repeatTimesRow).setVisibility(stagedViewModel.getRepeatTimes().getValue() == null || isSaved == null || !isSaved ? View.GONE : View.VISIBLE);
		});
	}

	protected void startListeners(View root) {
		((SwitchMaterial) root.findViewById(R.id.futureSwitch)).setOnCheckedChangeListener((view, isChecked) -> stagedViewModel.setIsFutureDated(isChecked));
		root.findViewById(R.id.dateInput).setOnFocusChangeListener((view, hasFocus) -> {
			if (hasFocus) datePicker.show(getActivity().getSupportFragmentManager(), datePicker.toString());
		});

		((SwitchMaterial) root.findViewById(R.id.repeatSwitch)).setOnCheckedChangeListener((view, isChecked) -> stagedViewModel.setIsRepeating(isChecked));
		((AutoCompleteTextView) root.findViewById(R.id.frequencyDropdown)).setOnItemClickListener((adapterView, view, pos, id) -> stagedViewModel.setFrequency(pos));
		root.findViewById(R.id.endDateInput).setOnFocusChangeListener((view, hasFocus) -> {
			if (hasFocus) endDatePicker.show(getActivity().getSupportFragmentManager(), endDatePicker.toString());
		});
		((TextInputEditText) root.findViewById(R.id.repeat_times_input)).addTextChangedListener(repeatWatcher);

		root.findViewById(R.id.save_btn).setOnClickListener(view -> stagedViewModel.saveRepeat());
	}

	private TextWatcher repeatWatcher = new TextWatcher() {
		@Override public void afterTextChanged(Editable s) {
			stagedViewModel.setRepeatTimes(s.toString().isEmpty() ? null : Integer.parseInt(s.toString()));
		}
		@Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
		@Override public void onTextChanged(CharSequence s, int start, int before, int count) { }
	};

	private void createDatePickers(View root) {
		CalendarConstraints.Builder constraintsBuilder = new CalendarConstraints.Builder();
		constraintsBuilder.setStart(DateUtils.now() + DateUtils.DAY);
		datePicker = MaterialDatePicker.Builder.datePicker()
			             .setCalendarConstraints(constraintsBuilder.build()).build();
		datePicker.addOnPositiveButtonClickListener(unixTime -> stagedViewModel.setFutureDate(unixTime));

		endDatePicker = MaterialDatePicker.Builder.datePicker()
			             .setCalendarConstraints(constraintsBuilder.build()).build();
		endDatePicker.addOnPositiveButtonClickListener(unixTime -> {
			stagedViewModel.setEndDate(unixTime);
			root.findViewById(R.id.repeat_times_input).requestFocus();
		});
	}

	private void createFrequencyDropdown(View root) {
		frequencyDropdown = root.findViewById(R.id.frequencyDropdown);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(requireActivity(), R.array.frequency_array, R.layout.stax_spinner_item);
		frequencyDropdown.setAdapter(adapter);
		frequencyDropdown.setText(frequencyDropdown.getAdapter().getItem(0).toString(), false);
	}

	protected void contactPicker(int requestCode, Context c) {
		Amplitude.getInstance().logEvent(getString(R.string.try_contact_select));
		if (PermissionUtils.hasContactPermission(c))
			startContactIntent(requestCode);
		else
			requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, requestCode);
	}

	private void startContactIntent(int requestCode) {
		Intent contactPickerIntent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
		startActivityForResult(contactPickerIntent, requestCode);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (new PermissionHelper(getContext()).permissionsGranted(grantResults)) {
			Amplitude.getInstance().logEvent(getString(R.string.contact_perm_success));
			startContactIntent(requestCode);
		} else {
			Amplitude.getInstance().logEvent(getString(R.string.contact_perm_denied));
			UIHelper.flashMessage(getContext(), getResources().getString(R.string.contact_perm_error));
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == Activity.RESULT_OK) {
			StaxContactModel staxContactModel = new StaxContactModel(data, getContext());
			if (staxContactModel.getPhoneNumber() != null) {
				Amplitude.getInstance().logEvent(getString(R.string.contact_select_success));
				onContactSelected(requestCode, staxContactModel);
			} else {
				Amplitude.getInstance().logEvent(getString(R.string.contact_select_error));
				UIHelper.flashMessage(getContext(), getResources().getString(R.string.selectContactErrorMessage));
			}
		}
	}

	protected abstract void onContactSelected(int requestCode, StaxContactModel contact);
}
