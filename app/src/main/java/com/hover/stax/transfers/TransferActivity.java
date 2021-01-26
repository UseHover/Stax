package com.hover.stax.transfers;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.amplitude.api.Amplitude;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.hover.stax.R;
import com.hover.stax.actions.Action;
import com.hover.stax.contacts.StaxContact;
import com.hover.stax.database.Constants;
import com.hover.stax.hover.HoverSession;
import com.hover.stax.schedules.Schedule;
import com.hover.stax.schedules.ScheduleDetailViewModel;
import com.hover.stax.security.BiometricChecker;
import com.hover.stax.utils.StagedViewModel;
import com.hover.stax.views.StaxDialog;

import static com.hover.stax.transfers.TransferStage.*;

public class TransferActivity extends AppCompatActivity implements BiometricChecker.AuthListener {
	final public static String TAG = "TransferActivity";

	private TransferViewModel transferViewModel;
	private ScheduleDetailViewModel scheduleViewModel = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		transferViewModel = new ViewModelProvider(this).get(TransferViewModel.class);

		startObservers();
		checkIntent();
		setContentView(R.layout.activity_transfer);
	}

	private void startObservers() {
		transferViewModel.getSelectedChannels().observe(this, channels -> Log.i(TAG, "This observer is neccessary to make updates fire, but all logic is in viewmodel."));
		transferViewModel.getActiveChannel().observe(this, channel -> Log.i(TAG, "This observer is neccessary to make updates fire, but all logic is in viewmodel."));
		transferViewModel.getActions().observe(this, actions -> onUpdateStage(transferViewModel.getStage().getValue()));
		transferViewModel.getActiveAction().observe(this, action -> onUpdateStage(transferViewModel.getStage().getValue()));

		transferViewModel.getStage().observe(this, this::onUpdateStage);
		transferViewModel.getIsFuture().observe(this, isFuture -> onUpdateStage(transferViewModel.getStage().getValue()));
		transferViewModel.getFutureDate().observe(this, date -> onUpdateStage(transferViewModel.getStage().getValue()));
		transferViewModel.repeatSaved().observe(this, isSaved -> onUpdateStage(transferViewModel.getStage().getValue()));
		transferViewModel.getIsEditing().observe(this, isEditing -> onUpdateStage(transferViewModel.getStage().getValue()));

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

	public void onContinue(View view) {
		if (transferViewModel.stageValidates()) {
			Log.d("CONTINUE", "IT VALIDATED O");
			if (transferViewModel.isDone())
				submit();
			else
				transferViewModel.setStage(REVIEW);
		}
		else Log.d("CONTINUE", "failed failed O");
	}

	private void submit() {
		transferViewModel.saveContact();
		if (transferViewModel.getIsFuture().getValue() != null && transferViewModel.getIsFuture().getValue() && transferViewModel.getFutureDate().getValue() != null) {
			transferViewModel.schedule();
			returnResult(Constants.SCHEDULE_REQUEST, RESULT_OK, null);
		} else {
			if (transferViewModel.repeatSaved().getValue() != null && transferViewModel.repeatSaved().getValue())
				transferViewModel.schedule();
			authenticate();
		}
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
		HoverSession.Builder hsb = new HoverSession.Builder(act, transferViewModel.getActiveChannel().getValue(),
				TransferActivity.this, Constants.TRANSFER_REQUEST)
				.extra(Action.AMOUNT_KEY, transferViewModel.getAmount().getValue())
				.extra(Action.NOTE_KEY, transferViewModel.getNote().getValue());

		if (transferViewModel.getContact().getValue() != null) { addRecipientInfo(hsb); }
		hsb.run();
	}
	private void addRecipientInfo(HoverSession.Builder hsb) {
		hsb.extra(Action.ACCOUNT_KEY, transferViewModel.getContact().getValue().phoneNumber)
			.extra(Action.PHONE_KEY, transferViewModel.getContact().getValue().getNumberFormatForInput(transferViewModel.getActiveAction().getValue(), transferViewModel.getActiveChannel().getValue()));
	}

	private void onUpdateStage(@Nullable StagedViewModel.StagedEnum stage) {
		if (Navigation.findNavController(this, R.id.nav_host_fragment).getCurrentDestination().getId() == R.id.navigation_edit)
			((ExtendedFloatingActionButton) findViewById(R.id.fab)).hide();
		else if (findViewById(R.id.amountRow) != null) {
			setSummaryCard(stage);
			setCurrentCard(stage);
			setFab(stage);
		}
	}

	private void setSummaryCard(@Nullable StagedViewModel.StagedEnum stage) {
		findViewById(R.id.amountRow).setVisibility(stage.compare(AMOUNT) > 0 ? View.VISIBLE : View.GONE);
		findViewById(R.id.accountsRow).setVisibility(stage.compare(FROM_ACCOUNT) > 0 ? View.VISIBLE : View.GONE);
		findViewById(R.id.recipientRow).setVisibility((stage.compare(RECIPIENT) > 0 || transferViewModel.getRequest().getValue() != null && transferViewModel.getRequest().getValue().hasRequesterInfo()) ? View.VISIBLE : View.GONE);
		findViewById(R.id.noteRow).setVisibility((stage.compare(NOTE) > 0 && transferViewModel.getNote().getValue() != null && !transferViewModel.getNote().getValue().isEmpty()) ? View.VISIBLE : View.GONE);
		findViewById(R.id.btnRow).setVisibility(stage.compare(AMOUNT) > 0 ? View.VISIBLE : View.GONE);
	}

	private void setCurrentCard(StagedViewModel.StagedEnum stage) {
		findViewById(R.id.summaryCard).setVisibility(stage.compare(REVIEW) == 0 ? View.VISIBLE : View.GONE);
		findViewById(R.id.transactionFormCard).setVisibility(stage.compare(REVIEW) != 0 ? View.VISIBLE : View.GONE);
		//findViewById(R.id.reasonEditText).setVisibility(!transferViewModel.getType().equals(Action.AIRTIME) ? View.VISIBLE : View.GONE);
		//findViewById(R.id.reasonCard).setVisibility(stage.compare(NOTE) == 0 ? View.VISIBLE : View.GONE);
		//findViewById(R.id.amountCard).setVisibility(stage.compare(AMOUNT) == 0 ? View.VISIBLE : View.GONE);
		//findViewById(R.id.fromAccountCard).setVisibility(stage.compare(FROM_ACCOUNT) == 0 ? View.VISIBLE : View.GONE);
		//findViewById(R.id.networkCard).setVisibility(stage.compare(TO_NETWORK) == 0 ? View.VISIBLE : View.GONE);
		//findViewById(R.id.recipientCard).setVisibility(stage.compare(RECIPIENT) == 0 ? View.VISIBLE : View.GONE);
		//findViewById(R.id.futureCard).setVisibility(stage.compare(REVIEW_DIRECT) < 0 && transferViewModel.getFutureDate().getValue() == null ? View.VISIBLE : View.GONE);
		//findViewById(R.id.repeatCard).setVisibility(stage.compare(REVIEW_DIRECT) < 0 && (transferViewModel.repeatSaved().getValue() == null || !transferViewModel.repeatSaved().getValue()) ? View.VISIBLE : View.GONE);
	}

	private void setFab(StagedViewModel.StagedEnum stage) {
		ExtendedFloatingActionButton fab = findViewById(R.id.fab);
		if (stage.compare(REVIEW) >= 0) {
			if (transferViewModel.getPageError().getValue() != null)
				fab.hide();
			else if (transferViewModel.getIsFuture().getValue() != null && transferViewModel.getIsFuture().getValue()) {
				fab.setText(getString(R.string.fab_schedule));
				if (transferViewModel.getFutureDate().getValue() == null) { fab.hide(); } else { fab.show(); }
			} else if(transferViewModel.getType().equals(Action.AIRTIME)) {
				fab.setText(getString(R.string.fab_sendnow));
				fab.show();
			}
			else {
				fab.setText(getString(R.string.fab_transfernow));
				fab.show();
			}
		} else {
			fab.setText(R.string.btn_continue);
			fab.show();
		}
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
		if (type == Constants.SCHEDULE_REQUEST)
			i.putExtra(Schedule.DATE_KEY, transferViewModel.getFutureDate().getValue());
		if (transferViewModel.getContact().getValue() != null)
			i.putExtra(StaxContact.ID_KEY, transferViewModel.getContact().getValue().id);
		i.setAction(type == Constants.SCHEDULE_REQUEST ? Constants.SCHEDULED : Constants.TRANSFERED);
		setResult(result, i);
		finish();
	}

	@Override
	public void onBackPressed() {
		if (Navigation.findNavController(findViewById(R.id.nav_host_fragment)).getCurrentDestination().getId() != R.id.navigation_edit ||
			    !Navigation.findNavController(findViewById(R.id.nav_host_fragment)).popBackStack()) {
			if (transferViewModel.getStage().getValue().compare(REVIEW) == 0 && transferViewModel.getSchedule().getValue() == null && transferViewModel.getRequest().getValue() == null)
				transferViewModel.goToPrevStage();
			else
				super.onBackPressed();
		}
	}
}