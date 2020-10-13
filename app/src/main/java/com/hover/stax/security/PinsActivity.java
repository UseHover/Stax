package com.hover.stax.security;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.amplitude.api.Amplitude;
import com.hover.sdk.permissions.PermissionHelper;
import com.hover.stax.R;
import com.hover.stax.home.MainActivity;
import com.hover.stax.views.StaxDialog;

public class PinsActivity extends AppCompatActivity {

	private PinsViewModel pinViewModel;
	private AlertDialog dialog;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pins);
		pinViewModel = new ViewModelProvider(this).get(PinsViewModel.class);

		if (new PermissionHelper(this).hasAllPerms())
			Navigation.findNavController(findViewById(R.id.nav_host_fragment)).navigate(R.id.navigation_pin_entry);
	}

	public void done(View view) {
		Amplitude.getInstance().logEvent(getString(R.string.completed_pin_entry));
		pinViewModel.savePins(PinsActivity.this);
		balanceAsk();
	}

	public void checkBalances() {
		setResult(RESULT_OK);
		finish();
	}

	public void cancel(View view) {
		setResult(RESULT_CANCELED);
		finish();
	}

	public void balanceAsk() {
		dialog = new StaxDialog(this)
			.setDialogMessage(R.string.check_balance_ask)
			.setNegButton(R.string.skip, (View.OnClickListener) btn -> cancel(null))
			.setPosButton(R.string.check_balances, (View.OnClickListener) btn -> checkBalances())
			.showIt();
	}

	public void skipPins(View view) {
		dialog = new StaxDialog(this)
			.setDialogMessage(R.string.ask_every_time)
			.setPosButton(R.string.ok, (View.OnClickListener) btn -> balanceAsk())
			.showIt();
	}

	public void learnMore(View view) {
		dialog = new StaxDialog(this)
			.setDialogTitle(R.string.about_our_security)
			.setDialogMessage(R.string.about_our_security_content)
			.setPosButton(R.string.ok, (View.OnClickListener) btn -> {})
			.showIt();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (dialog != null) dialog.dismiss();
	}
}
