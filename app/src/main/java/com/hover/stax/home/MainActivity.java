package com.hover.stax.home;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
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
import com.hover.stax.channels.ChannelsActivity;
import com.hover.stax.hover.HoverSession;
import com.hover.stax.requests.RequestActivity;
import com.hover.stax.schedules.Schedule;
import com.hover.stax.security.BiometricChecker;
import com.hover.stax.security.SecurityFragment;

import com.hover.stax.transactions.TransactionHistoryViewModel;
import com.hover.stax.transfers.TransferActivity;
import com.hover.stax.utils.DateUtils;
import com.hover.stax.utils.UIHelper;
import com.wangjie.rapidfloatingactionbutton.RapidFloatingActionButton;
import com.wangjie.rapidfloatingactionbutton.RapidFloatingActionHelper;
import com.wangjie.rapidfloatingactionbutton.RapidFloatingActionLayout;
import com.wangjie.rapidfloatingactionbutton.contentimpl.labellist.RFACLabelItem;
import com.wangjie.rapidfloatingactionbutton.contentimpl.labellist.RapidFloatingActionContentLabelList;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements BalancesViewModel.RunBalanceListener, BalanceAdapter.BalanceListener, BiometricChecker.AuthListener, RapidFloatingActionContentLabelList.OnRapidFloatingActionContentLabelListListener {
	final public static String TAG = "MainActivity";

	final public static int ADD_SERVICE = 200;

	private BalancesViewModel balancesViewModel;
	private RapidFloatingActionHelper rfabHelper;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);
		BottomNavigationView navView = findViewById(R.id.nav_view);
		// Passing each menu ID as a set of Ids because each
		// menu should be considered as top level destinations.
		AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
				R.id.navigation_home, R.id.navigation_security).build();
		NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
		NavigationUI.setupWithNavController(navView, navController);

		setupFloatingButton();

		balancesViewModel = new ViewModelProvider(this).get(BalancesViewModel.class);
		balancesViewModel.setListener(this);
		balancesViewModel.getBalanceActions().observe(this, actions -> Log.i(TAG, "This observer is neccessary to make updates fire, but all logic is in viewmodel") );
		balancesViewModel.getToRun().observe(this, actions -> Log.i(TAG, "This observer is neccessary to make updates fire, but all logic is in viewmodel") );

		if (getIntent().getBooleanExtra(SecurityFragment.LANG_CHANGE, false))
			navController.navigate(R.id.navigation_security);
	}

	void setupFloatingButton() {
		RapidFloatingActionContentLabelList rfaContent = new RapidFloatingActionContentLabelList(this);
		rfaContent.setOnRapidFloatingActionContentLabelListListener(this);
		List<RFACLabelItem> items = new ArrayList<>();
		items.add(new RFACLabelItem<Integer>()
			.setLabel(getResources().getString(R.string.transfer))
			.setLabelSizeSp(21)
			.setLabelColor(getResources().getColor(R.color.offWhite))
			.setIconNormalColor(R.color.cardViewColor)
			.setLabelBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.cardViewColor)))
			.setWrapper(0)
		);
		items.add(new RFACLabelItem<Integer>()
			.setLabel(getResources().getString(R.string.nav_airtime))
			.setLabelSizeSp(21)
			.setLabelColor(getResources().getColor(R.color.offWhite))
			.setLabelBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.cardViewColor)))
			.setWrapper(1)
		);
		items.add(new RFACLabelItem<Integer>()
			.setLabel(getResources().getString(R.string.title_request))
			.setLabelSizeSp(21)
			.setLabelColor(getResources().getColor(R.color.offWhite))
			.setLabelBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.cardViewColor)))
			.setWrapper(2));


		rfaContent.setItems(items);

		RapidFloatingActionButton rfaBtn = findViewById(R.id.activity_main_rfab);
		RapidFloatingActionLayout rfaLayout = findViewById(R.id.container);

		rfaLayout.setIsContentAboveLayout(true);

		rfaLayout.setFrameColor(getResources().getColor(R.color.cardViewColor));
//		rfaLayout.setDisableContentDefaultAnimation(true);
		rfabHelper = new RapidFloatingActionHelper(this, rfaLayout, rfaBtn, rfaContent).build();
	}

	public void addAccount(View view) {
		Amplitude.getInstance().logEvent(getString(R.string.click_add_account));
		startActivityForResult(new Intent(this, ChannelsActivity.class), MainActivity.ADD_SERVICE);
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

		if ((requestCode == TransferActivity.TRANSFER_REQUEST || requestCode < 100) && resultCode == RESULT_OK)
			onProbableHoverCall(data);

		if (requestCode < 100) { // Balance call
			balancesViewModel.setRan(requestCode);
		} else if (requestCode == ADD_SERVICE && resultCode == RESULT_OK) {
			balancesViewModel.setRunning();
		}

		if ((requestCode == RequestActivity.REQUEST_REQUEST) && resultCode == RESULT_OK)
			UIHelper.flashMessage(this, findViewById(R.id.content), getString(R.string.request_sent));
	}

	private void onProbableHoverCall(Intent data) {
		if (data.getAction().equals(TransferActivity.TRANSFERED)) {
			Amplitude.getInstance().logEvent(getString(R.string.finish_load_screen));
			new ViewModelProvider(this).get(TransactionHistoryViewModel.class).saveTransaction(data, this);
		} else if (data.getAction().equals(TransferActivity.SCHEDULED)) {
			UIHelper.flashMessage(this, findViewById(R.id.content),
				getString(R.string.schedule_created, DateUtils.humanFriendlyDate(data.getIntExtra(Schedule.DATE_KEY, 0))));
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
			case 0: startTransfer(Action.P2P); break;
			case 1: startTransfer(Action.AIRTIME); break;
			case 2: startActivityForResult(new Intent(this, RequestActivity.class), RequestActivity.REQUEST_REQUEST); break;
			case 3: Navigation.findNavController(findViewById(R.id.nav_host_fragment)).navigate(R.id.navigation_security); break;
			default: Navigation.findNavController(findViewById(R.id.nav_host_fragment)).navigate(R.id.navigation_home);
		}
		rfabHelper.toggleContent();
	}

	private void startTransfer(String type) {
		Intent i = new Intent(this, TransferActivity.class);
		i.setAction(type);
		startActivityForResult(i, TransferActivity.TRANSFER_REQUEST);
	}

	@Override
	public void onRFACItemIconClick(int position, RFACLabelItem item) {
		rfabHelper.toggleContent();
	}
}


