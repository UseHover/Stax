package com.hover.stax.home;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.amplitude.api.Amplitude;
import com.hover.sdk.transactions.TransactionContract;
import com.hover.stax.R;
import com.hover.stax.actions.Action;
import com.hover.stax.balances.BalanceAdapter;
import com.hover.stax.balances.BalancesViewModel;
import com.hover.stax.channels.Channel;
import com.hover.stax.database.Constants;
import com.hover.stax.hover.HoverSession;
import com.hover.stax.requests.RequestSenderInterface;
import com.hover.stax.schedules.Schedule;
import com.hover.stax.settings.BiometricChecker;
import com.hover.stax.transactions.TransactionHistoryViewModel;
import com.hover.stax.transfers.TransferActivity;
import com.hover.stax.utils.DateUtils;
import com.hover.stax.utils.UIHelper;

import java.util.List;

public class MainActivity extends AbstractNavigationActivity implements
	BalancesViewModel.RunBalanceListener, BalanceAdapter.BalanceListener, BiometricChecker.AuthListener {

	final public static String TAG = "MainActivity";
	private BalancesViewModel balancesViewModel;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		balancesViewModel = new ViewModelProvider(this).get(BalancesViewModel.class);
		balancesViewModel.setListener(this);
		balancesViewModel.getSelectedChannels().observe(this, channels -> Log.i(TAG, "Channels observer is neccessary to make updates fire, but all logic is in viewmodel. " + channels.size()));
		balancesViewModel.getToRun().observe(this, actions -> Log.i(TAG, "RunActions observer is neccessary to make updates fire, but all logic is in viewmodel. " + actions.size()));
		balancesViewModel.getRunFlag().observe(this, flag -> Log.i(TAG, "Flag observer is neccessary to make updates fire, but all logic is in viewmodel. " + flag));
		balancesViewModel.getActions().observe(this, actions -> Log.i(TAG, "Actions observer is neccessary to make updates fire, but all logic is in viewmodel. " + actions.size()));

		setUpNav();
		checkForRequest(getIntent());
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		checkForRequest(intent);
	}

	private void checkForRequest(Intent intent) {
		if (intent.hasExtra(Constants.REQUEST_LINK))
			startTransfer(Action.P2P, true, intent);
	}

	@Override
	public void onTapDetail(int channel_id) {
		Bundle bundle = new Bundle();
		bundle.putInt(TransactionContract.COLUMN_CHANNEL_ID, channel_id);
		Navigation.findNavController(findViewById(R.id.nav_host_fragment)).navigate(R.id.channelsDetailsFragment, bundle);
	}

	@Override
	public void triggerRefreshAll() {
		balancesViewModel.setAllRunning(this);
	}

	@Override
	public void onTapRefresh(int channel_id) {
		Amplitude.getInstance().logEvent(getString(R.string.refresh_balance_single));
		balancesViewModel.setRunning(channel_id);
	}

	@Override
	public void startRun(Action a, int i) {
//		if (i == 0)
//			new BiometricChecker(this, this).startAuthentication(a);
//		else
			run(a, i);
	}

	private void run(Action action, int index) {
		Log.e(TAG, "running index: " + index);
		if (balancesViewModel.getChannel(action.channel_id) != null) {
			new HoverSession.Builder(action, balancesViewModel.getChannel(action.channel_id), MainActivity.this, index)
				.finalScreenTime(0).run();
		} else { // Fix for auth issue on OnePlus 6
			new Handler(Looper.getMainLooper()).post(() -> {
				balancesViewModel.getSelectedChannels().observe(MainActivity.this, new Observer<List<Channel>>() {
					@Override
					public void onChanged(List<Channel> channels) {
						if (channels != null && balancesViewModel.getChannel(channels, action.channel_id) != null) {
							run(action, 0);
							balancesViewModel.getSelectedChannels().removeObserver(this);
						}
					}
				});
			});
		}
	}

	@Override
	public void onAuthError(String error) {
		Log.e(TAG, "error: " + error);
	}

	@Override
	public void onAuthSuccess(Action act) {
			run(act, 0);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
			case Constants.TRANSFER_REQUEST:
				if (data != null) { onProbableHoverCall(data); }
				else Amplitude.getInstance().logEvent(getString(R.string.sdk_failure));
				break;
			case Constants.REQUEST_REQUEST:
				if (resultCode == RESULT_OK) { onRequest(data); }
				break;
			default: // requestCode < Constants.BALANCE_MAX // Balance call
				balancesViewModel.setRan(requestCode);
				if (resultCode == RESULT_OK && data != null && data.getAction() != null) onProbableHoverCall(data);
		}
	}

	private void onProbableHoverCall(Intent data) {
		if (data.getAction() != null && data.getAction().equals(Constants.SCHEDULED)) {
			showMessage(getString(R.string.toast_confirm_schedule, DateUtils.humanFriendlyDate(data.getLongExtra(Schedule.DATE_KEY, 0))));
		} else {
			Amplitude.getInstance().logEvent(getString(R.string.finish_load_screen));
			new ViewModelProvider(this).get(TransactionHistoryViewModel.class).saveTransaction(data, this);
		}
	}

	public void startTransfer(String type, boolean isFromStaxLink, Intent received) {
		Intent i = new Intent(this, TransferActivity.class);
		i.setAction(type);
		if (isFromStaxLink) i.putExtra(Constants.REQUEST_LINK, received.getExtras().getString(Constants.REQUEST_LINK));
		startActivityForResult(i, Constants.TRANSFER_REQUEST);
	}

	private void onRequest(Intent data) {
		if (data.getAction().equals(Constants.SCHEDULED))
			showMessage(getString(R.string.toast_request_scheduled, DateUtils.humanFriendlyDate(data.getLongExtra(Schedule.DATE_KEY, 0))));
		else
			showMessage(getString(R.string.toast_confirm_request));
	}

	private void showMessage(String str) {
		UIHelper.flashMessage(this, findViewById(R.id.fab), str);
	}
}