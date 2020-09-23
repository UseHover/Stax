package com.hover.stax.home;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.amplitude.api.Amplitude;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.hover.sdk.transactions.TransactionContract;
import com.hover.stax.R;
import com.hover.stax.actions.Action;
import com.hover.stax.hover.HoverSession;
import com.hover.stax.security.BiometricChecker;
import com.hover.stax.security.SecurityFragment;
import com.hover.stax.utils.UIHelper;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements BalanceAdapter.BalanceListener, BiometricChecker.AuthListener {
	final public static String TAG = "MainActivity";

	final public static String CHECK_ALL_BALANCES = "CHECK_ALL";
	final public static int ADD_SERVICE = 200, TRANSFER_REQUEST = 203;

	private HomeViewModel homeViewModel;
	private static List<Action> allBalanceActions;
	private static Action toRun = null;
	private static int index, runCount = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		BottomNavigationView navView = findViewById(R.id.nav_view);
		// Passing each menu ID as a set of Ids because each
		// menu should be considered as top level destinations.
		AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
				R.id.navigation_home, R.id.navigation_buyAirtime, R.id.navigation_moveMoney, R.id.navigation_security)
														  .build();
		NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
		NavigationUI.setupWithNavController(navView, navController);

		homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
		homeViewModel.getBalanceActions().observe(this, actions -> {
			if (actions != null) {
				allBalanceActions = actions;
				Log.d("CYCLER", "updated actions here");
			}
		});

		if (getIntent().getBooleanExtra(SecurityFragment.LANG_CHANGE, false)) navController.navigate(R.id.navigation_security);
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.d("CYCLE","ON PAUSE CALLED");
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.d("CYCLE","ON RESUME CALLED");
	}

	@Override
	public void onTapDetail(int channel_id) {
		Bundle bundle = new Bundle();
		bundle.putInt(TransactionContract.COLUMN_CHANNEL_ID, channel_id);
		Navigation.findNavController(findViewById(R.id.nav_host_fragment)).navigate(R.id.channelsDetailsFragment, bundle);
	}

	@Override
	public void onTapRefresh(int channel_id) {
		runCount = 0;
		Amplitude.getInstance().logEvent(getString(R.string.refresh_balance_single));
		for (Action action: allBalanceActions)
			if (action.channel_id == channel_id) {
				prepareRun(action, 0);
				return;
			}
	}

	public void runAllBalances(View view) {
		Amplitude.getInstance().logEvent(getString(R.string.refresh_balance_all));
		runCount = allBalanceActions.size();
		Log.d("CYCLER", "called run action");
		if(allBalanceActions.size() > 0) prepareRun(allBalanceActions.get(0), 0);
	}

	private void prepareRun(Action a, int i) {
		if (allBalanceActions == null || allBalanceActions.size() == 0) {
			UIHelper.flashMessage(this, "Error, no balance actions found.");
			return;
		}
		index = i;
		toRun = a;
		if (index == 0)
			new BiometricChecker(this, this).startAuthentication();
		else run(toRun, index);
	}

	@Override
	public void onAuthError(String error) {
		Log.e(TAG, "error: " + error);
	}

	@Override
	public void onAuthSuccess() {
		run(toRun, index);
	}

	private void run(Action action, int index) {
		new HoverSession.Builder(action, homeViewModel.getChannel(action.channel_id), this, index)
			.finalScreenTime(0).run();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_CANCELED) return;
		if (requestCode == MainActivity.TRANSFER_REQUEST || requestCode < 100) {
			Amplitude.getInstance().logEvent(getString(R.string.finish_load_screen));
			homeViewModel.saveTransaction(data, this);
			Navigation.findNavController(findViewById(R.id.nav_host_fragment)).navigate(R.id.navigation_home);
		}

		if (requestCode < 100) {
			index++;
			if (index < runCount)
				prepareRun(allBalanceActions.get(index), index);
		} else if (requestCode == ADD_SERVICE) {
			//ADDED THIS BECAUSE runAllBalances gets called first before actions is being updated from view model observer
				new Handler().postDelayed(() -> runAllBalances(null), 700);

		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				onBackPressed();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}
}


