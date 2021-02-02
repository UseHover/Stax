package com.hover.stax.requests;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.amplitude.api.Amplitude;
import com.hover.sdk.permissions.PermissionHelper;
import com.hover.stax.R;
import com.hover.stax.database.Constants;
import com.hover.stax.schedules.Schedule;
import com.hover.stax.schedules.ScheduleDetailViewModel;
import com.hover.stax.utils.UIHelper;
import com.hover.stax.views.StaxDialog;


public class RequestActivity extends AppCompatActivity implements RequestSenderInterface, SmsSentObserver.SmsSentListener {
	final public static String TAG = "TransferActivity";

	private NewRequestViewModel requestViewModel;
	private ScheduleDetailViewModel scheduleViewModel = null;

	AlertDialog dialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_request);
		requestViewModel = new ViewModelProvider(this).get(NewRequestViewModel.class);
		checkIntent();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (requestViewModel.getStarted())
			wasRequestSentDialog();
	}

	private void wasRequestSentDialog() {
		dialog = new StaxDialog(this)
			.setDialogTitle(R.string.reqsave_head)
			.setDialogMessage(R.string.reqsave_msg)
			.setPosButton(R.string.btn_saveanyway, btn -> onFinished(-1))
			.setNegButton(R.string.btn_dontsave, btn ->  cancel())
			.showIt();
	}

	private void checkIntent() {
		if (getIntent().hasExtra(Schedule.SCHEDULE_ID))
			createFromSchedule(getIntent().getIntExtra(Schedule.SCHEDULE_ID, -1));
		else
			Amplitude.getInstance().logEvent(getString(R.string.visit_screen, getString(R.string.visit_new_request)));
	}

	private void createFromSchedule(int schedule_id) {
		scheduleViewModel = new ViewModelProvider(this).get(ScheduleDetailViewModel.class);
		scheduleViewModel.getSchedule().observe(this, schedule -> {
			if (schedule == null) return;
			requestViewModel.setSchedule(schedule);
		});
		scheduleViewModel.setSchedule(schedule_id);
		Amplitude.getInstance().logEvent(getString(R.string.clicked_schedule_notification));
	}

//	public void onContinue(View view) {
//		if (requestViewModel.isDone())
//			submit();
//		else if (requestViewModel.stageValidates())
//			requestViewModel.setStage(REVIEW);
//	}
//
//	private void submit() {
//		if (requestViewModel.getIsFuture().getValue() != null && requestViewModel.getIsFuture().getValue() && requestViewModel.getFutureDate().getValue() != null) {
//			requestViewModel.setStarted();
//			requestViewModel.schedule();
//			onFinished(Constants.SCHEDULE_REQUEST);
//		} else
//			sendSms(null);
//	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (requestCode == Constants.SMS && new PermissionHelper(this).permissionsGranted(grantResults)) {
			Amplitude.getInstance().logEvent(getString(R.string.sms_perm_success));
			sendSms(null);
		} else if (requestCode == Constants.SMS) {
			Amplitude.getInstance().logEvent(getString(R.string.sms_perm_denied));
			UIHelper.flashMessage(this, getResources().getString(R.string.toast_error_smsperm));
		}
	}

	public void sendSms(View view) {
		start();
		Amplitude.getInstance().logEvent(getString(R.string.clicked_send_sms_request));
		new SmsSentObserver(this, requestViewModel.getRequestees().getValue(), new Handler(), this).start();
		sendSms(requestViewModel.getRequest().getValue(), requestViewModel.getRequestees().getValue(), this);
	}

	public void sendWhatsapp(View view) {
		start();
		Amplitude.getInstance().logEvent(getString(R.string.clicked_send_whatsapp_request));
		sendWhatsapp(requestViewModel.getRequest().getValue(), requestViewModel.getRequestees().getValue(), null, this);
	}

	public void copyShareLink(View view) {
		start();
		Amplitude.getInstance().logEvent(getString(R.string.clicked_copylink_request));
		copyShareLink(requestViewModel.getRequest().getValue(), view.findViewById(R.id.copylink_share_selection), this);
	}

	private void start() {
		requestViewModel.setStarted();
	}

	@Override
	public void onSmsSendEvent(boolean wasSent) {
		if (wasSent) onFinished(Constants.SMS);
	}

	private void onFinished(int type) {
		requestViewModel.saveToDatabase();
		setResult(RESULT_OK, createSuccessIntent(type));
		finish();
	}

	private Intent createSuccessIntent(int type) {
		Intent i = new Intent();
		i.setAction(type == Constants.SCHEDULE_REQUEST ? Constants.SCHEDULED : Constants.TRANSFERED);
		return i;
	}

	private void cancel() {
		setResult(RESULT_CANCELED);
		finish();
	}

	protected void onStop() {
		super.onStop();
		if (dialog != null) {
			dialog.dismiss();
			dialog = null;
		}
	}
}
