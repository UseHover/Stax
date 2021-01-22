package com.hover.stax.permissions;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.hover.sdk.permissions.PermissionHelper;
import com.hover.stax.R;

public class PermissionsActivity extends AppCompatActivity {

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pins);

		if (new PermissionHelper(this).hasAllPerms()){
			setResult(RESULT_OK);
			finish();
		}
	}
}
