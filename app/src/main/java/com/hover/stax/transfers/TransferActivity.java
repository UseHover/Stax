package com.hover.stax.transfers;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;

import com.amplitude.api.Amplitude;
import com.hover.sdk.actions.HoverAction;
import com.hover.stax.R;
import com.hover.stax.actions.ActionSelectViewModel;
import com.hover.stax.channels.ChannelDropdownViewModel;
import com.hover.stax.contacts.StaxContact;
import com.hover.stax.navigation.AbstractNavigationActivity;
import com.hover.stax.utils.Constants;
import com.hover.stax.hover.HoverSession;
import com.hover.stax.schedules.Schedule;
import com.hover.stax.schedules.ScheduleDetailViewModel;
import com.hover.stax.views.StaxDialog;

public class TransferActivity extends AbstractNavigationActivity {
	final public static String TAG = "TransferActivity";

	private ChannelDropdownViewModel channelDropdownViewModel;
	private ActionSelectViewModel actionSelectViewModel;
	private TransferViewModel transferViewModel;
	private ScheduleDetailViewModel scheduleViewModel = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		channelDropdownViewModel = new ViewModelProvider(this).get(ChannelDropdownViewModel.class);
		actionSelectViewModel = new ViewModelProvider(this).get(ActionSelectViewModel.class);
		transferViewModel = new ViewModelProvider(this).get(TransferViewModel.class);
		transferViewModel.setType(getIntent().getAction());
		channelDropdownViewModel.setType(getIntent().getAction());

		checkIntent();
		setContentView(R.layout.activity_transfer);
		setUpNav();
	}

	private void checkIntent() {
		if (getIntent().hasExtra(Schedule.SCHEDULE_ID))
			createFromSchedule(getIntent().getIntExtra(Schedule.SCHEDULE_ID, -1));
		else if (getIntent().hasExtra(Constants.REQUEST_LINK))
			createFromRequest(getIntent().getStringExtra(Constants.REQUEST_LINK));
		else
			Amplitude.getInstance().logEvent(getString(R.string.visit_screen, getIntent().getAction()));
	}

	private void createFromSchedule(int schedule_id) {
		scheduleViewModel = new ViewModelProvider(this).get(ScheduleDetailViewModel.class);
		scheduleViewModel.getAction().observe(this, action -> {
			if (action != null) actionSelectViewModel.setActiveAction(action);
		});
		scheduleViewModel.getSchedule().observe(this, schedule -> {
			if (schedule == null) return;
			transferViewModel.view(schedule);
		});
		scheduleViewModel.setSchedule(schedule_id);
		Amplitude.getInstance().logEvent(getString(R.string.clicked_schedule_notification));
	}

	private void createFromRequest(String link) {
		transferViewModel.decrypt(link);
		observeRequest();
		observeActiveChannel();
		Amplitude.getInstance().logEvent(getString(R.string.clicked_request_link));
	}

	private void observeRequest() {
		AlertDialog dialog = new StaxDialog(this).setDialogMessage(R.string.loading_link_dialoghead).showIt();
		transferViewModel.getRequest().observe(this, request -> {
			Log.e(TAG, "maybe viewing request");
			if (request == null) return;

			Log.e(TAG, "viewing request " + request);
			if (dialog != null) {
				transferViewModel.setRecipientSmartly(request, channelDropdownViewModel.getActiveChannel().getValue());
				dialog.dismiss();
			}
		});
	}
	private void observeActiveChannel() {
		channelDropdownViewModel.getActiveChannel().observe(this, channel -> {
			transferViewModel.setRecipientSmartly(transferViewModel.getRequest().getValue(), channelDropdownViewModel.getActiveChannel().getValue());
		});
	}


	void submit() {
		makeHoverCall(actionSelectViewModel.getActiveAction().getValue());
	}

	private void makeHoverCall(HoverAction act) {
		Amplitude.getInstance().logEvent(getString(R.string.finish_transfer, transferViewModel.getType()));
		transferViewModel.checkSchedule();
		makeCall(act);
	}

	private void makeCall(HoverAction act) {
		HoverSession.Builder hsb = new HoverSession.Builder(act, channelDropdownViewModel.getActiveChannel().getValue(),
				TransferActivity.this, Constants.TRANSFER_REQUEST)
				.extra(HoverAction.AMOUNT_KEY, transferViewModel.getAmount().getValue())
				.extra(HoverAction.NOTE_KEY, transferViewModel.getNote().getValue());

		if (transferViewModel.getContact().getValue() != null) { addRecipientInfo(hsb); }
		hsb.run();
	}
	private void addRecipientInfo(HoverSession.Builder hsb) {
		hsb.extra(HoverAction.ACCOUNT_KEY, transferViewModel.getContact().getValue().phoneNumber)
			.extra(HoverAction.PHONE_KEY, transferViewModel.getContact().getValue().getNumberFormatForInput(actionSelectViewModel.getActiveAction().getValue(), channelDropdownViewModel.getActiveChannel().getValue()));
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == Constants.TRANSFER_REQUEST)
			returnResult(requestCode, resultCode, data);
	}

	private void returnResult(int type, int result, Intent data) {
		Intent i = data == null ? new Intent() : new Intent(data);
		if (transferViewModel.getContact().getValue() != null)
			i.putExtra(StaxContact.LOOKUP_KEY, transferViewModel.getContact().getValue().lookupKey);
		i.setAction(type == Constants.SCHEDULE_REQUEST ? Constants.SCHEDULED : Constants.TRANSFERED);
		setResult(result, i);
		finish();
	}

	@Override
	public void onBackPressed() {
		if (!transferViewModel.getIsEditing().getValue()) transferViewModel.setEditing(true);
		else super.onBackPressed();
	}
}