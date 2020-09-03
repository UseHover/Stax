package com.hover.stax.ui.onboard;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.WorkManager;

import com.hover.sdk.api.Hover;
import com.hover.stax.MainActivity;
import com.hover.stax.channels.UpdateChannelsWorker;

public class SplashScreenActivity extends AppCompatActivity {
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Hover.initialize(this);
		WorkManager wm = WorkManager.getInstance(this);
		wm.beginUniqueWork(UpdateChannelsWorker.CHANNELS_WORK_ID, ExistingWorkPolicy.KEEP, UpdateChannelsWorker.makeWork()).enqueue();
		wm.enqueueUniquePeriodicWork(UpdateChannelsWorker.TAG, ExistingPeriodicWorkPolicy.KEEP, UpdateChannelsWorker.makeToil());

		startActivity(new Intent(this, MainActivity.class));
		finish();
	}
}
