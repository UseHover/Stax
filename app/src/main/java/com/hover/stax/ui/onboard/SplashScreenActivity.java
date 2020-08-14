package com.hover.stax.ui.onboard;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.hover.stax.MainActivity;
import com.hover.stax.R;

public class SplashScreenActivity extends AppCompatActivity {
@Override
protected void onCreate(@Nullable Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.intro_layout);

	new Handler().postDelayed(new Runnable() {
		@Override
		public void run() {
			MainActivity.GO_TO_SPLASH_SCREEN = false;
			startActivity(new Intent(SplashScreenActivity.this, MainActivity.class));
			finishAffinity();
		}
	}, 1500);
}
}
