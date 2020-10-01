package com.hover.stax.home;

import android.content.Intent;
import android.os.Bundle;
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

public class MainActivity extends AppCompatActivity implements HomeViewModel.RunBalanceListener, BalanceAdapter.BalanceListener, BiometricChecker.AuthListener {
	final public static String TAG = "MainActivity";

	final public static int ADD_SERVICE = 200, TRANSFER_REQUEST = 203;

	private HomeViewModel homeViewModel;

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
		homeViewModel.setListener(this);
		homeViewModel.getBalanceActions().observe(this, actions -> Log.e(TAG, "This observer is neccessary to make updates fire, but all logic is in viewmodel") );
		homeViewModel.getToRun().observe(this, actions -> Log.i(TAG, "This observer is neccessary to make updates fire, but all logic is in viewmodel") );

		if (getIntent().getBooleanExtra(SecurityFragment.LANG_CHANGE, false)) navController.navigate(R.id.navigation_security);
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
		homeViewModel.setRunning(channel_id);
	}

	public void runAllBalances(View view) {
		Amplitude.getInstance().logEvent(getString(R.string.refresh_balance_all));
		homeViewModel.setRunning();
	}

	@Override
	public void startRun(Action a, int i) {
		if (i == 0)
			new BiometricChecker(this, this).startAuthentication(a);
		else run(a, i);
	}

	private void run(Action action, int index) {
		new HoverSession.Builder(action, homeViewModel.getChannel(action.channel_id), this, index)
			.finalScreenTime(0).run();
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
		if (resultCode == RESULT_CANCELED) return;
		if (requestCode == MainActivity.TRANSFER_REQUEST || requestCode < 100) {
			Amplitude.getInstance().logEvent(getString(R.string.finish_load_screen));
			homeViewModel.saveTransaction(data, this);
			Navigation.findNavController(findViewById(R.id.nav_host_fragment)).navigate(R.id.navigation_home);
		}

		if (requestCode < 100) {
			homeViewModel.setRan(requestCode);
		} else if (requestCode == ADD_SERVICE) {
			homeViewModel.setRunning();
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


