package com.hover.stax.home;

import android.annotation.SuppressLint;
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

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.amplitude.api.Amplitude;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.hover.sdk.transactions.TransactionContract;
import com.hover.stax.R;
import com.hover.stax.actions.Action;
import com.hover.stax.channels.ChannelsActivity;
import com.hover.stax.database.Constants;
import com.hover.stax.hover.HoverSession;
import com.hover.stax.requests.RequestActivity;
import com.hover.stax.schedules.Schedule;
import com.hover.stax.security.BiometricChecker;
import com.hover.stax.security.SecurityFragment;
import com.hover.stax.transactions.TransactionHistoryViewModel;
import com.hover.stax.transfers.TransferActivity;
import com.hover.stax.utils.DateUtils;
import com.hover.stax.utils.UIHelper;
import com.hover.stax.utils.Utils;
import com.hover.stax.utils.customSwipeRefresh.CustomSwipeRefreshLayout;


public class MainActivity extends AppCompatActivity implements
	BalancesViewModel.RunBalanceListener, BalanceAdapter.BalanceListener, BiometricChecker.AuthListener, SwipeAllBalanceListener {

	final public static String TAG = "MainActivity";
	private BalancesViewModel balancesViewModel;
	private CustomSwipeRefreshLayout swipeRefreshLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);

		balancesViewModel = new ViewModelProvider(this).get(BalancesViewModel.class);
		balancesViewModel.setListener(this);
		balancesViewModel.getSelectedChannels().observe(this, channels -> Log.i(TAG, "This observer is neccessary to make updates fire, but all logic is in viewmodel"));
		balancesViewModel.getBalanceActions().observe(this, actions -> Log.i(TAG, "This observer is neccessary to make updates fire, but all logic is in viewmodel"));
		balancesViewModel.getToRun().observe(this, actions -> Log.i(TAG, "This observer is neccessary to make updates fire, but all logic is in viewmodel"));

		setUpNav();
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
		if (i == 0)
			new BiometricChecker(this, this).startAuthentication(a);
		else run(a, i);
	}

	private void run(Action action, int index) {
		new HoverSession.Builder(action, balancesViewModel.getChannel(action.channel_id), this, index)
				.finalScreenTime(0)
				.run();
	}

	@Override
	public void onAuthError(String error) {
		if(swipeRefreshLayout !=null) new Handler(Looper.getMainLooper()).postDelayed(()->swipeRefreshLayout.refreshComplete(), 1000);
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
				if (resultCode == RESULT_OK && data != null && data.getAction() != null) { onProbableHoverCall(data); }
				else Amplitude.getInstance().logEvent(getString(R.string.sdk_failure));
				break;
			case Constants.ADD_SERVICE:
				onAddServices(resultCode);
				break;
			case Constants.REQUEST_REQUEST:
				if (resultCode == RESULT_OK) { onRequest(data); }
				break;
			default: // requestCode < Constants.BALANCE_MAX // Balance call
				if(swipeRefreshLayout !=null) new Handler(Looper.getMainLooper()).postDelayed(()->swipeRefreshLayout.refreshComplete(), 1000);
				balancesViewModel.setRan(requestCode);
				if (resultCode == RESULT_OK && data != null && data.getAction() != null) {
					onProbableHoverCall(data);
					maybeRunShowcase();
				}
		}
	}

	private void onProbableHoverCall(Intent data) {
		if (data.getAction().equals(Constants.SCHEDULED)) {
			UIHelper.flashMessage(this, findViewById(R.id.home_root),
				getString(R.string.toast_confirm_schedule, DateUtils.humanFriendlyDate(data.getIntExtra(Schedule.DATE_KEY, 0))));
		} else {
			Amplitude.getInstance().logEvent(getString(R.string.finish_load_screen));
			new ViewModelProvider(this).get(TransactionHistoryViewModel.class).saveTransaction(data, this);
		}
	}

	private void onAddServices(int resultCode) {
		if (resultCode == RESULT_OK)
			balancesViewModel.setRunning();
		maybeRunShowcase();
	}
	private void maybeRunShowcase() {
		if (balancesViewModel.hasChannels() && Utils.getSharedPrefs(this).getInt(ShowcaseExecutor.SHOW_TUTORIAL, 0) == 0)
			new ShowcaseExecutor(this, findViewById(R.id.home_root)).startShowcasing();
	}

	private void startTransfer(String type) {
		Intent i = new Intent(this, TransferActivity.class);
		i.setAction(type);
		startActivityForResult(i, Constants.TRANSFER_REQUEST);
	}

	private void onRequest(Intent data) {
		if (data.getAction().equals(Constants.SCHEDULED))
			showMessage(getString(R.string.toast_request_scheduled, DateUtils.humanFriendlyDate(data.getIntExtra(Schedule.DATE_KEY, 0))));
		else
			showMessage(getString(R.string.toast_confirm_request));
	}

	private void showMessage(String str) {
		UIHelper.flashMessage(this, findViewById(R.id.home_root), str);
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
		FloatingActionButton fab = setupFloatingButton();
		BottomAppBar nav = findViewById(R.id.nav_view);
		NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
		AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
		NavigationUI.setupWithNavController(nav, navController, appBarConfiguration);

		nav.setOnMenuItemClickListener((Toolbar.OnMenuItemClickListener) item -> {
			switch (item.getItemId()) {
				case R.id.navigation_home: navController.navigate(R.id.navigation_home); break;
				case R.id.navigation_security: navController.navigate(R.id.navigation_security); break;
			}
            return false;
		});

		navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
			switch (destination.getId()) {
				case R.id.navigation_security:
					changeDrawableColor(nav.findViewById(R.id.navigation_home), R.color.offWhite);
					changeDrawableColor(nav.findViewById(R.id.navigation_security), R.color.brightBlue);
					fab.hide();
					break;
				case R.id.navigation_home:
					changeDrawableColor(nav.findViewById(R.id.navigation_security), R.color.offWhite);
					changeDrawableColor(nav.findViewById(R.id.navigation_home), R.color.brightBlue);
					fab.show();
					break;
				default:
					changeDrawableColor(nav.findViewById(R.id.navigation_security), R.color.offWhite);
					changeDrawableColor(nav.findViewById(R.id.navigation_home), R.color.offWhite);
					fab.show();
			}
		});

		if (getIntent().getBooleanExtra(SecurityFragment.LANG_CHANGE, false))
			navController.navigate(R.id.navigation_security);
	}

	private void changeDrawableColor(TextView tv, int color) {
		for (Drawable d : tv.getCompoundDrawables()) {
			if (d != null)
				d.setColorFilter(new PorterDuffColorFilter(this.getResources().getColor(color), PorterDuff.Mode.SRC_IN));
		}
	}

	@SuppressLint("UseCompatLoadingForDrawables")
	FloatingActionButton setupFloatingButton() {
		FloatingActionButton fab = findViewById(R.id.fab);
		fab.setOnClickListener(view -> {
			fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_close));
			PopupMenu popup = new PopupMenu(MainActivity.this, fab);
			popup.getMenuInflater().inflate(R.menu.fab_menu, popup.getMenu());

			popup.setOnMenuItemClickListener(item -> {
				switch (item.getItemId()) {
					case R.id.transfer: startTransfer(Action.P2P); break;
					case R.id.airtime: startTransfer(Action.AIRTIME); break;
					case R.id.request: startActivityForResult(new Intent(this, RequestActivity.class), Constants.REQUEST_REQUEST); break;
					default: break;
				}
				return true;
			});
			popup.setOnDismissListener(menu -> fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_money)));

			popup.show();
		});
		return fab;
	}

	@Override
	public void triggerRefresh(CustomSwipeRefreshLayout swipeRefreshLayout) {
		this.swipeRefreshLayout = swipeRefreshLayout;
		runAllBalances(null);
	}
}


