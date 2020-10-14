package com.hover.stax.requests;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.amplitude.api.Amplitude;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.hover.stax.R;
import com.hover.stax.schedules.Schedule;
import com.hover.stax.schedules.ScheduleDetailViewModel;
import com.hover.stax.utils.StagedViewModel;

import static com.hover.stax.requests.RequestStage.AMOUNT;
import static com.hover.stax.requests.RequestStage.NOTE;
import static com.hover.stax.requests.RequestStage.RECIPIENT;
import static com.hover.stax.requests.RequestStage.REVIEW;
import static com.hover.stax.requests.RequestStage.REVIEW_DIRECT;

public class RequestActivity extends AppCompatActivity {
	final public static String TAG = "TransferActivity";
	final public static int REQUEST_REQUEST = 301;
	private final static int SEND_SMS = 302;
	private final static int SEND_SMS_FOREGROUND = 303;

	private NewRequestViewModel requestViewModel;
	private ScheduleDetailViewModel scheduleViewModel = null;

	private boolean allowSchedule = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestViewModel = new ViewModelProvider(this).get(NewRequestViewModel.class);
		startObservers();
		checkIntent();
		setContentView(R.layout.activity_request);
	}

	private void startObservers() {
		requestViewModel.getStage().observe(this, this::onUpdateStage);
		requestViewModel.getAmount().observe(this, amount -> onUpdateStage(requestViewModel.getStage().getValue()));
		requestViewModel.getNote().observe(this, note -> onUpdateStage(requestViewModel.getStage().getValue()));
		requestViewModel.getIsFuture().observe(this, isFuture -> onUpdateStage(requestViewModel.getStage().getValue()));
		requestViewModel.getFutureDate().observe(this, date -> onUpdateStage(requestViewModel.getStage().getValue()));
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
			requestViewModel.setAmount(schedule.amount);
			requestViewModel.addRecipient(schedule.recipient);
			requestViewModel.setNote(schedule.reason);
			allowSchedule = false;
			requestViewModel.setStage(REVIEW_DIRECT);
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

	@SuppressLint("NewApi")
	private void submit() {
		requestViewModel.saveToDatabase();
		Intent i = new Intent(android.content.Intent.ACTION_VIEW);
		i.setType("vnd.android-dir/mms-sms");
		i.putExtra("address", requestViewModel.generateRecipientString());
		i.putExtra("sms_body", requestViewModel.generateSMS(this));
		startActivityForResult(i, SEND_SMS_FOREGROUND);
	}

	private void onUpdateStage(StagedViewModel.StagedEnum stage) {
		findViewById(R.id.recipientRow).setVisibility(stage.compare(RECIPIENT) > 0 ? View.VISIBLE : View.GONE);
		findViewById(R.id.amountRow).setVisibility(stage.compare(AMOUNT) > 0 && requestViewModel.getAmount().getValue() != null ? View.VISIBLE : View.GONE);
		findViewById(R.id.noteRow).setVisibility((stage.compare(NOTE) > 0 && requestViewModel.getNote().getValue() != null && !requestViewModel.getNote().getValue().isEmpty()) ? View.VISIBLE : View.GONE);
		findViewById(R.id.dateRow).setVisibility(requestViewModel.getFutureDate().getValue() != null ? View.VISIBLE : View.GONE);

		setCurrentCard(stage);
		setFab(stage);
	}

	private void setCurrentCard(StagedViewModel.StagedEnum stage) {
		findViewById(R.id.recipientCard).setVisibility(stage.compare(RECIPIENT) == 0 ? View.VISIBLE : View.GONE);
		findViewById(R.id.amountCard).setVisibility(stage.compare(AMOUNT) == 0 ? View.VISIBLE : View.GONE);
		findViewById(R.id.noteCard).setVisibility(stage.compare(NOTE) == 0 ? View.VISIBLE : View.GONE);
//		findViewById(R.id.futureCard).setVisibility(requestViewModel.getFutureDate().getValue() == null && allowSchedule ? View.VISIBLE : View.GONE);
	}

	private void setFab(StagedViewModel.StagedEnum stage) {
		ExtendedFloatingActionButton fab = findViewById(R.id.fab);
		if (stage.compare(REVIEW) >= 0) {
			if (requestViewModel.getIsFuture().getValue() != null && requestViewModel.getIsFuture().getValue())
				fab.setText(getString(R.string.schedule));
			else
				fab.setText(getString(R.string.notify_request_cta));
		} else {
			fab.setText(R.string.continue_text);
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_CANCELED) {
			return;
		}
		if (requestCode == SEND_SMS_FOREGROUND) {
			returnResult();
		}
	}

	private void returnResult() {
		Intent i = new Intent();
		setResult(RESULT_OK, i);
		finish();
	}
}
