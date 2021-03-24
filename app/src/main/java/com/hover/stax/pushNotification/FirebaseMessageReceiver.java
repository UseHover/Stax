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

public class FirebaseMessageReceiver extends FirebaseMessagingService {
	@Override
	public void onNewToken(@NonNull String s) {
		super.onNewToken(s);
	}

	@Override
	public void
	onMessageReceived(RemoteMessage remoteMessage) {
		if (remoteMessage.getNotification() != null) {
			showNotification(remoteMessage.getNotification().getTitle(), remoteMessage.getNotification().getBody());
		}
	}

	private RemoteViews getCustomDesign(String title, String message) {
		RemoteViews remoteViews = new RemoteViews(getApplicationContext().getPackageName(), R.layout.notification);
		remoteViews.setTextViewText(R.id.title, title);
		remoteViews.setTextViewText(R.id.message, message);
		remoteViews.setImageViewResource(R.id.icon, R.drawable.ic_stax);
		return remoteViews;
	}

	private void showNotification(String title, String message) {
		String channel_id = String.valueOf(DateUtils.now());

		Intent intent = new Intent(this, SplashScreenActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.putExtra(Constants.FROM_FCM, title);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

		NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), channel_id)
													 .setSmallIcon(R.drawable.ic_stax)
													 .setAutoCancel(false)
													 .setBadgeIconType(NotificationCompat.BADGE_ICON_LARGE)
													 .setPriority(2)
													 .setTicker(title)
													 .setOngoing(false)
													 .setUsesChronometer(true)
													 .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000})
													 .setOnlyAlertOnce(false)
													 .setContentIntent(pendingIntent);

		builder = builder.setContent(getCustomDesign(title, message));

		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			NotificationChannel notificationChannel = new NotificationChannel(channel_id, getString(R.string.app_name), NotificationManager.IMPORTANCE_HIGH);
			if (notificationManager != null)
				notificationManager.createNotificationChannel(notificationChannel);
		}
		if (notificationManager != null) notificationManager.notify(0, builder.build());
	}
}
