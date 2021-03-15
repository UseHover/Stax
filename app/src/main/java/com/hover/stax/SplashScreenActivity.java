package com.hover.stax;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.WorkManager;

import com.amplitude.api.Amplitude;
import com.hover.sdk.actions.HoverAction;
import com.hover.sdk.api.Hover;
import com.hover.sdk.database.HoverRoomDatabase;
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
	private final static String TAG = "SplashScreenActivity";

	private final static int BLUR_DELAY = 1000, LOGO_DELAY = 1200, NAV_DELAY = 1800,
		SPLASH_ICON_WIDTH = 177, SPLASH_ICON_HEIGHT = 57;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		startSplashForegroundSequence();
		startBackgroundProcesses();
	}

	private void startSplashForegroundSequence() {
		initSplashAnimation();
		authenticateBiometricOrNavigateScreenAfter110sec();
	}

	private void startBackgroundProcesses() {
		initAmplitude();
		initHover();
		createNotificationChannel();
		startWorkers();
	}

	private void initSplashAnimation() {
		setContentView(R.layout.splash_screen_layout);
		blurBackground();
		fadeInLogo();
	}

	private void blurBackground() {
		new Handler(Looper.getMainLooper()).postDelayed(() -> {
			Bitmap bg = BitmapFactory.decodeResource(getResources(), R.drawable.splash_background);
			Bitmap bitmap = new StaxBlur(this, 16, 1).transform(bg);
			ImageView bgView = findViewById(R.id.splash_image_blur);
			if (bgView != null) {
				bgView.setImageBitmap(bitmap);
				bgView.setVisibility(View.VISIBLE);
				bgView.setAnimation(loadFadeIn(this));
			}
		}, BLUR_DELAY);
	}

	private void fadeInLogo() {
		TextView tv = findViewById(R.id.splash_content);
		setSplashContentTopDrawable(tv);

		new Handler(Looper.getMainLooper()).postDelayed(() -> {
			tv.setVisibility(View.VISIBLE);
			tv.setAnimation(loadFadeIn(this));
		}, LOGO_DELAY);
	}

	private Animation loadFadeIn(Context context) {
		return AnimationUtils.loadAnimation(context, android.R.anim.fade_in);
	}

	private void setSplashContentTopDrawable(TextView tv) {
		Drawable dr = ResourcesCompat.getDrawable (getResources(), R.mipmap.stax, null);
		assert dr != null;
		Bitmap bitmap = ((BitmapDrawable) dr).getBitmap();
		Drawable d = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, SPLASH_ICON_WIDTH, SPLASH_ICON_HEIGHT, true));
		tv.setCompoundDrawablesWithIntrinsicBounds(null,d,null,null);
	}

	private void authenticateBiometricOrNavigateScreenAfter110sec() {
		new Handler().postDelayed(() -> {
			if (Utils.getSharedPrefs(this).getInt(AUTH_CHECK, 0) == 1) new BiometricChecker(this, this).startAuthentication(null);
			else navigateMainActivityOrRequestActivity(getIntent());
		}, NAV_DELAY);

	}

	private void initAmplitude() {
		Amplitude.getInstance().initialize(this, getString(R.string.amp)).enableForegroundTracking(getApplication());
	}

	private void initHover() {
		HoverRoomDatabase.getInstance(this);
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
		startChannelWorker(wm);
		startScheduleWorker(wm);
	}
	private void startChannelWorker(WorkManager wm) {
		wm.beginUniqueWork(UpdateChannelsWorker.CHANNELS_WORK_ID, ExistingWorkPolicy.KEEP, UpdateChannelsWorker.makeWork()).enqueue();
		wm.enqueueUniquePeriodicWork(UpdateChannelsWorker.TAG, ExistingPeriodicWorkPolicy.KEEP, UpdateChannelsWorker.makeToil());
	}
	private void startScheduleWorker(WorkManager wm) {
		wm.enqueueUniquePeriodicWork(ScheduleWorker.TAG, ExistingPeriodicWorkPolicy.KEEP, ScheduleWorker.makeToil());
	}

	private void navigateMainActivityOrRequestActivity(Intent intent) {
		if (intent.getAction() != null && intent.getAction().equals(Intent.ACTION_VIEW) && intent.getData() != null)
			goToFulfillRequest(intent);
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
	public void onAuthSuccess(HoverAction act) {
		navigateMainActivityOrRequestActivity(getIntent());
	}
}
