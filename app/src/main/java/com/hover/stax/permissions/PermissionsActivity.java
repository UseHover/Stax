package com.hover.stax.permissions;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.hover.sdk.actions.ActionContract;
import com.hover.sdk.permissions.PermissionHelper;
import com.hover.stax.R;
import com.hover.stax.actions.Action;

public class PermissionsActivity extends AppCompatActivity {

	private int mStackLevel = 0;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_permissions);

		mStackLevel = 0;
		if (new PermissionHelper(this).hasAllPerms()) {
			setResult(RESULT_OK);
			finish();
		} else showDialog();
	}

	void showDialog() {
		mStackLevel++;

		// DialogFragment.show() will take care of adding the fragment
		// in a transaction.  We also want to remove any currently showing
		// dialog, so make our own transaction and take care of that here.
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		Fragment prev = getSupportFragmentManager().findFragmentByTag("dialog");
		if (prev != null) {
			ft.remove(prev);
		}
		ft.addToBackStack(null);

		// Create and show the dialog.
		PermissionsFragment newFragment = PermissionsFragment.newInstance(getReason(), new PermissionHelper(this));
		newFragment.show(ft, "dialog");
	}

	private String getReason() {
		if (getIntent() != null && getIntent().getStringExtra(ActionContract.COLUMN_TRANSACTION_TYPE) != null) {
			String type = getIntent().getStringExtra(ActionContract.COLUMN_TRANSACTION_TYPE);
			if (type.equals(Action.AIRTIME)) return getString(R.string.buy_airtime);
			else if (type.equals(Action.BALANCE)) return getString(R.string.check_balance);
			else if (type.equals(Action.P2P)) return getString(R.string.send_money);
			else if (type.equals(Action.ME2ME)) return getString(R.string.move_money);
		}
		return getString(R.string.use_ussd);
	}
}
