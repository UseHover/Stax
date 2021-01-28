package com.hover.stax.transfers;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.amplitude.api.Amplitude;
import com.hover.stax.R;
import com.hover.stax.actions.Action;
import com.hover.stax.channels.ChannelDropdownViewModel;
import com.hover.stax.contacts.StaxContact;
import com.hover.stax.database.Constants;
import com.hover.stax.hover.HoverSession;
import com.hover.stax.schedules.Schedule;
import com.hover.stax.schedules.ScheduleDetailViewModel;
import com.hover.stax.settings.BiometricChecker;
import com.hover.stax.views.StaxDialog;

public class TransferActivity extends AppCompatActivity implements BiometricChecker.AuthListener {
	final public static String TAG = "TransferActivity";

	private ChannelDropdownViewModel channelDropdownViewModel;
	private TransferViewModel transferViewModel;
	private ScheduleDetailViewModel scheduleViewModel = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		channelDropdownViewModel = new ViewModelProvider(this).get(ChannelDropdownViewModel.class);
		transferViewModel = new ViewModelProvider(this).get(TransferViewModel.class);

		startObservers();
		checkIntent();
		setContentView(R.layout.activity_transfer);
	}

	private void startObservers() {
		transferViewModel.setType(getIntent().getAction());
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
			if (action != null) transferViewModel.setActiveAction(action);
		});
		scheduleViewModel.getSchedule().observe(this, schedule -> {
			if (schedule == null) return;
			transferViewModel.view(schedule);
		});
		scheduleViewModel.setSchedule(schedule_id);
		Amplitude.getInstance().logEvent(getString(R.string.clicked_schedule_notification));
	}

	private void createFromRequest(String link) {
		AlertDialog dialog = new StaxDialog(this).setDialogMessage(R.string.loading_dialoghead).showIt();
		transferViewModel.decrypt(link);
		transferViewModel.getRequest().observe(this, request -> {
			if (request == null) return;
			transferViewModel.view(request);
			if (dialog != null) dialog.dismiss();
		});
		Amplitude.getInstance().logEvent(getString(R.string.clicked_request_link));
	}

	private void submit() {
		transferViewModel.saveContact();
		authenticate();
	}

	private void authenticate() {
		makeHoverCall(transferViewModel.getActiveAction().getValue());
//		new BiometricChecker(this, this).startAuthentication(transferViewModel.getActiveAction().getValue());
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
		Amplitude.getInstance().logEvent(getString(R.string.finish_transfer, transferViewModel.getType()));
		transferViewModel.checkSchedule();
		makeCall(act);
	}
	private void makeCall(Action act) {
		HoverSession.Builder hsb = new HoverSession.Builder(act, channelDropdownViewModel.getActiveChannel(),
				TransferActivity.this, Constants.TRANSFER_REQUEST)
				.extra(Action.AMOUNT_KEY, transferViewModel.getAmount().getValue())
				.extra(Action.NOTE_KEY, transferViewModel.getNote().getValue());

		if (transferViewModel.getContact().getValue() != null) { addRecipientInfo(hsb); }
		hsb.run();
	}
	private void addRecipientInfo(HoverSession.Builder hsb) {
		hsb.extra(Action.ACCOUNT_KEY, transferViewModel.getContact().getValue().phoneNumber)
			.extra(Action.PHONE_KEY, transferViewModel.getContact().getValue().getNumberFormatForInput(transferViewModel.getActiveAction().getValue(), channelDropdownViewModel.getActiveChannel()));
	}

	private void setReasonEditTextVisibility() {
		findViewById(R.id.reasonEditText).setVisibility(transferViewModel.getActiveAction().getValue()!= null && transferViewModel.getActiveAction().getValue().allowsNote() ? View.VISIBLE : View.GONE);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == Constants.TRANSFER_REQUEST) {
			returnResult(requestCode, resultCode, data);
		} else if (requestCode == Constants.ADD_SERVICE)
			startObservers();
	}

	private void returnResult(int type, int result, Intent data) {
		Intent i = data == null ? new Intent() : new Intent(data);
		if (transferViewModel.getContact().getValue() != null)
			i.putExtra(StaxContact.ID_KEY, transferViewModel.getContact().getValue().id);
		i.setAction(type == Constants.SCHEDULE_REQUEST ? Constants.SCHEDULED : Constants.TRANSFERED);
		setResult(result, i);
		finish();
	}
}