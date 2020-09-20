package com.hover.stax;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.WorkManager;

import com.amplitude.api.Amplitude;
import com.hover.sdk.api.Hover;
import com.hover.sdk.utils.Utils;
import com.hover.stax.channels.UpdateChannelsWorker;
import com.hover.stax.home.MainActivity;
import com.hover.stax.languages.SelectLanguageActivity;

public class SplashScreenActivity extends AppCompatActivity {
	final public static String LANGUAGE_CHECK = "Language";
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Amplitude.getInstance().initialize(this, "9275a8bd8ab3037bd1f8e072f83548d3").enableForegroundTracking(getApplication());
		Hover.initialize(this);
		Hover.setBranding("Stax", R.mipmap.fullsize_logo, this);
		WorkManager wm = WorkManager.getInstance(this);
		wm.beginUniqueWork(UpdateChannelsWorker.CHANNELS_WORK_ID, ExistingWorkPolicy.KEEP, UpdateChannelsWorker.makeWork()).enqueue();
		wm.enqueueUniquePeriodicWork(UpdateChannelsWorker.TAG, ExistingPeriodicWorkPolicy.KEEP, UpdateChannelsWorker.makeToil());

		if (Utils.getSharedPrefs(ApplicationInstance.getContext()).getInt(LANGUAGE_CHECK, 0) == 1)
			startActivity(new Intent(this, MainActivity.class));
		else
			startActivity(new Intent(this, SelectLanguageActivity.class));
		finish();
	}
}
