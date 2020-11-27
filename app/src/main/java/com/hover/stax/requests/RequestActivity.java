package com.hover.stax.requests;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.amplitude.api.Amplitude;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.hover.sdk.permissions.PermissionHelper;
import com.hover.stax.R;
import com.hover.stax.database.Constants;
import com.hover.stax.schedules.Schedule;
import com.hover.stax.schedules.ScheduleDetailViewModel;
import com.hover.stax.utils.PermissionUtils;
import com.hover.stax.utils.StagedViewModel;
import com.hover.stax.utils.UIHelper;
import com.hover.stax.utils.Utils;
import com.hover.stax.views.StaxDialog;

import static com.hover.stax.requests.RequestStage.*;

public class RequestActivity extends AppCompatActivity implements SmsSentObserver.SmsSentListener {
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
		if (requestViewModel.getStarted().getValue() != null && requestViewModel.getStarted().getValue())
			showRequestNotSentDialog();
	}

	private void showRequestNotSentDialog() {
		dialog = new StaxDialog(this)
			.setDialogTitle(R.string.reqcancel_head)
			.setDialogMessage(R.string.reqcancel_msg)
			.setNegButton(R.string.btn_saveanyway, btn -> onFinished(-1))
			.setPosButton(R.string.btn_cancel, btn ->  cancel())
			.isDestructive()
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
			sendSMS(null);
	}



	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (requestCode == Constants.SMS && new PermissionHelper(this).permissionsGranted(grantResults)) {
			Amplitude.getInstance().logEvent(getString(R.string.sms_perm_success));
			sendSMS(null);
		} else if (requestCode == Constants.SMS) {
			Amplitude.getInstance().logEvent(getString(R.string.sms_perm_denied));
			UIHelper.flashMessage(this, getResources().getString(R.string.toast_error_smsperm));
		}
	}

	public void sendSMS(View view) { Request.sendUsingSms(requestViewModel, this, this, this); }
	public void sendWhatsapp(View view) { requestViewModel.getCountryAlphaAndSendWithWhatsApp(this, this); }
	public void copyShareLink(View view) {
		ImageView copyImage = view.findViewById(R.id.copyLinkImage);
			if (Utils.copyToClipboard(requestViewModel.generateSMS(), this)) {
				requestViewModel.saveToDatabase();
				copyImage.setActivated(true);
				copyImage.setImageResource(R.drawable.copy_icon_white);
				TextView copyLabel = view.findViewById(R.id.copyLinkText);
				copyLabel.setText(getString(R.string.link_copied_label));
			} else copyImage.setActivated(false);
	}

	@Override
	public void onSmsSendEvent(boolean wasSent) {
		if (wasSent) onFinished(Constants.SMS);
	}

	private void onUpdateStage(@Nullable StagedViewModel.StagedEnum stage) {
		if (Navigation.findNavController(this, R.id.nav_host_fragment).getCurrentDestination().getId() == R.id.navigation_edit)
			((ExtendedFloatingActionButton) findViewById(R.id.fab)).hide();
		else if (findViewById(R.id.recipientRow) != null) {
			setSummaryCard(stage);
			setCurrentCard(stage);
			setFab(stage);
		}
	}

	private void setSummaryCard(@Nullable StagedViewModel.StagedEnum stage) {
		findViewById(R.id.recipientRow).setVisibility(stage.compare(RECIPIENT) > 0 ? View.VISIBLE : View.GONE);
		findViewById(R.id.amountRow).setVisibility(stage.compare(AMOUNT) > 0 && requestViewModel.getAmount().getValue() != null ? View.VISIBLE : View.GONE);
		findViewById(R.id.requesteeChannelRow).setVisibility(stage.compare(RECEIVING_ACCOUNT_INFO) > 0 && requestViewModel.getActiveChannel().getValue() != null ? View.VISIBLE : View.GONE);
		findViewById(R.id.noteRow).setVisibility((stage.compare(NOTE) > 0 && requestViewModel.getNote().getValue() != null && !requestViewModel.getNote().getValue().isEmpty()) ? View.VISIBLE : View.GONE);
		findViewById(R.id.btnRow).setVisibility(stage.compare(RECIPIENT) > 0 ? View.VISIBLE : View.GONE);
	}

	private void setCurrentCard(StagedViewModel.StagedEnum stage) {
//		findViewById(R.id.summaryCard).setVisibility(stage.compare(RECIPIENT) > 0 ? View.VISIBLE : View.GONE);
		findViewById(R.id.amountCard).setVisibility(stage.compare(AMOUNT) == 0 ? View.VISIBLE : View.GONE);
		findViewById(R.id.recipientCard).setVisibility(stage.compare(RECIPIENT) == 0 ? View.VISIBLE : View.GONE);
		findViewById(R.id.receiving_account_infoCard).setVisibility(stage.compare(RECEIVING_ACCOUNT_INFO) == 0 ?View.VISIBLE : View.GONE);
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

	@Override
	public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_CANCELED) {
			return;
		}
		if (requestCode == Constants.SMS) {
			onFinished(requestCode);
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
			    !Navigation.findNavController(findViewById(R.id.nav_host_fragment)).popBackStack())
			super.onBackPressed();
	}

	protected void onStop() {
		super.onStop();
		if (dialog != null) {
			dialog.dismiss();
			dialog = null;
		}
	}
}
