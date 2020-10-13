package com.hover.stax.security;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.RecyclerView;

import com.amplitude.api.Amplitude;
import com.hover.sdk.permissions.PermissionHelper;
import com.hover.stax.R;
import com.hover.stax.requests.RequestDetailFragment;
import com.hover.stax.utils.UIHelper;

public class PinsActivity extends AppCompatActivity {

	private PinsViewModel pinViewModel;

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
		AlertDialog alertDialog = new AlertDialog.Builder(this, R.style.StaxDialog)
			.setMessage(R.string.check_balance_ask)
			.setNegativeButton(R.string.skip, (DialogInterface.OnClickListener) (dialog, whichButton) -> cancel(null))
			.setPositiveButton(R.string.continue_text, (DialogInterface.OnClickListener) (dialog, whichButton) -> checkBalances())
			.create();
		alertDialog.show();
	}

	public void skipPins(View view) {
		AlertDialog alertDialog = new AlertDialog.Builder(this, R.style.StaxDialog)
			.setMessage(R.string.ask_every_time)
			.setPositiveButton(R.string.ok, (DialogInterface.OnClickListener) (dialog, whichButton) -> balanceAsk())
			.create();
		alertDialog.show();
	}

	public void learnMore(View view) {
		AlertDialog alertDialog = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.StaxDialog))
			.setTitle(R.string.about_our_security)
			.setMessage(R.string.about_our_security_content)
			.setPositiveButton(R.string.ok, (DialogInterface.OnClickListener) (dialog, whichButton) -> {})
			.create();
		alertDialog.show();
	}
}
