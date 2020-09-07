package com.hover.stax;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.hover.sdk.api.Hover;
import com.hover.sdk.api.HoverParameters;
import com.hover.stax.actions.Action;
import com.hover.stax.channels.ChannelsActivity;
import com.hover.stax.channels.UpdateChannelsWorker;
import com.hover.stax.database.KeyStoreExecutor;
import com.hover.stax.home.BalanceModel;
import com.hover.stax.home.HomeViewModel;
import com.hover.stax.onboard.SplashScreenActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

	final public static String CHECK_ALL_BALANCES = "CHECK_ALL";

	private HomeViewModel homeViewModel;
	private List<String> toRun, hasRun;

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

		if (getIntent().getAction() != null && getIntent().getAction().equals(CHECK_ALL_BALANCES))
			runAllBalances();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		if (intent.getAction() != null && intent.getAction().equals(CHECK_ALL_BALANCES))
			runAllBalances();
	}

	private void runAllBalances() {
		homeViewModel.getBalanceActions().observe(this, actions -> {
			toRun = new ArrayList<>(actions.size());
			hasRun = new ArrayList<>();
			for (int a = 0; a < actions.size(); a++) {
				toRun.add(actions.get(a).public_id);
			}
			makeHoverCall(actions.get(0), 0);
		});
	}

	private void makeHoverCall(Action action, int runId) {
		HoverParameters.Builder builder = new HoverParameters.Builder(this);
		builder.request(action.public_id);
//			builder.setEnvironment(HoverParameters.PROD_ENV);
		builder.style(R.style.myHoverTheme);
		builder.finalMsgDisplayTime(2000);
		builder.extra("pin", KeyStoreExecutor.decrypt(homeViewModel.getChannel(action.channel_id).pin, ApplicationInstance.getContext()));
		Intent i = builder.buildIntent();
		startActivityForResult(i, runId);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		hasRun.add(data.getStringExtra("action_id"));
		if (toRun.size() > hasRun.size()) {
			makeHoverCall(homeViewModel.getAction(toRun.get(requestCode + 1)), requestCode + 1);
		}
	}
}


