package com.hover.stax.home;

import android.content.Intent;
import android.graphics.Color;
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
import com.wangjie.rapidfloatingactionbutton.RapidFloatingActionButton;
import com.wangjie.rapidfloatingactionbutton.RapidFloatingActionHelper;
import com.wangjie.rapidfloatingactionbutton.RapidFloatingActionLayout;
import com.wangjie.rapidfloatingactionbutton.contentimpl.labellist.RFACLabelItem;
import com.wangjie.rapidfloatingactionbutton.contentimpl.labellist.RapidFloatingActionContentLabelList;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements BalanceAdapter.BalanceListener, BiometricChecker.AuthListener, RapidFloatingActionContentLabelList.OnRapidFloatingActionContentLabelListListener {
	final public static String TAG = "MainActivity";

	final public static String CHECK_ALL_BALANCES = "CHECK_ALL";
	final public static int ADD_SERVICE = 200, TRANSFER_REQUEST = 203;

	private HomeViewModel homeViewModel;
	private RapidFloatingActionHelper rfabHelper;
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

		setupFloatingButton();

		homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
		homeViewModel.getBalanceActions().observe(this, actions -> {
			if (actions != null) {
				allBalanceActions = actions;
			}
		});

		if (getIntent().getBooleanExtra(SecurityFragment.LANG_CHANGE, false)) navController.navigate(R.id.navigation_security);
	}

	void setupFloatingButton() {
		RapidFloatingActionContentLabelList rfaContent = new RapidFloatingActionContentLabelList(this);
		rfaContent.setOnRapidFloatingActionContentLabelListListener(this);
		List<RFACLabelItem> items = new ArrayList<>();
		items.add(new RFACLabelItem<Integer>()
						  .setLabel(getResources().getString(R.string.transfer))
						  .setLabelSizeSp(21)
						  .setLabelColor(getResources().getColor(R.color.colorAccentDark))
						  .setWrapper(0)
		);
		items.add(new RFACLabelItem<Integer>()
						  .setLabel(getResources().getString(R.string.nav_airtime))
						  .setLabelSizeSp(21)
						  .setLabelColor(getResources().getColor(R.color.colorAccentDark))
						  .setWrapper(1)
		);
		/*items.add(new RFACLabelItem<Integer>()
						  .setLabel(getResources().getString(R.string.title_request))
						  .setLabelSizeSp(21)
						  .setWrapper(2)); */


		rfaContent.setItems(items);

		RapidFloatingActionButton rfaBtn = findViewById(R.id.activity_main_rfab);
		RapidFloatingActionLayout rfaLayout = findViewById(R.id.container);

		rfaLayout.setIsContentAboveLayout(true);
		rfaLayout.setFrameAlpha(0.92f);

		rfaLayout.setFrameColor(getResources().getColor(R.color.cardViewColor));
		rfaLayout.setDisableContentDefaultAnimation(true);


		rfabHelper = new RapidFloatingActionHelper(
				this,
				rfaLayout,
				rfaBtn,
				rfaContent
		).build();
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
		if (allBalanceActions == null || allBalanceActions.size() == 0) {
			UIHelper.flashMessage(this, "Error, no balance actions found.");
			return;
		}
		for (Action action: allBalanceActions)
			if (action.channel_id == channel_id) {
				prepareRun(action, 0);
				return;
			}
	}

	public void runAllBalances(View view) {
		Amplitude.getInstance().logEvent(getString(R.string.refresh_balance_all));
		if (allBalanceActions == null || allBalanceActions.size() == 0) {
			UIHelper.flashMessage(this, "Error, no balance actions found.");
			return;
		}
		runCount = allBalanceActions.size();
		prepareRun(allBalanceActions.get(0), 0);
	}

	private void prepareRun(Action a, int i) {
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
			if (index < runCount && allBalanceActions != null && allBalanceActions.size() > index)
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

	@Override
	public void onRFACItemLabelClick(int position, RFACLabelItem item) {
		switch (position) {
			case 0: Navigation.findNavController(findViewById(R.id.nav_host_fragment)).navigate(R.id.navigation_moveMoney);
			break;
			case 1: Navigation.findNavController(findViewById(R.id.nav_host_fragment)).navigate(R.id.navigation_buyAirtime);
			break;
			case 2: Navigation.findNavController(findViewById(R.id.nav_host_fragment)).navigate(R.id.navigation_security);
			break;
		}
		rfabHelper.toggleContent();
	}

	@Override
	public void onRFACItemIconClick(int position, RFACLabelItem item) {
		rfabHelper.toggleContent();
	}
}


