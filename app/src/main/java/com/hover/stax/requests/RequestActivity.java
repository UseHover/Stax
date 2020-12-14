package com.hover.stax.requests;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.amplitude.api.Amplitude;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.hover.sdk.permissions.PermissionHelper;
import com.hover.stax.R;
import com.hover.stax.database.Constants;
import com.hover.stax.schedules.Schedule;
import com.hover.stax.schedules.ScheduleDetailViewModel;
import com.hover.stax.utils.StagedViewModel;
import com.hover.stax.utils.UIHelper;
import com.hover.stax.utils.Utils;
import com.hover.stax.views.StaxDialog;

import static com.hover.stax.requests.RequestStage.*;

public class RequestActivity extends AbstractMessageSendingActivity implements SmsSentObserver.SmsSentListener {
	final public static String TAG = "TransferActivity";

	private NewRequestViewModel requestViewModel;
	private ScheduleDetailViewModel scheduleViewModel = null;

	AlertDialog dialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_request);
		requestViewModel = new ViewModelProvider(this).get(NewRequestViewModel.class);
		startObservers();
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

	private void startObservers() {
		requestViewModel.getStage().observe(this, this::onUpdateStage);
		requestViewModel.getAmount().observe(this, amount -> onUpdateStage(requestViewModel.getStage().getValue()));
		requestViewModel.getNote().observe(this, note -> onUpdateStage(requestViewModel.getStage().getValue()));
		requestViewModel.getIsFuture().observe(this, isFuture -> onUpdateStage(requestViewModel.getStage().getValue()));
		requestViewModel.getFutureDate().observe(this, date -> onUpdateStage(requestViewModel.getStage().getValue()));
		requestViewModel.repeatSaved().observe(this, isSaved -> onUpdateStage(requestViewModel.getStage().getValue()));
		requestViewModel.getIsEditing().observe(this, isEditing -> onUpdateStage(requestViewModel.getStage().getValue()));
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

	public void onContinue(View view) {
		if (requestViewModel.isDone())
			submit();
		else if (requestViewModel.stageValidates())
			requestViewModel.goToNextStage();
	}

	private void submit() {
		if (requestViewModel.getIsFuture().getValue() != null && requestViewModel.getIsFuture().getValue() && requestViewModel.getFutureDate().getValue() != null) {
			requestViewModel.schedule();
			onFinished(Constants.SCHEDULE_REQUEST);
		} else
			sendSms(null);
	}

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
		super.sendSms(view);
	}

	public void sendWhatsapp(View view) {
		start();
		Amplitude.getInstance().logEvent(getString(R.string.clicked_send_whatsapp_request));
		super.sendWhatsapp(view);
	}

	public void copyShareLink(View view) {
		start();
		Amplitude.getInstance().logEvent(getString(R.string.clicked_copylink_request));
		super.copyShareLink(view);
	}

	private void start() {
		requestViewModel.setStarted();
		channel = requestViewModel.getActiveChannel().getValue();
		currentRequest = requestViewModel.getRequest().getValue();
		requestees = requestViewModel.getRequestees().getValue();
	}

	@Override
	public void onSmsSendEvent(boolean wasSent) {
		if (wasSent) onFinished(Constants.SMS);
	}

	private void onUpdateStage(@Nullable StagedViewModel.StagedEnum stage) {
		if (Navigation.findNavController(this, R.id.nav_host_fragment).getCurrentDestination().getId() == R.id.navigation_edit)
			((ExtendedFloatingActionButton) findViewById(R.id.fab)).hide();
		else if (findViewById(R.id.requesteeRow) != null) {
			setSummaryCard(stage);
			setCurrentCard(stage);
			setFab(stage);
		}
	}

	private void setSummaryCard(@Nullable StagedViewModel.StagedEnum stage) {
		findViewById(R.id.amountRow).setVisibility(stage.compare(AMOUNT) > 0 && requestViewModel.getAmount().getValue() != null ? View.VISIBLE : View.GONE);
		findViewById(R.id.requesteeRow).setVisibility(stage.compare(REQUESTEE) > 0 ? View.VISIBLE : View.GONE);
		findViewById(R.id.requesterAccountRow).setVisibility(stage.compare(REQUESTER) > 0 && requestViewModel.getActiveChannel().getValue() != null ? View.VISIBLE : View.GONE);
		findViewById(R.id.noteRow).setVisibility((stage.compare(NOTE) > 0 && requestViewModel.getNote().getValue() != null && !requestViewModel.getNote().getValue().isEmpty()) ? View.VISIBLE : View.GONE);
		findViewById(R.id.btnRow).setVisibility(stage.compare(REQUESTEE) > 0 ? View.VISIBLE : View.GONE);
	}

	private void setCurrentCard(StagedViewModel.StagedEnum stage) {
		findViewById(R.id.recipientCard).setVisibility(stage.compare(REQUESTEE) == 0 ? View.VISIBLE : View.GONE);
		findViewById(R.id.amountCard).setVisibility(stage.compare(AMOUNT) == 0 ? View.VISIBLE : View.GONE);
		findViewById(R.id.requesterCard).setVisibility(stage.compare(REQUESTER) == 0 ? View.VISIBLE : View.GONE);
		findViewById(R.id.noteCard).setVisibility(stage.compare(NOTE) == 0 ? View.VISIBLE : View.GONE);
		findViewById(R.id.shareCard).setVisibility(stage.compare(REVIEW) >= 0 ? View.VISIBLE : View.GONE);
		findViewById(R.id.futureCard).setVisibility(stage.compare(REVIEW_DIRECT) < 0 && requestViewModel.getFutureDate().getValue() == null ? View.VISIBLE : View.GONE);
		findViewById(R.id.repeatCard).setVisibility(stage.compare(REVIEW_DIRECT) < 0 && (requestViewModel.repeatSaved().getValue() == null || !requestViewModel.repeatSaved().getValue()) ? View.VISIBLE : View.GONE);
	}

	private void setFab(StagedViewModel.StagedEnum stage) {
		ExtendedFloatingActionButton fab = findViewById(R.id.fab);
		if (stage.compare(REVIEW) >= 0) {
			if (requestViewModel.getIsFuture().getValue() != null && requestViewModel.getIsFuture().getValue()) {
				fab.setText(getString(R.string.fab_schedule));
				if (requestViewModel.getFutureDate().getValue() == null) { fab.hide(); } else { fab.show(); }
			} else {
				fab.setText(getString(R.string.notify_request_cta));
				fab.hide();
			}
		} else {
			fab.setText(R.string.btn_continue);
			fab.show();
		}
	}

	private void onFinished(int type) {
		requestViewModel.saveToDatabase();
		setResult(RESULT_OK, createSuccessIntent(type));
		finish();
	}

	private Intent createSuccessIntent(int type) {
		Intent i = new Intent();
		if (type == Constants.SCHEDULE_REQUEST)
			i.putExtra(Schedule.DATE_KEY, requestViewModel.getFutureDate().getValue());
		i.setAction(type == Constants.SCHEDULE_REQUEST ? Constants.SCHEDULED : Constants.TRANSFERED);
		return i;
	}

	private void cancel() {
		setResult(RESULT_CANCELED);
		finish();
	}

	@Override
	public void onBackPressed() {
		if (Navigation.findNavController(findViewById(R.id.nav_host_fragment)).getCurrentDestination().getId() != R.id.navigation_edit ||
			    !Navigation.findNavController(findViewById(R.id.nav_host_fragment)).popBackStack()) {
			if (requestViewModel.getStarted())
				wasRequestSentDialog();
			else
				super.onBackPressed();
		}
	}

	protected void onStop() {
		super.onStop();
		if (dialog != null) {
			dialog.dismiss();
			dialog = null;
		}
	}
}
