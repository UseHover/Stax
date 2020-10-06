package com.hover.stax.schedules;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.hover.stax.R;
import com.hover.stax.actions.Action;
import com.hover.stax.database.AppDatabase;
import com.hover.stax.transfers.TransferActivity;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class ScheduleWorker extends Worker {
	public final static String TAG = "ScheduleWorker";

	private final ScheduleDao scheduleDao;

	public ScheduleWorker(@NonNull Context context, @NonNull WorkerParameters params) {
		super(context, params);
		scheduleDao = AppDatabase.getInstance(context).scheduleDao();
	}

	public static PeriodicWorkRequest makeToil() {
		return new PeriodicWorkRequest.Builder(ScheduleWorker.class, 24, TimeUnit.HOURS).build();
	}

	public static OneTimeWorkRequest makeWork() { // Just for testing
		return new OneTimeWorkRequest.Builder(ScheduleWorker.class).build();
	}

	@Override
	public Worker.Result doWork() {
		List<Schedule> scheduled = scheduleDao.getFuture();
		for (Schedule schedule: scheduled) {
			if (schedule.isScheduledForToday())
				notifyUser(schedule);
		}
		return Result.success();
	}

	private void notifyUser(Schedule s) {
		NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), "DEFAULT")
			.setSmallIcon(R.drawable.ic_stax)
			.setContentTitle(s.title(getApplicationContext()))
			.setContentText(s.notificationMsg(getApplicationContext()))
			.setStyle(new NotificationCompat.BigTextStyle().bigText(s.notificationMsg(getApplicationContext())))
			.setPriority(NotificationCompat.PRIORITY_DEFAULT)
			.setCategory(NotificationCompat.CATEGORY_REMINDER)
			.setContentIntent(createTransferIntent(s))
			.setAutoCancel(true);

		NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
		notificationManager.notify(s.id, builder.build());
	}

	private PendingIntent createTransferIntent(Schedule s) {
		Intent intent = new Intent(getApplicationContext(), TransferActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		intent.setAction(s.type);
		intent.putExtra(Schedule.SCHEDULE_ID, s.id);
		return PendingIntent.getActivity(getApplicationContext(), TransferActivity.SCHEDULED_REQUEST, intent, PendingIntent.FLAG_UPDATE_CURRENT);
	}
}
