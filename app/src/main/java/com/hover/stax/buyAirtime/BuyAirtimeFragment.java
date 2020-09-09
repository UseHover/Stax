package com.hover.stax.buyAirtime;

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
import com.hover.stax.models.StaxContactModel;
import com.hover.stax.utils.PermissionUtils;
import com.hover.stax.utils.UIHelper;

public class BuyAirtimeFragment extends Fragment {

	private BuyAirtimeViewModel buyAirtimeViewModel;
	private AppCompatSpinner spinnerTo;
	private AppCompatSpinner spinnerFrom;
	private View detailsBlock;
	private View recipientBlock;
	private EditText recipientInput;
	private ImageButton contactButton;
	private View pageError;
	EditText amountInput;

	final public static int READ_CONTACT = 201;

	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		buyAirtimeViewModel = new ViewModelProvider(this).get(BuyAirtimeViewModel.class);
		View root = inflater.inflate(R.layout.fragment_buyairtime, container, false);

		initView(root);
		startListeners();
		setUpSpinners();
		createContactSelector();
		onSubmit(root);

		return root;
	}

	private void initView(View root) {
		spinnerTo = root.findViewById(R.id.toSpinner);
		spinnerFrom = root.findViewById(R.id.fromSpinner);
		detailsBlock = root.findViewById(R.id.details_block);
		recipientBlock = root.findViewById(R.id.recipient_block);
		recipientInput = root.findViewById(R.id.recipient_number);
		amountInput = root.findViewById(R.id.airtimeAmountEditId);
		contactButton = root.findViewById(R.id.contact_button);
		pageError = root.findViewById(R.id.error_message);
	}

	private void startListeners() {
		buyAirtimeViewModel.getSelectedChannels().observe(getViewLifecycleOwner(), channels -> {
			channels.add(new Channel(getResources().getString(R.string.addAService)));
			ChannelAdapter adapter = new ChannelAdapter(getActivity(), R.layout.spinner_items, channels);
			spinnerFrom.setAdapter(adapter);
		});

		buyAirtimeViewModel.getActions().observe(getViewLifecycleOwner(), actions -> {
			if (actions != null && actions.size() > 0) {
				detailsBlock.setVisibility(View.VISIBLE);
				pageError.setVisibility(View.GONE);
				buyAirtimeViewModel.setActiveAction(actions.get(0));
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
					buyAirtimeViewModel.setActiveChannel(channel);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) { }
		});

		spinnerTo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				Action action = (Action) spinnerTo.getItemAtPosition(position);
				buyAirtimeViewModel.setActiveAction(action);
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
		root.findViewById(R.id.buyAirtimeContinueButton).setOnClickListener(view3 -> {
			if (buyAirtimeViewModel.getActiveAction() != null) {
				String amount = amountInput.getText().toString();
				if (TextUtils.getTrimmedLength(amountInput.getText().toString()) > 0) {
					if (buyAirtimeViewModel.getActiveAction().requiresRecipient()) {
						String recipientNumber = recipientInput.getText().toString();
						if (TextUtils.getTrimmedLength(recipientNumber) > 0) {
							makeHoverCall(buyAirtimeViewModel.getActiveAction());
						} else
							UIHelper.flashMessage(getContext(), getResources().getString(R.string.enterRecipientNumberError));
					} else {
						makeHoverCall(buyAirtimeViewModel.getActiveAction());
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
		builder.extra("recipientNumber", recipientInput.getText().toString());
		builder.extra("amount", amountInput.getText().toString());
		builder.finalMsgDisplayTime(2000);
		builder.extra("pin", KeyStoreExecutor.decrypt(buyAirtimeViewModel.getActiveChannel().getValue().pin, ApplicationInstance.getContext()));
		Intent i = builder.buildIntent();
		int AIRTIME_RUN = 203;
		startActivityForResult(i, AIRTIME_RUN);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == READ_CONTACT && resultCode == Activity.RESULT_OK) {
			StaxContactModel staxContactModel = new StaxContactModel(data);
			if (staxContactModel != null) {
				UIHelper.flashMessage(getContext(), getView(), "Selected:     " + staxContactModel.getName() + " - " + staxContactModel.getPhoneNumber());
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

