package com.hover.stax.transfers;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.amplitude.api.Amplitude;
import com.hover.sdk.permissions.PermissionHelper;
import com.hover.stax.R;
import com.hover.stax.actions.Action;
import com.hover.stax.channels.Channel;
import com.hover.stax.channels.ChannelsActivity;
import com.hover.stax.home.MainActivity;
import com.hover.stax.hover.HoverSession;
import com.hover.stax.security.BiometricChecker;
import com.hover.stax.utils.PermissionUtils;
import com.hover.stax.utils.UIHelper;
import com.hover.stax.utils.Utils;

public class TransferFragment extends Fragment implements BiometricChecker.AuthListener {
	private static final String TAG = "TransferFragment";

	private TransferViewModel transferViewModel;
	private String transferType;
	private AppCompatSpinner spinnerTo;
	private AppCompatSpinner spinnerFrom;
	private View detailsBlock;
	private View recipientBlock;
	private TextView recipientLabel;
	private EditText recipientInput;
	private ImageButton contactButton;
	private TextView pageError;
	EditText amountInput;

	final public static int READ_CONTACT = 201;

	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		transferType = getArguments() != null ? getArguments().getString(Action.TRANSACTION_TYPE) : Action.P2P;
		transferViewModel = new ViewModelProvider(this).get(TransferViewModel.class);
		transferViewModel.setType(transferType);
		Amplitude.getInstance().logEvent(getString(R.string.visit_screen, transferType));

		View root = inflater.inflate(R.layout.fragment_transfer, container, false);

		initView(root);
		startListeners();
		setUpSpinners();
		createContactSelector();
		onSubmit(root);

		return root;
	}

	private void initView(View root) {
		((TextView) root.findViewById(R.id.transfer_title)).setText(getTitle());
		spinnerTo = root.findViewById(R.id.toSpinner);
		spinnerFrom = root.findViewById(R.id.fromSpinner);
		detailsBlock = root.findViewById(R.id.details_block);
		recipientBlock = root.findViewById(R.id.recipient_block);
		recipientLabel = root.findViewById(R.id.recipient_label);
		recipientInput = root.findViewById(R.id.recipient_number);
		amountInput = root.findViewById(R.id.amount_input);
		contactButton = root.findViewById(R.id.contact_button);
		pageError = root.findViewById(R.id.error_message);
		pageError.setText(getError());
	}

	private String getTitle() {
		if (transferType.equals(Action.AIRTIME)) return getString(R.string.title_airtime);
		else return getString(R.string.title_p2p);
	}

	private String getError() {
		if (transferType.equals(Action.AIRTIME)) return getString(R.string.no_airtime_action_error);
		else return getString(R.string.no_p2p_action_error);
	}

	private void startListeners() {
		transferViewModel.getSelectedChannels().observe(getViewLifecycleOwner(), channels -> {
			if (channels.size() == 0) {
				channels.add(new Channel(-1, getResources().getString(R.string.choose_service_hint)));
			}
			channels.add(new Channel(-2, getResources().getString(R.string.add_service)));
			ArrayAdapter<Channel> adapter = new ArrayAdapter(getActivity(), R.layout.spinner_items, channels);
			spinnerFrom.setAdapter(adapter);
		});

		transferViewModel.getActions().observe(getViewLifecycleOwner(), actions -> {
			if (transferViewModel.getActiveChannel().getValue() == null || transferViewModel.getActiveChannel().getValue().id == -1) {
				detailsBlock.setVisibility(View.GONE);
				pageError.setVisibility(View.GONE);
			} else if (actions != null && actions.size() > 0) {
				detailsBlock.setVisibility(View.VISIBLE);
				pageError.setVisibility(View.GONE);
				transferViewModel.setActiveAction(actions.get(0));
			} else {
				detailsBlock.setVisibility(View.GONE);
				pageError.setVisibility(View.VISIBLE);
			}
			ArrayAdapter<Action> adapter = new ArrayAdapter(getActivity(), R.layout.spinner_items, actions);
			spinnerTo.setAdapter(adapter);
		});
	}

	private void setUpSpinners() {
		spinnerFrom.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				Channel channel = (Channel) spinnerFrom.getItemAtPosition(position);
				if (channel.id == -2)
					startActivity(new Intent(getActivity(), ChannelsActivity.class));
				else
					transferViewModel.setActiveChannel(channel);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});

		spinnerTo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				Action action = (Action) spinnerTo.getItemAtPosition(position);
				transferViewModel.setActiveAction(action);
				updateView(action);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});
	}

	private void updateView(Action action) {
		recipientBlock.setVisibility(action.requiresRecipient() ? View.VISIBLE : View.GONE);
		if (action.getRequiredParams().contains(Action.ACCOUNT_KEY)) {
			recipientLabel.setText(getString(R.string.recipient_account));
		} else {
			recipientLabel.setText(getString(R.string.recipient_phone));
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

	private void onSubmit(View root) {
		root.findViewById(R.id.confirm_button).setOnClickListener(view3 -> {
			if (transferViewModel.getActiveAction() != null) {
				if (TextUtils.getTrimmedLength(amountInput.getText().toString()) > 0) {
					if (transferViewModel.getActiveAction().requiresRecipient()) {
						if (TextUtils.getTrimmedLength(recipientInput.getText().toString()) > 0) {
							authenticate();
						} else
							UIHelper.flashMessage(getContext(), getResources().getString(R.string.enterRecipientNumberError));
					} else {
						authenticate();
					}
				} else
					UIHelper.flashMessage(getContext(), getResources().getString(R.string.enterAmountError));
			} else
				UIHelper.flashMessage(getContext(), getResources().getString(R.string.selectServiceError));
		});
	}

	private void authenticate() {
		new BiometricChecker(this, (AppCompatActivity) getActivity()).startAuthentication(transferViewModel.getActiveAction());
	}

	@Override
	public void onAuthError(String error) {
		Log.e(TAG, error);
	}

	@Override
	public void onAuthSuccess(Action act) {
		makeHoverCall(act);
	}

	private void makeHoverCall(Action act) {
		Amplitude.getInstance().logEvent(getString(R.string.finish_screen, transferType));
		new HoverSession.Builder(act, transferViewModel.getActiveChannel().getValue(),
				getActivity(), MainActivity.TRANSFER_REQUEST)
				.extra(Action.PHONE_KEY, recipientInput.getText().toString())
				.extra(Action.ACCOUNT_KEY, recipientInput.getText().toString())
				.extra(Action.AMOUNT_KEY, amountInput.getText().toString())
				.run();
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
		if (requestCode == READ_CONTACT && PermissionHelper.permissionsGranted(grantResults)) {
			Amplitude.getInstance().logEvent(getString(R.string.contact_perm_success));
			Intent contactPickerIntent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
			startActivityForResult(contactPickerIntent, READ_CONTACT);
		} else {
			Amplitude.getInstance().logEvent(getString(R.string.contact_perm_denied));
			UIHelper.flashMessage(getContext(), getResources().getString(R.string.contact_perm_error));
		}
	}
}

