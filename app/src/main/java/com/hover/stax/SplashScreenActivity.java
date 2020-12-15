package com.hover.stax;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.WorkManager;

import com.amplitude.api.Amplitude;
import com.hover.sdk.api.Hover;
import com.hover.stax.actions.Action;
import com.hover.stax.channels.UpdateChannelsWorker;
import com.hover.stax.database.Constants;
import com.hover.stax.destruct.SelfDestructActivity;
import com.hover.stax.home.MainActivity;
import com.hover.stax.languages.SelectLanguageActivity;
import com.hover.stax.requests.RequestActivity;
import com.hover.stax.schedules.ScheduleWorker;
import com.hover.stax.security.BiometricChecker;
import com.hover.stax.utils.UIHelper;
import com.hover.stax.utils.Utils;

import static com.hover.stax.database.Constants.AUTH_CHECK;
import static com.hover.stax.database.Constants.LANGUAGE_CHECK;

public class SplashScreenActivity extends AppCompatActivity implements BiometricChecker.AuthListener {

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (SelfDestructActivity.isExpired(this)) {
			startActivity(new Intent(this, SelfDestructActivity.class));
			finish();
			return;
		}

		Amplitude.getInstance().initialize(this, getString(R.string.amp)).enableForegroundTracking(getApplication());
		initHover();
		createNotificationChannel();
		startWorkers();

		if (Utils.getSharedPrefs(this).getInt(AUTH_CHECK, 0) == 1)
			new BiometricChecker(this, this).startAuthentication(null);
		else
			chooseNav();
	}

	private void initHover() {
		Hover.initialize(this);
		Hover.setBranding(getString(R.string.app_name), R.mipmap.stax, this);
	}

	private void createNotificationChannel() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			int importance = NotificationManager.IMPORTANCE_DEFAULT;
			NotificationChannel channel = new NotificationChannel("DEFAULT", getString(R.string.notify_default_title), importance);
			channel.setDescription(getString(R.string.notify_default_channel_descrip));
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

	private void chooseNav() {
		if (getIntent().getAction().equals(Intent.ACTION_VIEW) && getIntent().getData() != null)
			goToRequest(getIntent());
		else if (Utils.getSharedPrefs(this).getInt(LANGUAGE_CHECK, 0) == 0)
			startActivity(new Intent(this, SelectLanguageActivity.class));
		else {
			startActivity(new Intent(this, MainActivity.class));
		}
		finish();
	}

	private void goToRequest(Intent intent) {
		Intent i = new Intent(this, MainActivity.class);
		i.putExtra(Constants.REQUEST_LINK, intent.getData().toString());
		startActivity(i);
	}

	@Override
	public void onAuthError(String error) {
		UIHelper.flashMessage(this, getString(R.string.toast_error_auth));
	}

	@Override
	public void onAuthSuccess(Action act) {
		chooseNav();
	}
}
