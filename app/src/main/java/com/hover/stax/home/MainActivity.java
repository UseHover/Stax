package com.hover.stax.home;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.amplitude.api.Amplitude;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.hover.stax.R;
import com.hover.stax.actions.Action;
import com.hover.stax.hover.HoverSession;
import com.hover.stax.security.BiometricChecker;
import com.hover.stax.security.SecurityFragment;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements BalanceAdapter.RefreshListener, BiometricChecker.AuthListener {
	final public static String TAG = "MainActivity";

	final public static String CHECK_ALL_BALANCES = "CHECK_ALL";
	final public static int ADD_SERVICE = 200, TRANSFER_REQUEST = 203;

	private HomeViewModel homeViewModel;
	private static List<Action> toRun;
	private static List<String> hasRun;

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

		if (getIntent().getBooleanExtra(SecurityFragment.LANG_CHANGE, false)) navController.navigate(R.id.navigation_security);
	}

	public void runAllBalances() {
		hasRun = new ArrayList<>();
		homeViewModel.getBalanceActions().observe(this, actions -> {
			toRun = actions;
			new BiometricChecker(this, this).startAuthentication();
		});
	}

	@Override
	public void onTap(int channel_id) {
		hasRun = new ArrayList<>();
		Amplitude.getInstance().logEvent(getString(R.string.refresh_balance_single));
		homeViewModel.getBalanceAction(channel_id).observe(this, actions -> {
			toRun = actions;
			new BiometricChecker(this, this).startAuthentication();
		});
	}

	@Override
	public void onAuthError(String error) {
		Log.e(TAG, "error: " + error);
		chooseRun(0);
	}

	@Override
	public void onAuthSuccess() {
		Log.e(TAG, "success");
		chooseRun(0);
	}

	private void chooseRun(int index) {
		if (toRun != null && toRun.size() > hasRun.size()) {
			new HoverSession.Builder(toRun.get(index), homeViewModel.getChannel(toRun.get(index).channel_id), this, index)
					.finalScreenTime(0).run();
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_CANCELED) return;
		Log.e(TAG, "Activity result. request code: " + requestCode);
		if (requestCode == MainActivity.TRANSFER_REQUEST || requestCode < 100) {
			Amplitude.getInstance().logEvent(getString(R.string.finish_load_screen));
			homeViewModel.saveTransaction(data, this);
			Navigation.findNavController(findViewById(R.id.nav_host_fragment)).navigate(R.id.navigation_home);
		}

		if (requestCode < 100) { // Some fragments use request codes in in the 100's for unrelated stuff
			if (hasRun == null) {
				hasRun = new ArrayList<>();
			}
			hasRun.add(data.getStringExtra("action_id"));
			chooseRun(requestCode + 1);
		} else if (requestCode == ADD_SERVICE) {
			runAllBalances();
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


