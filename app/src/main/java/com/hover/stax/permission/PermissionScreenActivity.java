package com.hover.stax.permission;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.hover.sdk.permissions.PermissionActivity;
import com.hover.stax.R;
import com.hover.stax.utils.PermissionUtils;
import com.hover.stax.utils.UIHelper;

public class PermissionScreenActivity extends AppCompatActivity {
private final int PERMISSION_REQ_CODE = 201;
@Override
protected void onCreate(@Nullable Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.permissions_activity);

	findViewById(R.id.permission_button).setOnClickListener(view-> {
		if (!PermissionUtils.has(new String[]{ Manifest.permission.READ_PHONE_STATE, Manifest.permission.CALL_PHONE}, this)) {
			startActivityForResult(new Intent(this, PermissionActivity.class), PERMISSION_REQ_CODE);
		}
	});
}

@Override
protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
	super.onActivityResult(requestCode, resultCode, data);
	if(requestCode == PERMISSION_REQ_CODE) {
		if(resultCode != RESULT_OK) {
			UIHelper.flashMessage(this, getCurrentFocus(), getResources().getString(R.string.permission_failure));
		}
		else {
			UIHelper.flashMessage(this, getCurrentFocus(), getResources().getString(R.string.permission_success));
			finish();
		}
	}
}
}
