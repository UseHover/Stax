package com.hover.stax.moveMoney.toSomeoneEsle;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.AdapterView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.lifecycle.ViewModelProvider;

import com.hover.sdk.permissions.PermissionActivity;
import com.hover.stax.R;
import com.hover.stax.buyAirtime.BuyAirtimeViewModel;
import com.hover.stax.channels.Channel;
import com.hover.stax.models.StaxContactModel;
import com.hover.stax.permission.PermissionScreenActivity;
import com.hover.stax.utils.PermissionUtils;
import com.hover.stax.utils.UIHelper;

import java.util.ArrayList;
import java.util.List;

public class ToSomeElseActivity extends AppCompatActivity {
	private AppCompatSpinner spinnerFrom, spinnerTo;
	private ToSomeElseViewModel toSomeElseViewModel;
	private final int READ_CONTACT = 202;
	private final int PERMISSION_REQ_CODE = 201;

	private List<Integer> fromChannelIdList;
	private List<String> encryptedPins;
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.move_money_inner_fragment);
		spinnerFrom = findViewById(R.id.bs_from);
		spinnerTo = findViewById(R.id.bs_to);

		loadInitialSpinners();
		fromChannelIdList = new ArrayList<>();
		encryptedPins = new ArrayList<>();


		toSomeElseViewModel = new ViewModelProvider(this).get(ToSomeElseViewModel.class);
		toSomeElseViewModel.getAllSelectedChannels().observe(ToSomeElseActivity.this, channels -> {
			ArrayList<String> channelNames = new ArrayList<>();
			channelNames.add(getResources().getString(R.string.choose_account_initial));
			fromChannelIdList.add(0);
			encryptedPins.add(null);

			for (Channel model : channels) {
				channelNames.add(model.name);
				encryptedPins.add(model.pin);
				fromChannelIdList.add(model.id);
			}
			channelNames.add(getResources().getString(R.string.addAService));
			fromChannelIdList.add(0);
			encryptedPins.add(null);
			UIHelper.loadSpinnerItems(channelNames, spinnerFrom, ToSomeElseActivity.this);
		});

		spinnerFrom.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				AppCompatTextView textView = (AppCompatTextView) parent.getChildAt(0);
				if (textView != null) {
					textView.setTextColor(getResources().getColor(R.color.white));
				}

				if (position != 0) {
					if (position == fromChannelIdList.size() - 1)
						startActivity(new Intent(ToSomeElseActivity.this, PermissionScreenActivity.class));
					else {
						int tappedChannelId = fromChannelIdList.get(position);

					}
				} //Add else later
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}
		});

		findViewById(R.id.pickContact).setOnClickListener(view->{
			if (PermissionUtils.hasContactPermission()) {
				Intent contactPickerIntent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
				startActivityForResult(contactPickerIntent, READ_CONTACT);
			} else {
				startActivityForResult(new Intent(ToSomeElseActivity.this, PermissionActivity.class), PERMISSION_REQ_CODE);
			}
		});

		findViewById(R.id.toSomeElseContinueButton).setOnClickListener(view-> {

		});
	}

	private void loadInitialSpinners() {
		ArrayList<String> init = new ArrayList<>();
		init.add(getResources().getString(R.string.choose_account_initial));
		init.add(getResources().getString(R.string.addAService));

		UIHelper.loadSpinnerItems(init, spinnerFrom, this);
		UIHelper.loadSpinnerItems(init, spinnerTo, this);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == READ_CONTACT && resultCode == Activity.RESULT_OK) {
			StaxContactModel staxContactModel = UIHelper.getContactInfo(data, getCurrentFocus());
			if (staxContactModel != null) {
				UIHelper.flashMessage(this, getCurrentFocus(), "Selected:     " + staxContactModel.getName() + " - " + staxContactModel.getPhoneNumber());
				String phone = staxContactModel.getPhoneNumber();
			} else
				UIHelper.flashMessage(this, getResources().getString(R.string.selectContactErrorMessage));///showError

		} else if (requestCode == PERMISSION_REQ_CODE && resultCode == Activity.RESULT_OK) {
			Intent contactPickerIntent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
			startActivityForResult(contactPickerIntent, READ_CONTACT);
		}

	}
}
