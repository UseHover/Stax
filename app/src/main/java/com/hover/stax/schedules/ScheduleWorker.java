package com.hover.stax.schedules;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.hover.stax.R;
import com.hover.stax.contacts.ContactDao;
import com.hover.stax.contacts.StaxContact;
import com.hover.stax.database.AppDatabase;
import com.hover.stax.requests.RequestActivity;
import com.hover.stax.transfers.TransferActivity;
import com.hover.stax.utils.Constants;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class ScheduleWorker extends Worker {
    public final static String TAG = "ScheduleWorker";

    private final ScheduleDao scheduleDao;
    private final ContactDao contactDao;

    public ScheduleWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
        scheduleDao = AppDatabase.getInstance(context).scheduleDao();
        contactDao = AppDatabase.getInstance(context).contactDao();
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
        for (Schedule s : scheduled) {
            if (s.isScheduledForToday())
                notifyUser(s);
        }
        return Result.success();
    }

    private void notifyUser(Schedule s) {
        List<StaxContact> contacts = contactDao.get(s.recipient_ids.split(","));
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), "DEFAULT")
                .setSmallIcon(R.drawable.ic_stax)
                .setContentTitle(s.title(getApplicationContext()))
                .setContentText(s.notificationMsg(getApplicationContext(), contacts))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(s.notificationMsg(getApplicationContext(), contacts)))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setContentIntent(createIntent(s))
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
        notificationManager.notify(s.id, builder.build());
    }

    private PendingIntent createIntent(Schedule s) {
        Intent intent;
        if (s.type.equals(Constants.REQUEST_TYPE))
            intent = new Intent(getApplicationContext(), RequestActivity.class);
        else
            intent = new Intent(getApplicationContext(), TransferActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.setAction(s.type);
        intent.putExtra(Schedule.SCHEDULE_ID, s.id);
        return PendingIntent.getActivity(getApplicationContext(), Constants.SCHEDULE_REQUEST, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
