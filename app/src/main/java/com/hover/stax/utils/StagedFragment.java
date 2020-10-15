package com.hover.stax.utils;

import android.view.View;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.hover.stax.R;

public abstract class StagedFragment extends Fragment {

	protected StagedViewModel stagedViewModel;

	protected MaterialDatePicker<Long> datePicker;

	protected void init(View root) {
		createDatePicker();
		startObservers(root);
		startListeners(root);
	}

	protected void startObservers(View root) {
		stagedViewModel.getIsFuture().observe(getViewLifecycleOwner(), isFuture -> root.findViewById(R.id.dateInput).setVisibility(isFuture ? View.VISIBLE : View.GONE));
		stagedViewModel.getFutureDate().observe(getViewLifecycleOwner(), futureDate -> {
			((TextView) root.findViewById(R.id.dateInput)).setText(futureDate != null ? DateUtils.humanFriendlyDate(futureDate) : getString(R.string.date));
			((TextView) root.findViewById(R.id.dateValue)).setText(futureDate != null ? DateUtils.humanFriendlyDate(futureDate) : getString(R.string.date));
		});
	}

	protected void startListeners(View root) {
		((SwitchMaterial) root.findViewById(R.id.futureSwitch)).setOnCheckedChangeListener((view, isChecked) -> stagedViewModel.setIsFutureDated(isChecked));
		root.findViewById(R.id.dateInput).setOnClickListener(view -> datePicker.show(getActivity().getSupportFragmentManager(), datePicker.toString()));
	}

	private void createDatePicker() {
		CalendarConstraints.Builder constraintsBuilder = new CalendarConstraints.Builder();
		constraintsBuilder.setStart(DateUtils.now() + DateUtils.DAY);
		datePicker = MaterialDatePicker.Builder.datePicker()
			             .setCalendarConstraints(constraintsBuilder.build()).build();
		datePicker.addOnPositiveButtonClickListener(unixTime -> stagedViewModel.setFutureDate(unixTime));
	}
}
