package com.hover.stax;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.WorkManager;

import com.amplitude.api.Amplitude;
import com.hover.sdk.api.Hover;
import com.hover.stax.actions.Action;
import com.hover.stax.channels.UpdateChannelsWorker;
import com.hover.stax.destruct.SelfDestructActivity;
import com.hover.stax.utils.Constants;
import com.hover.stax.home.MainActivity;
import com.hover.stax.schedules.ScheduleWorker;
import com.hover.stax.settings.BiometricChecker;
import com.hover.stax.utils.blur.StaxBlur;
import com.hover.stax.utils.UIHelper;
import com.hover.stax.utils.Utils;

import static com.hover.stax.utils.Constants.AUTH_CHECK;

public class SplashScreenActivity extends AppCompatActivity implements BiometricChecker.AuthListener {

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if(shouldSelfDestructWhenAppVersionExpires(false)) {return;}
		startBackgroundProcesses();
		startSplashForegroundSequence();

	}

	private void startBackgroundProcesses() {
		initAmplitude();
		initHover();
		createNotificationChannel();
		startWorkers();
	}
	private void startSplashForegroundSequence() {
		initSplashAnimation();
		authenticateBiometricOrNavigateScreenAfter110sec();
	}

	private void initSplashAnimation() {
		setContentView(R.layout.splash_screen_layout);
		blurBackgroundAfter60sec();
		fadeInSplashContentAfter90sec();
	}
	private void blurBackgroundAfter60sec() {
		Bitmap bg = BitmapFactory.decodeResource(getResources(), R.drawable.stax_splash);
		Bitmap bitmap = new StaxBlur(this,16, 1).transform(bg);
		ImageView bgView = findViewById(R.id.splash_image_blur);
		new Handler(Looper.getMainLooper()).postDelayed(() -> {
			bgView.setImageBitmap(bitmap);
			bgView.setVisibility(View.VISIBLE);
			bgView.setAnimation(UIHelper.loadFadeIn(this));
		}, 1000);
	}

	private void fadeInSplashContentAfter90sec() {
		TextView tv = findViewById(R.id.splash_content);
		new Handler(Looper.getMainLooper()).postDelayed(() -> {
			tv.setVisibility(View.VISIBLE);
			tv.setAnimation(UIHelper.loadFadeIn(this));
		}, 1200);
	}

	private void authenticateBiometricOrNavigateScreenAfter110sec() {
		new Handler().postDelayed(() -> {
			if (Utils.getSharedPrefs(this).getInt(AUTH_CHECK, 0) == 1) new BiometricChecker(this, this).startAuthentication(null);
			else chooseNav();
		}, 1800);

	}

	private void initAmplitude() {
		Amplitude.getInstance().initialize(this, getString(R.string.amp)).enableForegroundTracking(getApplication());
	}

	private void initHover() {
		Hover.initialize(this);
		Hover.setBranding(getString(R.string.app_name), R.mipmap.stax, this);
	}

	private Boolean shouldSelfDestructWhenAppVersionExpires(Boolean value) {
		if(value && SelfDestructActivity.isExpired(this)){
			startActivity(new Intent(this, SelfDestructActivity.class));
			finish();
			return true;
		} else return false;
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
		if (getIntent().getAction() != null && getIntent().getAction().equals(Intent.ACTION_VIEW) && getIntent().getData() != null)
			goToFulfillRequest(getIntent());
		else startActivity(new Intent(this, MainActivity.class));

		finish();
	}

	private void goToFulfillRequest(Intent intent) {
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
