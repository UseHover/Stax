package com.hover.stax.permissions;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.hover.sdk.actions.HoverAction;
import com.hover.sdk.permissions.PermissionHelper;
import com.hover.stax.R;

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

		PermissionsFragment newFragment = PermissionsFragment.newInstance(HoverAction.getHumanFriendlyType(this, getIntent() != null ? getIntent().getStringExtra("transaction_type") : null), new PermissionHelper(this).hasOverlayPerm());
		newFragment.show(ft, "dialog");
	}
}
