package com.hover.stax.requests;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.hover.stax.R;
import com.hover.stax.home.MainActivity;
import com.hover.stax.utils.UIHelper;

public class ProcessingActivity extends AppCompatActivity {
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.request_process);
		new Handler().postDelayed(() -> {
			UIHelper.flashMessage(ProcessingActivity.this, getResources().getString(R.string.requests_sent));
			startActivity(new Intent(ProcessingActivity.this, MainActivity.class));
			finishAffinity();
		}, 4000);
	}
}
