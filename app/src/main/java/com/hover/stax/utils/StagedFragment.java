package com.hover.stax.utils;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.hover.stax.R;

public abstract class StagedFragment extends Fragment {

	protected StagedViewModel stagedViewModel;

	protected MaterialDatePicker<Long> datePicker;
	protected MaterialDatePicker<Long> endDatePicker;
	private AutoCompleteTextView frequencyDropdown;

	protected void init(View root) {
		createDatePickers();
		createFrequencyDropdown(root);
		startObservers(root);
		startListeners(root);
	}

	protected void startObservers(View root) {
		stagedViewModel.getIsFuture().observe(getViewLifecycleOwner(), isFuture -> root.findViewById(R.id.dateInput).setVisibility(isFuture ? View.VISIBLE : View.GONE));
		stagedViewModel.getFutureDate().observe(getViewLifecycleOwner(), futureDate -> {
			((TextView) root.findViewById(R.id.dateInput)).setText(futureDate != null ? DateUtils.humanFriendlyDate(futureDate) : getString(R.string.date));
			((TextView) root.findViewById(R.id.dateValue)).setText(futureDate != null ? DateUtils.humanFriendlyDate(futureDate) : getString(R.string.date));
			root.findViewById(R.id.dateRow).setVisibility(futureDate == null ? View.GONE : View.VISIBLE);
		});

		stagedViewModel.getIsRepeating().observe(getViewLifecycleOwner(), isRepeating ->
            root.findViewById(R.id.repeatInputs).setVisibility(isRepeating ? View.VISIBLE : View.GONE));
		stagedViewModel.getFrequency().observe(getViewLifecycleOwner(), frequency -> {
			((TextView) root.findViewById(R.id.frequencyValue)).setText(getResources().getStringArray(R.array.frequency_array)[frequency]);
		});
		stagedViewModel.getEndDate().observe(getViewLifecycleOwner(), endDate -> {
			((TextView) root.findViewById(R.id.endDateInput)).setText(endDate != null ? DateUtils.humanFriendlyDate(endDate) : getString(R.string.end_date_input));
			((TextView) root.findViewById(R.id.endDateValue)).setText(endDate != null ? DateUtils.humanFriendlyDate(endDate) : getString(R.string.end_date));
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
		root.findViewById(R.id.dateInput).setOnClickListener(view -> datePicker.show(getActivity().getSupportFragmentManager(), datePicker.toString()));

		((SwitchMaterial) root.findViewById(R.id.repeatSwitch)).setOnCheckedChangeListener((view, isChecked) -> stagedViewModel.setIsRepeating(isChecked));
		((AutoCompleteTextView) root.findViewById(R.id.frequencyDropdown)).setOnItemClickListener((adapterView, view, pos, id) -> stagedViewModel.setFrequency(pos));
		root.findViewById(R.id.endDateInput).setOnClickListener(view -> endDatePicker.show(getActivity().getSupportFragmentManager(), endDatePicker.toString()));
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

	private void createDatePickers() {
		CalendarConstraints.Builder constraintsBuilder = new CalendarConstraints.Builder();
		constraintsBuilder.setStart(DateUtils.now() + DateUtils.DAY);
		datePicker = MaterialDatePicker.Builder.datePicker()
			             .setCalendarConstraints(constraintsBuilder.build()).build();
		datePicker.addOnPositiveButtonClickListener(unixTime -> stagedViewModel.setFutureDate(unixTime));

		endDatePicker = MaterialDatePicker.Builder.datePicker()
			             .setCalendarConstraints(constraintsBuilder.build()).build();
		endDatePicker.addOnPositiveButtonClickListener(unixTime -> stagedViewModel.setEndDate(unixTime));
	}

	private void createFrequencyDropdown(View root) {
		frequencyDropdown = root.findViewById(R.id.frequencyDropdown);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(requireActivity(), R.array.frequency_array, R.layout.stax_spinner_item);
		frequencyDropdown.setAdapter(adapter);
		frequencyDropdown.setText(frequencyDropdown.getAdapter().getItem(0).toString(), false);
	}
}
