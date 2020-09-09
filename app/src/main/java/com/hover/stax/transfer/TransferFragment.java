package com.hover.stax.transfer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.hover.sdk.api.HoverParameters;
import com.hover.sdk.permissions.PermissionHelper;
import com.hover.stax.ApplicationInstance;
import com.hover.stax.R;
import com.hover.stax.actions.Action;
import com.hover.stax.actions.ActionAdapter;
import com.hover.stax.channels.Channel;
import com.hover.stax.channels.ChannelAdapter;
import com.hover.stax.channels.ChannelsActivity;
import com.hover.stax.database.KeyStoreExecutor;
import com.hover.stax.utils.PermissionUtils;
import com.hover.stax.utils.UIHelper;

public class TransferFragment extends Fragment {
	private String transferType;
	private TransferViewModel transferViewModel;
	private AppCompatSpinner spinnerTo;
	private AppCompatSpinner spinnerFrom;
	private View detailsBlock;
	private View recipientBlock;
	private EditText recipientInput;
	private ImageButton contactButton;
	private TextView pageError;
	EditText amountInput;

	final public static int READ_CONTACT = 201;

	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		transferType = getArguments() != null ? getArguments().getString(Action.TRANSACTION_TYPE) : Action.P2P;
		transferViewModel = new ViewModelProvider(this).get(TransferViewModel.class);
		transferViewModel.setType(transferType);
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
			channels.add(new Channel(getResources().getString(R.string.addAService)));
			ChannelAdapter adapter = new ChannelAdapter(getActivity(), R.layout.spinner_items, channels);
			spinnerFrom.setAdapter(adapter);
		});

		transferViewModel.getActions().observe(getViewLifecycleOwner(), actions -> {
			if (actions != null && actions.size() > 0) {
				detailsBlock.setVisibility(View.VISIBLE);
				pageError.setVisibility(View.GONE);
				transferViewModel.setActiveAction(actions.get(0));
			} else {
				detailsBlock.setVisibility(View.GONE);
				pageError.setVisibility(View.VISIBLE);
			}
			ActionAdapter adapter = new ActionAdapter(getActivity(), R.layout.spinner_items, actions);
			spinnerTo.setAdapter(adapter);
		});
	}

	private void setUpSpinners() {
		spinnerFrom.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				Channel channel = (Channel) spinnerFrom.getItemAtPosition(position);
				if (channel.id == -1)
					startActivity(new Intent(getActivity(), ChannelsActivity.class));
				else
					transferViewModel.setActiveChannel(channel);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) { }
		});

		spinnerTo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				Action action = (Action) spinnerTo.getItemAtPosition(position);
				transferViewModel.setActiveAction(action);
				recipientBlock.setVisibility(action.requiresRecipient() ? View.VISIBLE : View.GONE);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) { }
		});
	}

	private void createContactSelector() {
		contactButton.setOnClickListener(view -> {
			if (PermissionUtils.hasContactPermission()) {
				Intent contactPickerIntent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
				startActivityForResult(contactPickerIntent, READ_CONTACT);
			} else {
				PermissionUtils.requestContactPerms(getActivity(), READ_CONTACT);
			}
		});
	}

	private void onSubmit(View root) {
		root.findViewById(R.id.confirm_button).setOnClickListener(view3 -> {
			if (transferViewModel.getActiveAction() != null) {
				if (TextUtils.getTrimmedLength(amountInput.getText().toString()) > 0) {
					if (transferViewModel.getActiveAction().requiresRecipient()) {
						if (TextUtils.getTrimmedLength(recipientInput.getText().toString()) > 0) {
							makeHoverCall(transferViewModel.getActiveAction());
						} else
							UIHelper.flashMessage(getContext(), getResources().getString(R.string.enterRecipientNumberError));
					} else {
						makeHoverCall(transferViewModel.getActiveAction());
					}
				} else
					UIHelper.flashMessage(getContext(), getResources().getString(R.string.enterAmountError));
			} else
				UIHelper.flashMessage(getContext(), getResources().getString(R.string.selectServiceError));
		});
	}

	private void makeHoverCall(Action action) {
		HoverParameters.Builder builder = new HoverParameters.Builder(getContext());
		builder.request(action.public_id);
		//builder.setEnvironment(HoverParameters.PROD_ENV);
		builder.style(R.style.myHoverTheme);
		builder.finalMsgDisplayTime(2000);
		builder.extra(Action.PHONE_KEY, recipientInput.getText().toString());
		builder.extra(Action.ACCOUNT_KEY, recipientInput.getText().toString());
		builder.extra(Action.AMOUNT_KEY, amountInput.getText().toString());
		builder.extra(Action.PIN_KEY, KeyStoreExecutor.decrypt(transferViewModel.getActiveChannel().getValue().pin, ApplicationInstance.getContext()));
		Intent i = builder.buildIntent();
		int AIRTIME_RUN = 203;
		startActivityForResult(i, AIRTIME_RUN);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == READ_CONTACT && resultCode == Activity.RESULT_OK) {
			StaxContactModel staxContactModel = new StaxContactModel(data);
			if (staxContactModel.getPhoneNumber() != null) {
				recipientInput.setText(staxContactModel.getPhoneNumber());
			} else
				UIHelper.flashMessage(getContext(), getResources().getString(R.string.selectContactErrorMessage));
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (requestCode == READ_CONTACT && PermissionHelper.permissionsGranted(grantResults)) {
			Intent contactPickerIntent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
			startActivityForResult(contactPickerIntent, READ_CONTACT);
		} else {
			UIHelper.flashMessage(getContext(), getResources().getString(R.string.contact_perm_error));
		}
	}
}

