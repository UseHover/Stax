package com.hover.stax.home;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.amplitude.api.Amplitude;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.hover.sdk.permissions.PermissionDialog;
import com.hover.sdk.permissions.PermissionHelper;
import com.hover.sdk.transactions.TransactionContract;
import com.hover.stax.R;
import com.hover.stax.actions.Action;
import com.hover.stax.balances.BalanceAdapter;
import com.hover.stax.balances.BalancesViewModel;
import com.hover.stax.channels.Channel;
import com.hover.stax.channels.ChannelsActivity;
import com.hover.stax.database.Constants;
import com.hover.stax.hover.HoverSession;
import com.hover.stax.requests.AbstractMessageSendingActivity;
import com.hover.stax.requests.RequestActivity;
import com.hover.stax.schedules.Schedule;
import com.hover.stax.settings.BiometricChecker;
import com.hover.stax.settings.SettingsFragment;
import com.hover.stax.transactions.TransactionHistoryViewModel;
import com.hover.stax.transfers.TransferActivity;
import com.hover.stax.utils.DateUtils;
import com.hover.stax.permissions.PermissionUtils;
import com.hover.stax.utils.UIHelper;

import java.util.List;

public class MainActivity extends AbstractMessageSendingActivity implements
	BalancesViewModel.RunBalanceListener, BalanceAdapter.BalanceListener, BiometricChecker.AuthListener, HomeNavigationListener {

	final public static String TAG = "MainActivity";
	private BalancesViewModel balancesViewModel;
	private ShowcaseExecutor showCase;
	private NavController navController;
	private int navigateToWhere = 0;
	private boolean askStarted = false;
	private final static String BASIC_PERM = "basic_permission";
	private boolean currentAskIsForBasicPermssion;
	private AlertDialog dialog;
	public final static int PHONE_REQUEST = 0, SMS_REQUEST = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		balancesViewModel = new ViewModelProvider(this).get(BalancesViewModel.class);
		balancesViewModel.setListener(this);
		balancesViewModel.getToRun().observe(this, actions -> Log.i(TAG, "RunActions observer is neccessary to make updates fire, but all logic is in viewmodel. " + actions.size()));
		balancesViewModel.getBalanceActions().observe(this, actions -> Log.i(TAG, "Actions observer is neccessary to make updates fire, but all logic is in viewmodel. " + actions.size()));
		balancesViewModel.getSelectedChannels().observe(this, channels -> maybeRunAShowcase());

		setUpNav();
		checkForRequest(getIntent());
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (!currentAskIsForBasicPermssion && askStarted) requestNext();
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

	public void addAccount(View view) {
		Amplitude.getInstance().logEvent(getString(R.string.click_add_account));
		startActivityForResult(new Intent(this, ChannelsActivity.class), Constants.ADD_SERVICE);
	}

	@Override
	public void onTapDetail(int channel_id) {
		Bundle bundle = new Bundle();
		bundle.putInt(TransactionContract.COLUMN_CHANNEL_ID, channel_id);
		Navigation.findNavController(findViewById(R.id.nav_host_fragment)).navigate(R.id.channelsDetailsFragment, bundle);
	}

	@Override
	public void triggerRefreshAll() {
		runAllBalances(null);
	}

	@Override
	public void onTapRefresh(int channel_id) {
		Amplitude.getInstance().logEvent(getString(R.string.refresh_balance_single));
		balancesViewModel.setRunning(channel_id);
	}

	public void runAllBalances(View view) {
		Amplitude.getInstance().logEvent(getString(R.string.refresh_balance_all));
		balancesViewModel.setRunning();
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
		} else { // Possible fix for auth issue on OnePlus 6
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
			case Constants.ADD_SERVICE:
				if (resultCode == RESULT_OK) { maybeRunAShowcase(); }
				break;
			case Constants.REQUEST_REQUEST:
				if (resultCode == RESULT_OK) { onRequest(data); }
				break;
			default: // requestCode < Constants.BALANCE_MAX // Balance call
				balancesViewModel.setRan(requestCode);
				if (resultCode == RESULT_OK && data != null && data.getAction() != null) onProbableHoverCall(data);
		}
	}

	public void maybeRunAShowcase() {
		/*if (showCase == null)
			showCase = new ShowcaseExecutor(this, findViewById(R.id.home_root));
		switch (ShowcaseExecutor.getStage(this)) {
			case 0: showCase.showcaseAddAcctStage();
				break;
			case 1:
				if (balancesViewModel.getSelectedChannels().getValue() != null && balancesViewModel.getSelectedChannels().getValue().size() > 0)
					showCase.showcaseRefreshAccountStage();
				break;
			case 2:
				if (balancesViewModel.getSelectedChannels().getValue() != null && balancesViewModel.getSelectedChannels().getValue().size() > 0)
					showCase.showcasePeekBalanceStage();
				break;
		} */
	}

	private void onProbableHoverCall(Intent data) {
		if (data.getAction() != null && data.getAction().equals(Constants.SCHEDULED)) {
			showMessage(getString(R.string.toast_confirm_schedule, DateUtils.humanFriendlyDate(data.getLongExtra(Schedule.DATE_KEY, 0))));
		} else {
			Amplitude.getInstance().logEvent(getString(R.string.finish_load_screen));
			new ViewModelProvider(this).get(TransactionHistoryViewModel.class).saveTransaction(data, this);
		}
	}

	private void startTransfer(String type, boolean isFromStaxLink, Intent received) {
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

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			onBackPressed();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void setUpNav() {
		BottomAppBar nav = findViewById(R.id.nav_view);
		navController = Navigation.findNavController(this, R.id.nav_host_fragment);
		AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
		NavigationUI.setupWithNavController(nav, navController, appBarConfiguration);

		nav.setOnMenuItemClickListener((Toolbar.OnMenuItemClickListener) item -> {
			if(item.getItemId() == R.id.navigation_home) navController.navigate(R.id.navigation_home);
			else {
				navigateToWhere = item.getItemId();
				navigateOutsideHomeScreen(item.getItemId(), navController);
			}
            return false;
		});

		navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
			switch (destination.getId()) {
				case R.id.navigation_home:
					changeDrawableColor(nav.findViewById(R.id.navigation_home), R.color.brightBlue);
					changeDrawableColor(nav.findViewById(R.id.navigation_balance_history), R.color.offWhite);
					changeDrawableColor(nav.findViewById(R.id.navigation_security), R.color.offWhite);
					break;
				case R.id.navigation_balance_history:
					changeDrawableColor(nav.findViewById(R.id.navigation_home), R.color.offWhite);
					changeDrawableColor(nav.findViewById(R.id.navigation_balance_history), R.color.brightBlue);
					changeDrawableColor(nav.findViewById(R.id.navigation_security), R.color.offWhite);
					break;
				case R.id.navigation_security:
					changeDrawableColor(nav.findViewById(R.id.navigation_home), R.color.offWhite);
					changeDrawableColor(nav.findViewById(R.id.navigation_balance_history), R.color.offWhite);
					changeDrawableColor(nav.findViewById(R.id.navigation_security), R.color.brightBlue);
					break;

				default:
					changeDrawableColor(nav.findViewById(R.id.navigation_security), R.color.offWhite);
					changeDrawableColor(nav.findViewById(R.id.navigation_balance_history), R.color.offWhite);
					changeDrawableColor(nav.findViewById(R.id.navigation_home), R.color.offWhite);
			}
		});

		if (getIntent().getBooleanExtra(SettingsFragment.LANG_CHANGE, false))
			navController.navigate(R.id.navigation_security);
	}

	private void changeDrawableColor(TextView tv, int color) {
		for (Drawable d : tv.getCompoundDrawables()) {
			if (d != null)
				d.setColorFilter(new PorterDuffColorFilter(this.getResources().getColor(color), PorterDuff.Mode.SRC_IN));
		}
	}

	private void navigateOutsideHomeScreen(int toWhere, NavController navController) {
		PermissionHelper permissionHelper = new PermissionHelper(this);
		if(permissionHelper.hasPhonePerm() && permissionHelper.hasSmsPerm()) {
			askStarted = false;
			switch (toWhere) {
				case R.id.transfer: startTransfer(Action.P2P, false, getIntent()); break;
				case R.id.airtime: startTransfer(Action.AIRTIME, false, getIntent()); break;
				case R.id.request: startActivityForResult(new Intent(this, RequestActivity.class), Constants.REQUEST_REQUEST); break;
				case R.id.navigation_home: navController.navigate(R.id.navigation_home); break;
				case R.id.navigation_balance_history: navController.navigate(R.id.navigation_balance_history); break;
				case R.id.navigation_security: navController.navigate(R.id.navigation_security); break;
				default: break;
			}
		}
		else if (askStarted) requestNext();
		else PermissionUtils.showInformativeBasicPermissionDialog(posBtn-> startRequest(true), null, this);
	}

	@Override
	public void goToBuyAirtimeScreen(int resId) {
		navigateToWhere = resId;
		navigateOutsideHomeScreen(resId, null);
	}

	@Override
	public void goToRequestMoneyScreen(int resId) {
		navigateToWhere = resId;
		navigateOutsideHomeScreen(resId, null);
	}

	@Override
	public void goToSendMoneyScreen(int resId) {
		navigateToWhere = resId;
		navigateOutsideHomeScreen(resId, null);
	}



	//PERMISSIONS

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (PermissionUtils.permissionsGranted(grantResults)) navigateOutsideHomeScreen(navigateToWhere, navController);
		else requestNext();
	}

	private void startRequest(boolean isForBasicPermission) {
		askStarted = true;
		currentAskIsForBasicPermssion = isForBasicPermission;
		requestNext();
	}

	private void requestNext() {
		PermissionHelper ph = new PermissionHelper(this);
		if (currentAskIsForBasicPermssion && !ph.hasPhonePerm())
			requestPhone(ph);
		else if (!ph.hasSmsPerm())
			requestSMS(ph);
		else if (!currentAskIsForBasicPermssion && !ph.hasOverlayPerm())
			requestOverlay();
		else if (!currentAskIsForBasicPermssion && !ph.hasAccessPerm())
			requestAccessibility();
		else Amplitude.getInstance().logEvent(getString(R.string.granted_sdk_permissions));
	}

	private void requestPhone(PermissionHelper ph) {
		Amplitude.getInstance().logEvent(getString(R.string.request_permphone));
		ph.requestPhone(this, PHONE_REQUEST);
	}

	private void requestSMS(PermissionHelper ph) {
		Amplitude.getInstance().logEvent(getString(R.string.request_permsms));
		ph.requestBasicPerms(this, SMS_REQUEST);
	}

	public void requestOverlay() {
		Amplitude.getInstance().logEvent(getString(R.string.request_permoverlay));
		if (dialog != null) dialog.dismiss();
		dialog = new PermissionDialog(this, PermissionDialog.OVERLAY).createDialog(this);
	}

	public void requestAccessibility() {
		Amplitude.getInstance().logEvent(getString(R.string.request_permaccessibility));
		if (dialog != null) dialog.dismiss();
		dialog = new PermissionDialog(this, PermissionDialog.ACCESS).createDialog(this);
	}

}


