package com.hover.stax;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.WorkManager;

import com.amplitude.api.Amplitude;
import com.hover.sdk.api.Hover;
import com.hover.stax.channels.UpdateChannelsWorker;
import com.hover.stax.home.MainActivity;
import com.hover.stax.languages.SelectLanguageActivity;
import com.hover.stax.schedules.ScheduleWorker;
import com.hover.stax.security.PinsActivity;
import com.hover.stax.utils.Utils;

public class SplashScreenActivity extends AppCompatActivity {
	final public static String LANGUAGE_CHECK = "Language";

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Amplitude.getInstance().initialize(this, getString(R.string.amp)).enableForegroundTracking(getApplication());
		initHover();
		createNotificationChannel();
		startWorkers();

		if (Utils.getSharedPrefs(ApplicationInstance.getContext()).getInt(LANGUAGE_CHECK, 0) == 1)
			startActivity(new Intent(this, MainActivity.class));
		else
			startActivity(new Intent(this, SelectLanguageActivity.class));
		finish();
	}

	private void initHover() {
		Hover.initialize(this);
		Hover.setBranding("Stax", R.mipmap.fullsize_logo, this);
	}

	private void createNotificationChannel() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			int importance = NotificationManager.IMPORTANCE_DEFAULT;
			NotificationChannel channel = new NotificationChannel("DEFAULT", "User scheduled", importance);
			channel.setDescription("Show notifications to make transfers and requests scheduled by you.");
			NotificationManager notificationManager = getSystemService(NotificationManager.class);
			notificationManager.createNotificationChannel(channel);
		}
	}

	private void startWorkers() {
		WorkManager wm = WorkManager.getInstance(this);
		wm.beginUniqueWork(UpdateChannelsWorker.CHANNELS_WORK_ID, ExistingWorkPolicy.KEEP, UpdateChannelsWorker.makeWork()).enqueue();
		wm.enqueueUniquePeriodicWork(UpdateChannelsWorker.TAG, ExistingPeriodicWorkPolicy.KEEP, UpdateChannelsWorker.makeToil());
		wm.enqueueUniquePeriodicWork(ScheduleWorker.TAG, ExistingPeriodicWorkPolicy.KEEP, ScheduleWorker.makeToil());
	}
}
