package com.hover.stax.transfers;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.cardview.widget.CardView;
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
import com.hover.stax.languages.Lang;
import com.hover.stax.security.BiometricChecker;
import com.hover.stax.utils.PermissionUtils;
import com.hover.stax.utils.UIHelper;
import com.hover.stax.utils.Utils;
import com.hover.stax.utils.fonts.FontReplacer;

import java.util.ArrayList;
import java.util.List;

import static com.hover.stax.transfers.InputStage.AMOUNT;
import static com.hover.stax.transfers.InputStage.FROM_ACCOUNT;
import static com.hover.stax.transfers.InputStage.REVIEW;
import static com.hover.stax.transfers.InputStage.SEND;
import static com.hover.stax.transfers.InputStage.TO_NETWORK;

public class TransferFragment extends Fragment implements BiometricChecker.AuthListener {
	private static final String TAG = "TransferFragment";

	private TransferViewModel transferViewModel;
	private String transferType;
	private AppCompatSpinner spinnerTo;
	private View detailsBlock;
	private View recipientBlock;
	private TextView recipientLabel;
	private EditText recipientInput;
	private ImageButton contactButton;
	private TextView pageError;
	private EditText amountInput;
	private InputStage nextInputStage = AMOUNT;
	private InputStage previousInputStage = null;
	private RelativeLayout amountStageView;
	private CardView fromStageStageView;
	private CardView toStageView;
	private String networkLabelString;
	private RadioGroup fromRadioGroup;
	private List <Channel> fromChannels;




	private TextView amountLabel, amountValue, fromLabel, fromValue, toNetworkValue, toNetworkLabel, numberLabel, numberValue;

	final public static int READ_CONTACT = 201;

	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		transferType = getArguments() != null ? getArguments().getString(Action.TRANSACTION_TYPE) : Action.P2P;
		transferViewModel = new ViewModelProvider(this).get(TransferViewModel.class);
		transferViewModel.setType(transferType);
		Amplitude.getInstance().logEvent(getString(R.string.visit_screen, transferType));

		View root = inflater.inflate(R.layout.fragment_transfer, container, false);

		initView(root);
		startListeners();
		setUpSpinners(root);
		createContactSelector();
		onSubmit(root);
		handleInputStageChanges();
		handleBackButton(root);

		return root;
	}

	private void handleBackButton(View root) {
		root.findViewById(R.id.backButton).setOnClickListener(v -> {
			if(previousInputStage !=null) transferViewModel.setInputStage(previousInputStage);
			else if(getActivity() !=null) getActivity().onBackPressed();
		});
	}
	private void handleInputStageChanges() {
		transferViewModel.stageLiveData().observe(getViewLifecycleOwner(), this::updateStageViews);
	}



	private void initView(View root) {
		((TextView) root.findViewById(R.id.transfer_title)).setText(getTitleAndSetNetworkLabel());
		spinnerTo = root.findViewById(R.id.toSpinner);
		fromRadioGroup= root.findViewById(R.id.fromRadioGroup);
		detailsBlock = root.findViewById(R.id.details_block);
		recipientBlock = root.findViewById(R.id.recipient_block);
		recipientLabel = root.findViewById(R.id.recipient_label);
		recipientInput = root.findViewById(R.id.recipient_number);
		amountInput = root.findViewById(R.id.amount_input);
		contactButton = root.findViewById(R.id.contact_button);
		pageError = root.findViewById(R.id.error_message);
		amountValue = root.findViewById(R.id.amountValue);
		amountLabel = root.findViewById(R.id.amountLabel);
		fromLabel = root.findViewById(R.id.fromLabel);
		fromValue = root.findViewById(R.id.fromValue);
		toNetworkLabel = root.findViewById(R.id.toNetworkLabel);
		toNetworkValue = root.findViewById(R.id.toNetworkValue);
		numberLabel = root.findViewById(R.id.toNumberLabel);
		numberValue = root.findViewById(R.id.toNumberValue);
		amountStageView = root.findViewById(R.id.amountStageCardView);
		toStageView = root.findViewById(R.id.toStageCardView);
		fromStageStageView = root.findViewById(R.id.fromStageCardView);

		pageError.setText(getError());
		toNetworkLabel.setText(networkLabelString);
	}

	private void amountView(int visibility) {
		amountLabel.setVisibility(visibility);
		amountValue.setVisibility(visibility);
		if(recipientInput.getText() !=null) amountValue.setText(amountInput.getText().toString());
	}
	private void fromView(int visibility) {
		fromLabel.setVisibility(visibility);
		fromValue.setVisibility(visibility);
	}
	private void toNetworkView(int visibility) {
		toNetworkLabel.setVisibility(visibility);
		toNetworkValue.setVisibility(visibility);
	}
	private void numberView(int visibility) {
		numberValue.setVisibility(visibility);
		numberLabel.setVisibility(visibility);
		if(recipientInput.getText() !=null) numberValue.setText(recipientInput.getText().toString());
	}

	private void setToStageView(int visibility) {
		toStageView.setVisibility(visibility);
	}
	private void setFromStageStageView(int v) {
		fromStageStageView.setVisibility(v);
	}
	private void setAmountStageView(int v) {
		amountStageView.setVisibility(v);
	}




	private void updateStageViews(InputStage stage) {

		switch (stage) {
			case AMOUNT:
				amountView(View.GONE);
				fromView(View.GONE);
				toNetworkView(View.GONE);
				numberView(View.GONE);

				setAmountStageView(View.VISIBLE);
				setToStageView(View.GONE);
				setFromStageStageView(View.GONE);

				nextInputStage = FROM_ACCOUNT;
				previousInputStage = null;

				break;
			case FROM_ACCOUNT:

				amountView(View.VISIBLE);
				fromView(View.GONE);
				toNetworkView(View.GONE);
				numberView(View.GONE);

				setAmountStageView(View.GONE);
				setFromStageStageView(View.VISIBLE);
				setToStageView(View.GONE);

				nextInputStage = TO_NETWORK;
				previousInputStage = AMOUNT;
				break;
			case TO_NETWORK:
				amountView(View.VISIBLE);
				fromView(View.VISIBLE);
				toNetworkView(View.GONE);
				numberView(View.GONE);

				setAmountStageView(View.GONE);
				setFromStageStageView(View.GONE);
				setToStageView(View.VISIBLE);

				nextInputStage = REVIEW;
				previousInputStage = FROM_ACCOUNT;
				break;
			case TO_NUMBER:
				amountView(View.VISIBLE);
				fromView(View.VISIBLE);
				toNetworkView(View.VISIBLE);
				numberView(View.GONE);

				setAmountStageView(View.GONE);
				setFromStageStageView(View.GONE);
				setToStageView(View.VISIBLE);

				nextInputStage = REVIEW;
				previousInputStage = FROM_ACCOUNT;
				break;
			default: //REVIEW STAGE
				amountView(View.VISIBLE);
				fromView(View.VISIBLE);
				toNetworkView(View.VISIBLE);
				numberView(View.VISIBLE);

				setAmountStageView(View.GONE);
				setToStageView(View.GONE);
				setFromStageStageView(View.GONE);
				nextInputStage = SEND;
				previousInputStage = TO_NETWORK;
				break;
		}
	}



	private String getTitleAndSetNetworkLabel() {
		if (transferType.equals(Action.AIRTIME)) {
			networkLabelString = getString(R.string.to_who);
			return getString(R.string.title_airtime);
		}
		else {
			networkLabelString = getString(R.string.toNetwork);
			return getString(R.string.transfer);
		}
	}

	private String getError() {
		if (transferType.equals(Action.AIRTIME)) return getString(R.string.no_airtime_action_error);
		else return getString(R.string.no_p2p_action_error);
	}

	private void startListeners() {
		transferViewModel.getSelectedChannels().observe(getViewLifecycleOwner(), channels -> {

			fromChannels = new ArrayList<>();
			fromRadioGroup.removeAllViews();

			for (int i=0; i<channels.size(); i++) {
				Channel channel = channels.get(i);
				fromChannels.add(i, channel);
				RadioButton radioButton = new RadioButton(getActivity());
				radioButton.setText(channel.name);
				radioButton.setTextColor(Color.WHITE);
				radioButton.setHighlightColor(Color.WHITE);
				radioButton.setTextSize(18);
				radioButton.setHeight(75);
				radioButton.setPadding(16, 0, 0, 0);
				radioButton.setTag(i);
				Typeface font = FontReplacer.getLightFont();
				radioButton.setTypeface(font);

				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) radioButton.setButtonTintList(UIHelper.radioGroupColorState());

				fromRadioGroup.addView(radioButton);
			}
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

	private void setUpSpinners(View root) {

		fromRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {

			if(fromChannels!=null && fromChannels.size() > 0) {
				int checkedRadioButtonId = fromRadioGroup.getCheckedRadioButtonId();
				RadioButton radioBtn = root.findViewById(checkedRadioButtonId);
				if(radioBtn!=null) {
					int id = Integer.parseInt(radioBtn.getTag().toString());
					Channel channel = fromChannels.get(id);
					if(channel !=null) {
						transferViewModel.setActiveChannel(channel);
						fromValue.setText(channel.name);
					}
				}
			}
		});

		TextView addNewAccount = root.findViewById(R.id.add_new_account);
		UIHelper.setTextUnderline(addNewAccount, getString(R.string.add_an_account));

		root.findViewById(R.id.add_new_account).setOnClickListener(view->{
			startActivity(new Intent(getActivity(), ChannelsActivity.class));
		});


		spinnerTo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				Action action = (Action) spinnerTo.getItemAtPosition(position);
				transferViewModel.setActiveAction(action);
				updateView(action);
				toNetworkValue.setText(spinnerTo.getSelectedItem().toString());
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


			if(nextInputStage != SEND) {

				transferViewModel.setInputStage(nextInputStage);

			}else if (transferViewModel.getActiveAction() != null) {
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
		if (requestCode == READ_CONTACT && new PermissionHelper(getContext()).permissionsGranted(grantResults)) {
			Amplitude.getInstance().logEvent(getString(R.string.contact_perm_success));
			Intent contactPickerIntent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
			startActivityForResult(contactPickerIntent, READ_CONTACT);
		} else {
			Amplitude.getInstance().logEvent(getString(R.string.contact_perm_denied));
			UIHelper.flashMessage(getContext(), getResources().getString(R.string.contact_perm_error));
		}
	}
}

