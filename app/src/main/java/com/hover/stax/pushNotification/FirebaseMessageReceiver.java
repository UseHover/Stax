package com.hover.stax.pushNotification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.hover.stax.R;
import com.hover.stax.SplashScreenActivity;
import com.hover.stax.utils.Constants;
import com.hover.stax.utils.DateUtils;

import java.util.Date;
import java.util.Map;

public class FirebaseMessageReceiver extends FirebaseMessagingService {
	@Override
	public void onNewToken(@NonNull String s) {
		super.onNewToken(s);
	}

	@Override
	public void
	onMessageReceived(RemoteMessage remoteMessage) {
		Map<String, String> data = remoteMessage.getData();
		String redirect = data.get("redirect");
		if (remoteMessage.getNotification() != null) {
			showNotification(remoteMessage.getNotification().getTitle(), remoteMessage.getNotification().getBody(), redirect);
		}
	}

	private void showNotification(String title, String message, String redirect) {
		String channel_id = String.valueOf(DateUtils.now());

		Intent intent = new Intent(this, SplashScreenActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.putExtra(Constants.FROM_FCM, title);
		intent.putExtra(Constants.FRAGMENT_DIRECT, redirect);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

		NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), channel_id)
													 .setSmallIcon(R.drawable.ic_stax)
													 .setAutoCancel(false)
													 .setBadgeIconType(NotificationCompat.BADGE_ICON_LARGE)
													 .setPriority(2)
													 .setTicker(title)
													 .setOngoing(false)
													 .setUsesChronometer(false)
													 .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000})
													 .setOnlyAlertOnce(false)
													 .setContentTitle(title)
													 .setContentText(message)
													 .setContentIntent(pendingIntent);

		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			NotificationChannel notificationChannel = new NotificationChannel(channel_id, getString(R.string.app_name), NotificationManager.IMPORTANCE_HIGH);
			if (notificationManager != null)
				notificationManager.createNotificationChannel(notificationChannel);
		}
		if (notificationManager != null) {
			int randId = (int) ((new Date().getTime() / 1000L) % Integer.MAX_VALUE);
			notificationManager.notify(randId, builder.build());
		}
	}
}
