package com.hover.stax.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.amplitude.api.Amplitude;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.hover.sdk.permissions.PermissionHelper;
import com.hover.stax.R;
import com.hover.stax.channels.Channel;
import com.hover.stax.contacts.StaxContact;
import com.hover.stax.database.Constants;
import com.hover.stax.views.Stax2LineItem;

import java.util.List;

public abstract class StagedFragment extends Fragment {

	protected StagedViewModel stagedViewModel;

	protected RadioGroup channelRadioGroup;
	protected MaterialDatePicker<Long> datePicker;
	protected MaterialDatePicker<Long> endDatePicker;
	private AutoCompleteTextView frequencyDropdown;
	private TextInputEditText repeatInput;
	protected Stax2LineItem accountValue;

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		stagedViewModel.setEditing(false);
	}

	protected void init(View root) {
		channelRadioGroup = root.findViewById(R.id.channelRadioGroup);
		repeatInput = root.findViewById(R.id.repeat_times_input);
		accountValue = root.findViewById(R.id.account_value);
		createFuturePicker();
		createFrequencyDropdown(root);
		startObservers(root);
		startListeners(root);
	}

	protected void startObservers(View root) {
		stagedViewModel.getActiveChannel().observe(getViewLifecycleOwner(), this:: onActiveChannelChange);

		stagedViewModel.getSelectedChannels().observe(getViewLifecycleOwner(), channels -> {
			if (channels != null) createChannelSelector(channels);
		});

		stagedViewModel.getIsFuture().observe(getViewLifecycleOwner(), isFuture -> root.findViewById(R.id.dateEntry).setVisibility(isFuture ? View.VISIBLE : View.GONE));
		stagedViewModel.getFutureDate().observe(getViewLifecycleOwner(), futureDate -> {
			((TextView) root.findViewById(R.id.dateInput)).setText(futureDate == null ? "" : DateUtils.humanFriendlyDate(futureDate));
			((TextView) root.findViewById(R.id.dateValue)).setText(futureDate == null ? "" : DateUtils.humanFriendlyDate(futureDate));
			root.findViewById(R.id.dateRow).setVisibility(futureDate == null ? View.GONE : View.VISIBLE);
		});

		stagedViewModel.getIsRepeating().observe(getViewLifecycleOwner(), isRepeating -> {
            root.findViewById(R.id.repeatInputs).setVisibility(isRepeating ? View.VISIBLE : View.GONE);
			root.findViewById(R.id.repeatButtons).setVisibility(isRepeating ? View.VISIBLE : View.GONE);
		});
		stagedViewModel.getFrequency().observe(getViewLifecycleOwner(), frequency -> {
			((TextView) root.findViewById(R.id.frequencyValue)).setText(getResources().getStringArray(R.array.frequency_choices)[frequency]);
		});
		stagedViewModel.getEndDate().observe(getViewLifecycleOwner(), endDate -> {
			((TextView) root.findViewById(R.id.endDateInput)).setText(endDate == null ? "" : DateUtils.humanFriendlyDate(endDate));
			((TextView) root.findViewById(R.id.endDateValue)).setText(endDate == null ? "" : DateUtils.humanFriendlyDate(endDate));
		});
		stagedViewModel.getRepeatTimes().observe(getViewLifecycleOwner(), repeatTimes -> {
			if (repeatTimes == null || !repeatTimes.toString().equals(((EditText) root.findViewById(R.id.repeat_times_input)).getText().toString()))
				((EditText) root.findViewById(R.id.repeat_times_input)).setText(repeatTimes == null ? "" : repeatTimes.toString());
			((TextView) root.findViewById(R.id.repeatTimesValue)).setText(repeatTimes == null ? "" : repeatTimes.toString());
		});

		stagedViewModel.repeatSaved().observe(getViewLifecycleOwner(), isSaved -> {
			root.findViewById(R.id.frequencyRow).setVisibility(stagedViewModel.getFrequency().getValue() == null || isSaved == null || !isSaved ? View.GONE : View.VISIBLE);
			root.findViewById(R.id.endDateRow).setVisibility(stagedViewModel.getEndDate().getValue() == null || isSaved == null || !isSaved ? View.GONE : View.VISIBLE);
			root.findViewById(R.id.repeatTimesRow).setVisibility(stagedViewModel.getRepeatTimes().getValue() == null || isSaved == null || !isSaved ? View.GONE : View.VISIBLE);
		});
	}

	protected void onActiveChannelChange(Channel c) {
		if (c != null) {
			accountValue.setTitle(c.name);
			channelRadioGroup.check(c.id);
		}
	}

	protected void startListeners(View root) {
		((SwitchMaterial) root.findViewById(R.id.futureSwitch)).setOnCheckedChangeListener((view, isChecked) -> stagedViewModel.setIsFutureDated(isChecked));
		((SwitchMaterial) root.findViewById(R.id.repeatSwitch)).setOnCheckedChangeListener((view, isChecked) -> stagedViewModel.setIsRepeating(isChecked));
		dateDetailListeners(root);

		root.findViewById(R.id.save_repeat_btn).setOnClickListener(view -> stagedViewModel.saveRepeat());
		root.findViewById(R.id.edit_btn).setOnClickListener(Navigation.createNavigateOnClickListener(R.id.navigation_edit, null));
	}

	protected void createChannelSelector(List<Channel> channels) {
		channelRadioGroup.removeAllViews();

		for (Channel c : channels) {
			RadioButton radioButton = (RadioButton) LayoutInflater.from(getContext()).inflate(R.layout.stax_radio_button, null);
			radioButton.setText(c.name);
			radioButton.setId(c.id);
			if (stagedViewModel.getActiveChannel().getValue() != null && stagedViewModel.getActiveChannel().getValue().id == c.id) {
				radioButton.setChecked(true);
			}
			channelRadioGroup.addView(radioButton);
		}
		channelRadioGroup.setOnCheckedChangeListener((group, checkedId) -> stagedViewModel.setActiveChannel(checkedId));
	}

	protected void dateDetailListeners(View root) {
		root.findViewById(R.id.dateInput).setOnFocusChangeListener((view, hasFocus) -> {
			if (hasFocus) datePicker.show(getActivity().getSupportFragmentManager(), datePicker.toString());
		});
		((AutoCompleteTextView) root.findViewById(R.id.frequencyDropdown)).setOnItemClickListener((adapterView, view, pos, id) -> stagedViewModel.setFrequency(pos));
		root.findViewById(R.id.endDateInput).setOnFocusChangeListener((view, hasFocus) -> {
			if (hasFocus) createAndShowEndPicker();
		});
		((TextInputEditText) root.findViewById(R.id.repeat_times_input)).addTextChangedListener(repeatWatcher);
	}

	private TextWatcher repeatWatcher = new TextWatcher() {
		@Override public void afterTextChanged(Editable s) {
			if (repeatInput.hasFocus())
				stagedViewModel.setRepeatTimes(s.toString().isEmpty() ? null : Integer.parseInt(s.toString()));
		}
		@Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
		@Override public void onTextChanged(CharSequence s, int start, int before, int count) { }
	};

	private void createFuturePicker() {
		CalendarConstraints.Builder constraintsBuilder = new CalendarConstraints.Builder();
		constraintsBuilder.setStart(DateUtils.today() + DateUtils.DAY);
		constraintsBuilder.setValidator(DateValidatorPointForward.from(DateUtils.today() + DateUtils.DAY));

		MaterialDatePicker.Builder builder = MaterialDatePicker.Builder.datePicker();
		if (stagedViewModel.getFutureDate().getValue() != null)
			builder.setSelection(stagedViewModel.getFutureDate().getValue());
		builder.setTheme(R.style.StaxCalendar);
		datePicker = builder.setCalendarConstraints(constraintsBuilder.build()).build();
		datePicker.addOnPositiveButtonClickListener(unixTime -> stagedViewModel.setFutureDate(unixTime));
	}

	private void createAndShowEndPicker() {
		MaterialDatePicker.Builder b = MaterialDatePicker.Builder.datePicker();
		b.setTheme(R.style.StaxCalendar);
		if (stagedViewModel.getEndDate().getValue() != null)
			b.setSelection(stagedViewModel.getEndDate().getValue());
		CalendarConstraints.Builder cb = new CalendarConstraints.Builder();
		cb.setStart(DateUtils.today() + DateUtils.DAY);
		cb.setValidator(DateValidatorPointForward.from(stagedViewModel.getStartDate() + DateUtils.DAY));
		endDatePicker = b.setCalendarConstraints(cb.build()).build();
		endDatePicker.addOnPositiveButtonClickListener(unixTime -> {
			stagedViewModel.setEndDate(unixTime);
			repeatInput.requestFocus();
		});
		endDatePicker.show(getActivity().getSupportFragmentManager(), endDatePicker.toString());
	}

	private void createFrequencyDropdown(View root) {
		frequencyDropdown = root.findViewById(R.id.frequencyDropdown);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(requireActivity(), R.array.frequency_choices, R.layout.stax_spinner_item);
		frequencyDropdown.setAdapter(adapter);
		frequencyDropdown.setText(frequencyDropdown.getAdapter().getItem(stagedViewModel.getFrequency().getValue()).toString(), false);
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
			UIHelper.flashMessage(getContext(), getResources().getString(R.string.toast_error_contactperm));
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode != Constants.ADD_SERVICE && resultCode == Activity.RESULT_OK) {
			StaxContact staxContact = new StaxContact(data, getContext());
			if (staxContact.getPhoneNumber() != null) {
				Amplitude.getInstance().logEvent(getString(R.string.contact_select_success));
				onContactSelected(requestCode, staxContact);
			} else {
				Amplitude.getInstance().logEvent(getString(R.string.contact_select_error));
				UIHelper.flashMessage(getContext(), getResources().getString(R.string.toast_error_contactselect));
			}
		}
	}

	protected abstract void onContactSelected(int requestCode, StaxContact contact);
}
