package com.hover.stax.buyAirtime;

import android.content.Intent;
import android.os.Bundle;
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
import com.hover.stax.ApplicationInstance;
import com.hover.stax.R;
import com.hover.stax.actions.Action;
import com.hover.stax.channels.Channel;
import com.hover.stax.database.KeyStoreExecutor;
import com.hover.stax.permission.PermissionScreenActivity;
import com.hover.stax.utils.UIHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BuyAirtimeFragment extends Fragment {

	private BuyAirtimeViewModel buyAirtimeViewModel;
	private AppCompatSpinner spinnerTo;
	private AppCompatSpinner spinnerFrom;
	private List<Integer> fromChannelIdList;
	private List<String> encryptedPins;
	private String selectedChannelEncryptedPin = null;
	private AirtimeActionModel airtimeActionModel;
	private String finalChosenActionId;
	private boolean airtimeIsToSelf = true;
	private String recipientNumber;


	public View onCreateView(@NonNull LayoutInflater inflater,
							 ViewGroup container, Bundle savedInstanceState) {
		buyAirtimeViewModel = new ViewModelProvider(this).get(BuyAirtimeViewModel.class);
		View root = inflater.inflate(R.layout.fragment_buyairtime, container, false);
		EditText amountEdit = root.findViewById(R.id.airtimeAmountEditId);
		TextView recipientLabel = root.findViewById(R.id.airtime_recipientLabel);
		EditText recipientEdit = root.findViewById(R.id.airtimeToEditId);
		airtimeActionModel = new AirtimeActionModel();
		spinnerTo = root.findViewById(R.id.toSpinner);
		spinnerFrom = root.findViewById(R.id.fromSpinner);

		fromChannelIdList = new ArrayList<>();
		encryptedPins = new ArrayList<>();
		loadInitialSpinners();



		buyAirtimeViewModel.getSelectedChannels().observe(getViewLifecycleOwner(), channels -> {

			ArrayList<String> channelNames = new ArrayList<>();
			channelNames.add("Choose an account");
			fromChannelIdList.add(0);
			encryptedPins.add(null);
			for (Channel model : channels) {
				channelNames.add(model.name);
				encryptedPins.add(model.pin);
				fromChannelIdList.add(model.id);
			}
			channelNames.add("+ Add a service");
			fromChannelIdList.add(0);
			encryptedPins.add(null);
			UIHelper.loadSpinnerItems(channelNames, spinnerFrom, getContext());

		});

		buyAirtimeViewModel.getAirtimeActions().observe(getViewLifecycleOwner(), actionList -> {
			Log.d("ACTIONS: ", "START CHECKING ACTIONS");
			for(Action action : actionList) {
				Log.d("ACTIONS: ", "my action "+action.public_id);
			}
			this.airtimeActionModel = buyAirtimeViewModel.getAirtimeActionIds(actionList);

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
						buyAirtimeViewModel.setAirtimeActions(tappedChannelId);
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
				else {
					airtimeIsToSelf = false;
					recipientEdit.setVisibility(View.VISIBLE);
					recipientLabel.setVisibility(View.VISIBLE);

					if(airtimeActionModel.getToOthersActionId() !=null && !airtimeActionModel.getToOthersActionId().isEmpty()) {
						finalChosenActionId = airtimeActionModel.getToOthersActionId();
					}
					else UIHelper.flashMessage(getContext(), "Please try another option");
				}



			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}
		});

		root.findViewById(R.id.buyAirtimeContinueButton).setOnClickListener(view3->{
			if(finalChosenActionId != null) {
			String amount = amountEdit.getText().toString();
			if(recipientEdit == null) {
				recipientNumber = recipientEdit.getText().toString();
			}
				if(TextUtils.getTrimmedLength(amount) > 0) {
					if(!airtimeIsToSelf ) {
						if(TextUtils.getTrimmedLength(recipientNumber) > 0 ) { }
						else UIHelper.flashMessage(getContext(), "Please enter recipient number and amount");
					}

					else {}
				}
				else UIHelper.flashMessage(getContext(), "Please enter amount");
			}else UIHelper.flashMessage(getContext(), "Please select a service");
		});


		return root;
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
		fromInit.add("Choose an account");
		fromInit.add("+  Add a service");
		UIHelper.loadSpinnerItems(fromInit, spinnerFrom, getContext());

		ArrayList<String> fromTo = new ArrayList<>();
		fromTo.add("Airtime to self");
		fromTo.add("Someone in my contact");
		fromTo.add("Manually enter phone number");
		UIHelper.loadSpinnerItems(fromTo, spinnerTo, getContext());
	}
}

