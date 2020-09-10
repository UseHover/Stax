package com.hover.stax.security;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.amplitude.api.Amplitude;
import com.hover.sdk.permissions.PermissionActivity;
import com.hover.stax.R;
import com.hover.stax.channels.ChannelsActivity;
import com.hover.stax.utils.PermissionUtils;
import com.hover.stax.utils.UIHelper;

public class PermissionScreenActivity extends AppCompatActivity {
	private final int PERMISSION_REQ_CODE = 201;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (PermissionUtils.hasRequiredPermissions()) {
			startActivity(new Intent(this, ChannelsActivity.class));
			finish();
		}
		setContentView(R.layout.permissions_activity);

		findViewById(R.id.permission_button).setOnClickListener(view -> {
			if (!PermissionUtils.hasRequiredPermissions()) {
				Amplitude.getInstance().logEvent(getString(R.string.request_permissions));
				startActivityForResult(new Intent(this, PermissionActivity.class), PERMISSION_REQ_CODE);
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == PERMISSION_REQ_CODE) {
			if (resultCode != RESULT_OK) {
				Amplitude.getInstance().logEvent(getString(R.string.denied_sdk_permissions));
				UIHelper.flashMessage(this, getCurrentFocus(), getResources().getString(R.string.permission_failure));
			} else {
				Amplitude.getInstance().logEvent(getString(R.string.granted_sdk_permissions));
				UIHelper.flashMessage(this, getCurrentFocus(), getResources().getString(R.string.permission_success));
				startActivity(new Intent(this, ChannelsActivity.class));
				finish();
			}
		}
	}
}
