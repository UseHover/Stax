package com.hover.stax.transfers;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.amplitude.api.Amplitude;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.hover.stax.R;
import com.hover.stax.actions.Action;
import com.hover.stax.hover.HoverSession;
import com.hover.stax.schedules.Schedule;
import com.hover.stax.schedules.ScheduleDetailViewModel;
import com.hover.stax.security.BiometricChecker;
import com.hover.stax.utils.UIHelper;

import static com.hover.stax.transfers.InputStage.AMOUNT;
import static com.hover.stax.transfers.InputStage.FROM_ACCOUNT;
import static com.hover.stax.transfers.InputStage.REASON;
import static com.hover.stax.transfers.InputStage.RECIPIENT;
import static com.hover.stax.transfers.InputStage.REVIEW;
import static com.hover.stax.transfers.InputStage.REVIEW_DIRECT;
import static com.hover.stax.transfers.InputStage.TO_NETWORK;

public class TransferActivity extends AppCompatActivity implements BiometricChecker.AuthListener {
	final public static String TAG = "TransferActivity";
	final public static int TRANSFER_REQUEST = 203, SCHEDULED_REQUEST = 204;
	final public static String TRANSFERED = "TRANSFERED", SCHEDULED = "SCHEDULED";

	private TransferViewModel transferViewModel;
	private ScheduleDetailViewModel scheduleViewModel = null;

	private boolean allowSchedule = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		transferViewModel = new ViewModelProvider(this).get(TransferViewModel.class);

		startObservers();
		checkIntent();
		setContentView(R.layout.activity_transfer);
	}

	private void startObservers() {
		transferViewModel.getSelectedChannels().observe(this, channels -> {
			if (scheduleViewModel != null && scheduleViewModel.getSchedule().getValue() != null)
				transferViewModel.setActiveChannel(scheduleViewModel.getSchedule().getValue().channel_id);
		});
		transferViewModel.getActiveChannel().observe(this, channel -> Log.i(TAG, "This observer is neccessary to make updates fire, but all logic is in viewmodel."));
		transferViewModel.getActions().observe(this, actions -> Log.i(TAG, "This observer is neccessary to make updates fire, but all logic is in viewmodel."));
		transferViewModel.getActiveAction().observe(this, action -> {
			onUpdateStage(transferViewModel.getStage().getValue());
		});

		transferViewModel.getStage().observe(this, this::onUpdateStage);
		transferViewModel.getIsFuture().observe(this, isFuture -> onUpdateStage(transferViewModel.getStage().getValue()));
		transferViewModel.getFutureDate().observe(this, date -> onUpdateStage(transferViewModel.getStage().getValue()));

		transferViewModel.setType(getIntent().getAction());
	}

	private void checkIntent() {
		if (getIntent().hasExtra(Schedule.SCHEDULE_ID)) {
			createFromSchedule(getIntent().getIntExtra(Schedule.SCHEDULE_ID, -1));
		} else
			Amplitude.getInstance().logEvent(getString(R.string.visit_screen, getIntent().getAction()));
	}

	private void createFromSchedule(int schedule_id) {
		scheduleViewModel = new ViewModelProvider(this).get(ScheduleDetailViewModel.class);
		scheduleViewModel.getAction().observe(this, action -> {
			if (action != null) transferViewModel.setActiveAction(action);
		});
		scheduleViewModel.getSchedule().observe(this, schedule -> {
			if (schedule == null) return;
			transferViewModel.setType(schedule.type);
			transferViewModel.setActiveChannel(schedule.channel_id);
			transferViewModel.setAmount(schedule.amount);
			transferViewModel.setActiveChannel(schedule.channel_id);
			transferViewModel.setRecipient(schedule.recipient);
			transferViewModel.setReason(schedule.reason);
			allowSchedule = false;
			transferViewModel.setStage(REVIEW_DIRECT);
		});
		scheduleViewModel.setSchedule(schedule_id);
		Amplitude.getInstance().logEvent(getString(R.string.clicked_schedule_notification));
	}

//	private void handleBackButton(View root) {
//		findViewById(R.id.backButton).setOnClickListener(v -> {
//			if (transferViewModel.getStage().getValue() != AMOUNT) transferViewModel.goToStage(transferViewModel.getStage().getValue().prev());
//			else onBackPressed();
//		});
//	}

	public void onContinue(View view) {
		if (transferViewModel.getStage().getValue() != REVIEW && transferViewModel.getStage().getValue() != REVIEW_DIRECT) {
			transferViewModel.goToNextStage();
		} else if (transferViewModel.getActiveAction() != null) {
			submit();
		} else
			UIHelper.flashMessage(this, getResources().getString(R.string.selectServiceError));
	}

	private void submit() {
		if (transferViewModel.getIsFuture().getValue() != null && transferViewModel.getIsFuture().getValue() && transferViewModel.getFutureDate().getValue() != null) {
			transferViewModel.schedule(this);
			returnResult(SCHEDULED_REQUEST);
		} else authenticate();
	}

	private void authenticate() {
		new BiometricChecker(this, this).startAuthentication(transferViewModel.getActiveAction().getValue());
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
		new HoverSession.Builder(act, transferViewModel.getActiveChannel().getValue(),
				this, TransferActivity.TRANSFER_REQUEST)
				.extra(Action.PHONE_KEY, transferViewModel.getRecipient().getValue())
				.extra(Action.ACCOUNT_KEY, transferViewModel.getRecipient().getValue())
				.extra(Action.AMOUNT_KEY, transferViewModel.getAmount().getValue())
				.extra(Action.REASON_KEY, transferViewModel.getReason().getValue())
				.run();
	}

	private void onUpdateStage(InputStage stage) {
//		findViewById(R.id.summaryCard).setVisibility(stage.compareTo(AMOUNT) > 0 ? View.VISIBLE : View.GONE);
		findViewById(R.id.amountRow).setVisibility(stage.compareTo(AMOUNT) > 0 ? View.VISIBLE : View.GONE);
		findViewById(R.id.fromRow).setVisibility(stage.compareTo(FROM_ACCOUNT) > 0 ? View.VISIBLE : View.GONE);
		findViewById(R.id.toNetworkRow).setVisibility(stage.compareTo(TO_NETWORK) > 0 &&
															  transferViewModel.getActions().getValue() != null && transferViewModel.getActions().getValue().size() > 0 && (transferViewModel.getActions().getValue().size() > 1 || !transferViewModel.getActiveAction().getValue().toString().equals("Phone number")) ? View.VISIBLE : View.GONE);
		findViewById(R.id.recipientRow).setVisibility(stage.compareTo(RECIPIENT) > 0 && transferViewModel.getActiveAction().getValue() != null && transferViewModel.getActiveAction().getValue().requiresRecipient() ? View.VISIBLE : View.GONE);
		findViewById(R.id.reasonRow).setVisibility((stage.compareTo(REASON) > 0 && transferViewModel.getReason().getValue() != null && !transferViewModel.getReason().getValue().isEmpty()) ? View.VISIBLE : View.GONE);
		findViewById(R.id.dateRow).setVisibility(transferViewModel.getFutureDate().getValue() != null ? View.VISIBLE : View.GONE);

		setCurrentCard(stage);
		setFab(stage);
	}

	private void setCurrentCard(InputStage stage) {
		findViewById(R.id.amountCard).setVisibility(stage.compareTo(AMOUNT) == 0 ? View.VISIBLE : View.GONE);
		findViewById(R.id.fromAccountCard).setVisibility(stage.compareTo(FROM_ACCOUNT) == 0 ? View.VISIBLE : View.GONE);
		findViewById(R.id.networkCard).setVisibility(stage.compareTo(TO_NETWORK) == 0 ? View.VISIBLE : View.GONE);
		findViewById(R.id.recipientCard).setVisibility(stage.compareTo(RECIPIENT) == 0 ? View.VISIBLE : View.GONE);
		findViewById(R.id.reasonCard).setVisibility(stage.compareTo(REASON) == 0 ? View.VISIBLE : View.GONE);
		findViewById(R.id.futureCard).setVisibility(transferViewModel.getFutureDate().getValue() == null && allowSchedule ? View.VISIBLE : View.GONE);
	}

	private void setFab(InputStage stage) {
		ExtendedFloatingActionButton fab = findViewById(R.id.fab);
		if (stage.compareTo(REVIEW) >= 0) {
			if (transferViewModel.getIsFuture().getValue() != null && transferViewModel.getIsFuture().getValue()) {
				fab.setVisibility(transferViewModel.getFutureDate().getValue() == null ? View.GONE : View.VISIBLE);
				fab.setText(getString(R.string.schedule));
			} else {
				fab.setVisibility(View.VISIBLE);
				fab.setText(getString(R.string.transfer_now));
			}
		} else {
			fab.setVisibility(View.VISIBLE);
			fab.setText(R.string.continue_text);
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_CANCELED) {
			return;
		}
		if (requestCode == TRANSFER_REQUEST) {
			returnResult(requestCode);
		}
	}

	private void returnResult(int type) {
		Intent i = new Intent();
		if (type == SCHEDULED_REQUEST) {
			i.putExtra(Schedule.DATE_KEY, transferViewModel.getFutureDate().getValue());
		}
		i.setAction(type == SCHEDULED_REQUEST ? SCHEDULED : TRANSFERED);
		setResult(RESULT_OK, i);
		finish();
	}
}