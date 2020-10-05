package com.hover.stax.transfers;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.amplitude.api.Amplitude;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.hover.sdk.permissions.PermissionHelper;
import com.hover.stax.R;
import com.hover.stax.actions.Action;
import com.hover.stax.channels.Channel;
import com.hover.stax.channels.ChannelsActivity;
import com.hover.stax.utils.DateUtils;
import com.hover.stax.utils.PermissionUtils;
import com.hover.stax.utils.UIHelper;
import com.hover.stax.utils.Utils;

import java.util.List;

public class TransferFragment extends Fragment {
	private static final String TAG = "TransferFragment";

	private TransferViewModel transferViewModel;

	private String transferType;
	private EditText amountInput;
	private RadioGroup fromRadioGroup;
	private AppCompatSpinner spinnerNetwork;
	private com.google.android.material.textfield.TextInputLayout recipientEntry;
	private EditText recipientInput;
	private ImageButton contactButton;
	private EditText reasonInput;
	private View errorCard;
	private MaterialDatePicker<Long> datePicker;

	private TextView amountValue, fromValue, toNetworkValue, recipientValue, reasonValue, dateValue;

	final public static int READ_CONTACT = 201;

	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		transferType = getArguments() != null ? getArguments().getString(Action.TRANSACTION_TYPE) : Action.P2P;
		transferViewModel = new ViewModelProvider(requireActivity()).get(TransferViewModel.class);
		transferViewModel.setType(transferType);
		Amplitude.getInstance().logEvent(getString(R.string.visit_screen, transferType));

		View root = inflater.inflate(R.layout.fragment_transfer, container, false);

		initView(root);
		startObservers(root);
		setUpListeners(root);
		createContactSelector();

		return root;
	}

	private void initView(View root) {
		amountValue = root.findViewById(R.id.amountValue);
		fromValue = root.findViewById(R.id.fromValue);
		toNetworkValue = root.findViewById(R.id.toNetworkValue);
		recipientValue = root.findViewById(R.id.recipientValue);
		reasonValue = root.findViewById(R.id.reasonValue);
		dateValue = root.findViewById(R.id.dateValue);

		amountInput = root.findViewById(R.id.amount_input);
		fromRadioGroup = root.findViewById(R.id.fromRadioGroup);
		spinnerNetwork = root.findViewById(R.id.networkSpinner);
		recipientEntry = root.findViewById(R.id.recipientEntry);
		recipientInput = root.findViewById(R.id.recipient_number);
		contactButton = root.findViewById(R.id.contact_button);
		reasonInput = root.findViewById(R.id.reason_input);

		createDatePicker();
		errorCard = root.findViewById(R.id.errorCard);
		setErrorText();
	}

	private void startObservers(View root) {
		transferViewModel.getSelectedChannels().observe(getViewLifecycleOwner(), channels -> {
			if (channels != null) createChannelSelector(channels);
		});

		transferViewModel.getActiveChannel().observe(getViewLifecycleOwner(), c -> {
			Log.e(TAG, "active channel: " + c);
			if (c != null) {
				fromValue.setText(c.name);
				fromRadioGroup.check(c.id);
			}
		});

		transferViewModel.getActions().observe(getViewLifecycleOwner(), actions -> {
			Log.e(TAG, "action update, channel: " + transferViewModel.getActiveChannel().getValue());
			ArrayAdapter<Action> adapter = new ArrayAdapter<>(requireActivity(), R.layout.spinner_items, actions);
			spinnerNetwork.setAdapter(adapter);
		});

		transferViewModel.getStage().observe(getViewLifecycleOwner(), this::updateVariableValues);
		transferViewModel.getAmount().observe(getViewLifecycleOwner(), amount -> amountValue.setText(Utils.formatAmount(amount)));
		transferViewModel.getRecipient().observe(getViewLifecycleOwner(), recipient -> recipientValue.setText(recipient));
		transferViewModel.getReason().observe(getViewLifecycleOwner(), reason -> reasonValue.setText(reason));

		transferViewModel.getIsFuture().observe(getViewLifecycleOwner(), isFuture -> root.findViewById(R.id.dateInput).setVisibility(isFuture ? View.VISIBLE : View.GONE));
		transferViewModel.getFutureDate().observe(getViewLifecycleOwner(), futureDate -> {
			((TextView) root.findViewById(R.id.dateInput)).setText(futureDate != null ? DateUtils.humanFriendlyDate(futureDate) : getString(R.string.date));
			((TextView) root.findViewById(R.id.dateValue)).setText(futureDate != null ? DateUtils.humanFriendlyDate(futureDate) : getString(R.string.date));
		});
	}

	private void createChannelSelector(List<Channel> channels) {
		fromRadioGroup.removeAllViews();

		for (Channel c: channels) {
			RadioButton radioButton = (RadioButton) LayoutInflater.from(getContext()).inflate(R.layout.stax_radio_button, null);
			radioButton.setText(c.name);
			radioButton.setId(c.id);
			Log.e(TAG, "active channel " + transferViewModel.getActiveChannel().getValue());
			if (transferViewModel.getActiveChannel().getValue() != null && transferViewModel.getActiveChannel().getValue().id == c.id)
				radioButton.setChecked(true);
			fromRadioGroup.addView(radioButton);
		}
	}

	private void updateVariableValues(InputStage stage) {
		switch (stage) {
			case FROM_ACCOUNT:
				if (validates(amountInput, R.string.enterAmountError))
					transferViewModel.setAmount(amountInput.getText().toString());
				break;
			case REASON:
				if (validates(recipientInput, R.string.enterRecipientError))
					transferViewModel.setRecipient(recipientInput.getText().toString());
				break;
			case REVIEW:
				if (transferViewModel.getActiveAction().getValue().requiresReason()) {
					transferViewModel.setReason(reasonInput.getText().toString().isEmpty() ? " " : reasonInput.getText().toString());
				} else if (validates(recipientInput, R.string.enterRecipientError))
					transferViewModel.setRecipient(recipientInput.getText().toString());
				break;
		}
	}

	private boolean validates(EditText input, int errorMsg) {
		if (input.getText().toString().isEmpty()) {
			UIHelper.flashMessage(getContext(), getString(errorMsg));
			transferViewModel.goToPrevStage();
			return false;
		}
		return true;
	}

	private void setUpListeners(View root) {
		fromRadioGroup.setOnCheckedChangeListener((group, checkedId) -> transferViewModel.setActiveChannel(checkedId));
		root.findViewById(R.id.add_new_account).setOnClickListener(view -> startActivity(new Intent(getActivity(), ChannelsActivity.class)));

		spinnerNetwork.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				Action action = (Action) spinnerNetwork.getItemAtPosition(position);
				transferViewModel.setActiveAction(action);
				setRecipientHint(action);
				toNetworkValue.setText(spinnerNetwork.getSelectedItem().toString());
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) { }
		});

		((SwitchMaterial) root.findViewById(R.id.futureSwitch)).setOnCheckedChangeListener((view, isChecked) -> transferViewModel.setIsFutureDated(isChecked));
		root.findViewById(R.id.dateInput).setOnClickListener(view -> datePicker.show(getActivity().getSupportFragmentManager(), datePicker.toString()));
	}

	private void setRecipientHint(Action action) {
		recipientEntry.setVisibility(action.requiresRecipient() ? View.VISIBLE : View.GONE);
		if (action.getRequiredParams().contains(Action.ACCOUNT_KEY)) {
			recipientEntry.setHint(getString(R.string.recipient_account));
		} else {
			recipientEntry.setHint(getString(R.string.recipient_phone));
		}
	}

	private void createContactSelector() {
		contactButton.setOnClickListener(view -> {
			Amplitude.getInstance().logEvent(getString(R.string.try_contact_select));
			if (PermissionUtils.hasContactPermission()) {
				Intent contactPickerIntent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
				startActivityForResult(contactPickerIntent, READ_CONTACT);
			} else {
				requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, READ_CONTACT);
			}
		});
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == READ_CONTACT && resultCode == Activity.RESULT_OK) {
			StaxContactModel staxContactModel = new StaxContactModel(data);
			if (staxContactModel.getPhoneNumber() != null) {
				Amplitude.getInstance().logEvent(getString(R.string.contact_select_success));
				recipientInput.setText(Utils.normalizePhoneNumber(staxContactModel.getPhoneNumber(), transferViewModel.getActiveChannel().getValue().countryAlpha2));
			} else {
				Amplitude.getInstance().logEvent(getString(R.string.contact_select_error));
				UIHelper.flashMessage(getContext(), getResources().getString(R.string.selectContactErrorMessage));
			}
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (requestCode == READ_CONTACT && new PermissionHelper(getContext()).permissionsGranted(grantResults)) {
			Amplitude.getInstance().logEvent(getString(R.string.contact_perm_success));
			Intent contactPickerIntent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
			startActivityForResult(contactPickerIntent, READ_CONTACT);
		} else {
			Amplitude.getInstance().logEvent(getString(R.string.contact_perm_denied));
			UIHelper.flashMessage(getContext(), getResources().getString(R.string.contact_perm_error));
		}
	}

	private void createDatePicker() {
		CalendarConstraints.Builder constraintsBuilder = new CalendarConstraints.Builder();
		constraintsBuilder.setStart(DateUtils.now() + DateUtils.DAY);
		datePicker = MaterialDatePicker.Builder.datePicker()
			             .setCalendarConstraints(constraintsBuilder.build()).build();
		datePicker.addOnPositiveButtonClickListener(unixTime -> transferViewModel.setFutureDate(unixTime));
	}

	private void setErrorText() {
		TextView errorMsgView = errorCard.findViewById(R.id.error_message);
		if (transferType.equals(Action.AIRTIME)) errorMsgView.setText(getString(R.string.no_airtime_action_error));
		else errorMsgView.setText(getString(R.string.no_p2p_action_error));
	}
}

