package com.hover.stax.permissions;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.hover.sdk.permissions.PermissionHelper;
import com.hover.stax.R;
import com.hover.stax.actions.Action;

public class PermissionsActivity extends AppCompatActivity {

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_permissions);

		if (new PermissionHelper(this).hasAllPerms()) {
			setResult(RESULT_OK);
			finish();
		} else showDialog();
	}

	void showDialog() {
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		Fragment prev = getSupportFragmentManager().findFragmentByTag("dialog");
		if (prev != null) { ft.remove(prev); }
		ft.addToBackStack(null);

		PermissionsFragment newFragment = PermissionsFragment.newInstance(getReason(), new PermissionHelper(this));
		newFragment.show(ft, "dialog");
	}

	private String getReason() {
		if (getIntent() != null && getIntent().getStringExtra("transaction_type") != null) {
			String type = getIntent().getStringExtra("transaction_type");
			if (type.equals(Action.AIRTIME)) return getString(R.string.buy_airtime);
			else if (type.equals(Action.BALANCE)) return getString(R.string.check_balance);
			else if (type.equals(Action.P2P)) return getString(R.string.send_money);
			else if (type.equals(Action.ME2ME)) return getString(R.string.move_money);
		}
		return getString(R.string.use_ussd);
	}
}
