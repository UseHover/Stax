package com.hover.stax.buyAirtime;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.hover.sdk.api.HoverParameters;
import com.hover.sdk.permissions.PermissionActivity;
import com.hover.stax.ApplicationInstance;
import com.hover.stax.R;
import com.hover.stax.actions.Action;
import com.hover.stax.channels.Channel;
import com.hover.stax.database.KeyStoreExecutor;
import com.hover.stax.permission.PermissionScreenActivity;
import com.hover.stax.utils.PermissionUtils;
import com.hover.stax.utils.UIHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BuyAirtimeFragment extends Fragment {

	private BuyAirtimeViewModel buyAirtimeViewModel;
	private AppCompatSpinner spinnerTo;
	private AppCompatSpinner spinnerFrom;
	private TextView recipientLabel;
	private EditText recipientEdit;

	private List<Integer> fromChannelIdList;
	private List<String> encryptedPins;
	private String selectedChannelEncryptedPin = null;
	private AirtimeActionModel airtimeActionModel;
	private String finalChosenActionId;
	private boolean airtimeIsToSelf = true;
	private String recipientNumber;
	private final int READ_CONTACT = 202;
	private final int PERMISSION_REQ_CODE = 201;


	public View onCreateView(@NonNull LayoutInflater inflater,
							 ViewGroup container, Bundle savedInstanceState) {
		buyAirtimeViewModel = new ViewModelProvider(this).get(BuyAirtimeViewModel.class);
		View root = inflater.inflate(R.layout.fragment_buyairtime, container, false);
		EditText amountEdit = root.findViewById(R.id.airtimeAmountEditId);
		recipientLabel = root.findViewById(R.id.airtime_recipientLabel);
		recipientEdit = root.findViewById(R.id.airtimeToEditId);
		airtimeActionModel = new AirtimeActionModel();
		spinnerTo = root.findViewById(R.id.toSpinner);
		spinnerFrom = root.findViewById(R.id.fromSpinner);

		fromChannelIdList = new ArrayList<>();
		encryptedPins = new ArrayList<>();
		loadInitialSpinners();



		buyAirtimeViewModel.getSelectedChannels().observe(getViewLifecycleOwner(), channels -> {

			ArrayList<String> channelNames = new ArrayList<>();
			channelNames.add(getResources().getString(R.string.choose_account_initial));
			fromChannelIdList.add(0);
			encryptedPins.add(null);

			for (Channel model: channels) {
				channelNames.add(model.name);
				encryptedPins.add(model.pin);
				fromChannelIdList.add(model.id);
			}
			channelNames.add(getResources().getString(R.string.addAService));
			fromChannelIdList.add(0);
			encryptedPins.add(null);
			UIHelper.loadSpinnerItems(channelNames, spinnerFrom, getContext());

		});



		spinnerFrom.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				AppCompatTextView textView = (AppCompatTextView) parent.getChildAt(0);
				if (textView != null) {
					textView.setTextColor(getResources().getColor(R.color.white));
				}

				if (position != 0) {
					if(position == fromChannelIdList.size() - 1) startActivity(new Intent(getActivity(), PermissionScreenActivity.class));
					else {
						int tappedChannelId = fromChannelIdList.get(position);
						selectedChannelEncryptedPin = encryptedPins.get(position);
						buyAirtimeViewModel.getAirtimeActions(tappedChannelId).observe(getViewLifecycleOwner(), actionList -> {
							airtimeActionModel = buyAirtimeViewModel.getAirtimeActionModel(actionList);
						});

					}
				} else finalChosenActionId = null;
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}
		});



		spinnerTo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				AppCompatTextView textView = (AppCompatTextView) parent.getChildAt(0);
				if (textView != null) {
					textView.setTextColor(getResources().getColor(R.color.white));
				}

				if(position == 0) {
					airtimeIsToSelf = true;
					recipientEdit.setVisibility(View.GONE);
					recipientLabel.setVisibility(View.GONE);


					if(airtimeActionModel.getToSelfActionId() !=null && !airtimeActionModel.getToSelfActionId().isEmpty()) {
						finalChosenActionId = airtimeActionModel.getToSelfActionId();
					} else {
						finalChosenActionId = airtimeActionModel.getToOthersActionId();
						recipientEdit.setVisibility(View.VISIBLE);
						recipientLabel.setVisibility(View.VISIBLE);
					}

				}
				else if(position == 1) {
					if(PermissionUtils.hasContactPermission()) {
						Intent contactPickerIntent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
						startActivityForResult(contactPickerIntent,READ_CONTACT);
					} else {
						startActivityForResult(new Intent(getContext(), PermissionActivity.class), PERMISSION_REQ_CODE);
					}
				}
				else {
					settingsForAirtimeForOthers();
				}

			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}
		});



		root.findViewById(R.id.buyAirtimeContinueButton).setOnClickListener(view3->{
			if(finalChosenActionId != null) {
			String amount = amountEdit.getText().toString();
				recipientNumber = recipientEdit.getText().toString();
				if(TextUtils.getTrimmedLength(amount) > 0) {
					if(!airtimeIsToSelf ) {
						if(TextUtils.getTrimmedLength(recipientNumber) > 0 ) { makeHoverCall(finalChosenActionId, amount);}
						else UIHelper.flashMessage(getContext(), getResources().getString(R.string.enterRecipientNumberError));
					}

					else {makeHoverCall(finalChosenActionId, amount);}
				}
				else UIHelper.flashMessage(getContext(), getResources().getString(R.string.enterAmountError));
			}else UIHelper.flashMessage(getContext(), getResources().getString(R.string.selectServiceError));
		});


		return root;
	}

	private void settingsForAirtimeForOthers() {
		airtimeIsToSelf = false;
		recipientEdit.setVisibility(View.VISIBLE);
		recipientLabel.setVisibility(View.VISIBLE);

		if(airtimeActionModel.getToOthersActionId() !=null && !airtimeActionModel.getToOthersActionId().isEmpty()) {
			finalChosenActionId = airtimeActionModel.getToOthersActionId();
		}
		else UIHelper.flashMessage(getContext(), getResources().getString(R.string.enterAnotherOptionError));
	}


	private void makeHoverCall(String actionId, String amount) {
		HoverParameters.Builder builder = new HoverParameters.Builder(getContext());
		builder.request(actionId);
		//builder.setEnvironment(HoverParameters.PROD_ENV);
		builder.style(R.style.myHoverTheme);
		builder.extra("recipientNumber", recipientNumber);
		builder.extra("amount", amount);
		builder.finalMsgDisplayTime(2000);
		builder.extra("pin", KeyStoreExecutor.decrypt(selectedChannelEncryptedPin, ApplicationInstance.getContext()));
		Intent i = builder.buildIntent();
		int AIRTIME_RUN = 203;
		startActivityForResult(i, AIRTIME_RUN);
	}

	private void loadInitialSpinners() {
		ArrayList<String> fromInit = new ArrayList<>();
		fromInit.add(getResources().getString(R.string.choose_account_initial));
		fromInit.add(getResources().getString(R.string.addAService));
		UIHelper.loadSpinnerItems(fromInit, spinnerFrom, getContext());

		ArrayList<String> fromTo = new ArrayList<>();
		fromTo.add(getResources().getString(R.string.airtimeToSelf));
		fromTo.add(getResources().getString(R.string.airtimeMyContact));
		fromTo.add(getResources().getString(R.string.airtimeElse));
		UIHelper.loadSpinnerItems(fromTo, spinnerTo, getContext());
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

				if (requestCode == READ_CONTACT && resultCode == Activity.RESULT_OK) {
					Uri contactData = data.getData();
					if(contactData !=null && getContext() !=null) {
						Cursor cur =  getContext().getContentResolver().query(contactData, null, null, null, null);
						if(cur!=null) {
							if (cur.getCount() > 0) {// thats mean some resutl has been found
								if(cur.moveToNext()) {
									String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
									String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

									if (Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {

										Cursor phones = getContext().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = "+ id,null, null);
										if (phones !=null) {
											while (phones.moveToNext()) {
												String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
												UIHelper.flashMessage(getContext(),getView(), "Selected:     "+name+" - "+ phoneNumber);
												recipientEdit.setText(phoneNumber);
												settingsForAirtimeForOthers();
											}
											phones.close();
										} else UIHelper.flashMessage(getContext(), getResources().getString(R.string.selectContactErrorMessage));// ShowError

									}

								}
							}
							cur.close();
						} else UIHelper.flashMessage(getContext(), getResources().getString(R.string.selectContactErrorMessage));///error

					}
					else UIHelper.flashMessage(getContext(), getResources().getString(R.string.selectContactErrorMessage));///showError


				}
				else if(requestCode == PERMISSION_REQ_CODE && resultCode == Activity.RESULT_OK) {
					Intent contactPickerIntent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
					startActivityForResult(contactPickerIntent,READ_CONTACT);
				}

	}
}

