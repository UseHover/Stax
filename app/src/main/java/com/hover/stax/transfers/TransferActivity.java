package com.hover.stax.transfers;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.amplitude.api.Amplitude;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.hover.stax.R;
import com.hover.stax.actions.Action;
import com.hover.stax.home.MainActivity;
import com.hover.stax.hover.HoverSession;
import com.hover.stax.security.BiometricChecker;
import com.hover.stax.utils.DateUtils;
import com.hover.stax.utils.UIHelper;

import java.util.List;

import static com.hover.stax.transfers.InputStage.AMOUNT;
import static com.hover.stax.transfers.InputStage.FROM_ACCOUNT;
import static com.hover.stax.transfers.InputStage.REASON;
import static com.hover.stax.transfers.InputStage.REVIEW;
import static com.hover.stax.transfers.InputStage.TO_NETWORK;
import static com.hover.stax.transfers.InputStage.TO_NUMBER;

public class TransferActivity extends AppCompatActivity implements BiometricChecker.AuthListener {
	final public static String TAG = "TransferActivity";

	private TransferViewModel transferViewModel;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		transferViewModel = new ViewModelProvider(this).get(TransferViewModel.class);
		transferViewModel.getActiveAction().observe(this, action -> Log.i(TAG, "This observer is neccessary to make updates fire, but all logic is in viewmodel") );
		transferViewModel.getActions().observe(this, this::onUpdateActions);
		transferViewModel.getActiveChannel().observe(this, channel -> Log.i(TAG, "This observer is neccessary to make updates fire, but all logic is in viewmodel") );

		transferViewModel.getStage().observe(this, this::onUpdateStage);
		transferViewModel.getIsFuture().observe(this, isFuture -> onUpdateStage(transferViewModel.getStage().getValue()));
		transferViewModel.getFutureDate().observe(this, date -> onUpdateStage(transferViewModel.getStage().getValue()));
		setContentView(R.layout.activity_transfer);
	}

	private void handleBackButton(View root) {
		findViewById(R.id.backButton).setOnClickListener(v -> {
			if (transferViewModel.getStage().getValue() != AMOUNT) transferViewModel.goToPrevStage();
			else onBackPressed();
		});
	}

	public void onContinue(View view) {
		if (transferViewModel.getStage().getValue() != REVIEW) {
			transferViewModel.goToNextStage();
		} else if (transferViewModel.getActiveAction() != null) {
			submit();
		} else
			UIHelper.flashMessage(this, getResources().getString(R.string.selectServiceError));
	}

	private void submit() {
		if (transferViewModel.getIsFuture().getValue() != null && transferViewModel.getIsFuture().getValue() && transferViewModel.getFutureDate().getValue() != null) {
			transferViewModel.schedule();
			UIHelper.flashMessage(this, findViewById(R.id.root), getString(R.string.schedule_created, DateUtils.humanFriendlyDate(transferViewModel.getFutureDate().getValue())));
			startActivity(new Intent(this, MainActivity.class));
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
		Amplitude.getInstance().logEvent(getString(R.string.finish_screen, transferViewModel.getType()));
		new HoverSession.Builder(act, transferViewModel.getActiveChannel().getValue(),
			this, MainActivity.TRANSFER_REQUEST)
			.extra(Action.PHONE_KEY, transferViewModel.getRecipient().getValue())
			.extra(Action.ACCOUNT_KEY, transferViewModel.getRecipient().getValue())
			.extra(Action.AMOUNT_KEY, transferViewModel.getAmount().getValue())
			.extra(Action.REASON_KEY, transferViewModel.getReason().getValue())
			.run();
	}

	private void onUpdateActions(List<Action> actions) {
		Log.e(TAG, "action update, channel: " + transferViewModel.getActiveChannel().getValue());
		if (transferViewModel.getActiveChannel().getValue() == null) {
			findViewById(R.id.summaryCard).setVisibility(View.GONE);
			findViewById(R.id.errorCard).setVisibility(View.GONE);
		} else if (actions == null || actions.size() == 0) {
			findViewById(R.id.summaryCard).setVisibility(View.GONE);
			findViewById(R.id.errorCard).setVisibility(View.VISIBLE);
		} else {
			Log.e(TAG, "setting active action: " + actions.get(0));
			findViewById(R.id.summaryCard).setVisibility(View.VISIBLE);
			findViewById(R.id.errorCard).setVisibility(View.GONE);
			transferViewModel.setActiveAction(actions.get(0));
		}
	}

	private void onUpdateStage(InputStage stage) {
		findViewById(R.id.summaryCard).setVisibility(stage.compareTo(AMOUNT) > 0 ? View.VISIBLE : View.GONE);
		findViewById(R.id.amountRow).setVisibility(stage.compareTo(AMOUNT) > 0 ? View.VISIBLE : View.GONE);
		findViewById(R.id.fromRow).setVisibility(stage.compareTo(FROM_ACCOUNT) > 0 ? View.VISIBLE : View.GONE);
		findViewById(R.id.toNetworkRow).setVisibility(stage.compareTo(TO_NETWORK) > 0 && transferViewModel.getActiveAction().getValue().requiresRecipient() ? View.VISIBLE : View.GONE);
		findViewById(R.id.recipientRow).setVisibility(stage.compareTo(TO_NUMBER) > 0 && transferViewModel.getActiveAction().getValue().requiresRecipient() ? View.VISIBLE : View.GONE);
		findViewById(R.id.reasonRow).setVisibility((stage.compareTo(REASON) > 0 && transferViewModel.getReason().getValue() != null && !transferViewModel.getReason().getValue().isEmpty()) ? View.VISIBLE : View.GONE);
		findViewById(R.id.dateRow).setVisibility(transferViewModel.getFutureDate().getValue() != null ? View.VISIBLE : View.GONE);

		findViewById(R.id.amountCard).setVisibility(stage.compareTo(AMOUNT) == 0 ? View.VISIBLE : View.GONE);
		findViewById(R.id.fromAccountCard).setVisibility(stage.compareTo(FROM_ACCOUNT) == 0 ? View.VISIBLE : View.GONE);
		findViewById(R.id.networkCard).setVisibility(stage.compareTo(TO_NETWORK) == 0 ? View.VISIBLE : View.GONE);
		findViewById(R.id.recipientCard).setVisibility(stage.compareTo(TO_NUMBER) == 0 ? View.VISIBLE : View.GONE);
		findViewById(R.id.reasonCard).setVisibility(stage.compareTo(REASON) == 0 ? View.VISIBLE : View.GONE);
		findViewById(R.id.futureCard).setVisibility(transferViewModel.getFutureDate().getValue() == null ? View.VISIBLE : View.GONE);

		setFab(stage);
	}

	private void setFab(InputStage stage) {
		ExtendedFloatingActionButton fab = ((ExtendedFloatingActionButton) findViewById(R.id.fab));
		if (stage.compareTo(REVIEW) == 0) {
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
}