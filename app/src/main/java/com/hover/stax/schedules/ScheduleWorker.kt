package com.hover.stax.schedules

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.OneTimeWorkRequest
import androidx.work.PeriodicWorkRequest
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.hover.stax.R
import com.hover.stax.contacts.ContactDao
import com.hover.stax.database.AppDatabase
import com.hover.stax.home.MainActivity
import com.hover.stax.utils.Constants
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.concurrent.TimeUnit

class ScheduleWorker(context: Context, params: WorkerParameters) : Worker(context, params), KoinComponent {

    private val appDb: AppDatabase by inject()

    private var scheduleDao: ScheduleDao = appDb.scheduleDao()
    private var contactDao: ContactDao = appDb.contactDao()

    override fun doWork(): Result {
        val scheduled = scheduleDao.future
        scheduled.filter { it.isScheduledForToday }.forEach { notifyUser(it) }
        return Result.success()
    }

    private fun notifyUser(s: Schedule) {
        val contacts = contactDao[s.recipient_ids.split(",").toTypedArray()]
        val builder = NotificationCompat.Builder(applicationContext, "DEFAULT")
                .setSmallIcon(R.drawable.ic_stax)
                .setContentTitle(s.title(applicationContext))
                .setContentText(s.notificationMsg(applicationContext, contacts))
                .setStyle(NotificationCompat.BigTextStyle().bigText(s.notificationMsg(applicationContext, contacts)))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setOnlyAlertOnce(true)
                .setContentIntent(createIntent(s))
                .setAutoCancel(true)

        val notificationManager = NotificationManagerCompat.from(applicationContext)
        notificationManager.notify(s.id, builder.build())
    }

    private fun createIntent(s: Schedule): PendingIntent {
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            action = s.type
            putExtra(Schedule.SCHEDULE_ID, s.id)
            putExtra(Constants.REQUEST_TYPE, s.type == Constants.REQUEST_TYPE)
        }

        return PendingIntent.getActivity(applicationContext, Constants.SCHEDULE_REQUEST, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    companion object {

        fun makeToil(): PeriodicWorkRequest = PeriodicWorkRequest.Builder(ScheduleWorker::class.java, 24, TimeUnit.HOURS).build()

        fun makeWork(): OneTimeWorkRequest = OneTimeWorkRequest.Builder(ScheduleWorker::class.java).build()
    }
}