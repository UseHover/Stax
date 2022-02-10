package com.hover.stax.channels

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.hover.stax.BuildConfig
import com.hover.stax.R
import com.hover.stax.database.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber
import java.io.IOException

class ImportChannelsWorker(val context: Context, params: WorkerParameters) : CoroutineWorker(context, params), KoinComponent {

    private var channelDao: ChannelDao? = null

    private val db: AppDatabase by inject()

    init {
        channelDao = db.channelDao()
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        return ForegroundInfo(NOTIFICATION_ID, createNotification())
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        if (channelDao!!.channels.isEmpty()) {
            initNotification()

            parseChannelJson()?.let {
                val channelsJson = JSONObject(it)
                val data: JSONArray = channelsJson.getJSONArray("data")
                ChannelUtil.updateChannels(data, applicationContext)

                Timber.i("Channels imported successfully")
                Result.success()
            } ?: Timber.e("Error importing channels"); Result.retry()
        } else {
            Result.failure()
        }
    }

    private fun parseChannelJson(): String? {
        var channelsString: String? = null

        val fileToUse = if (BuildConfig.DEBUG)
            applicationContext.getString(R.string.channels_json_staging)
        else
            applicationContext.getString(R.string.channels_json_prod)

        try {
            val inputStream = applicationContext.assets.open(fileToUse)
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            channelsString = String(buffer, Charsets.UTF_8)
        } catch (e: IOException) {
            Timber.e(e)
        }

        return channelsString
    }

    private fun createNotification(): Notification {
        createNotificationChannel()

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stax)
            .setContentTitle(context.getString(R.string.importing_channels))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        return builder.build()
    }

    private suspend fun initNotification() = try {
        setForeground(getForegroundInfo())
    } catch (e: IllegalArgumentException) {
        Timber.e(e)
    } /*catch (f: ForegroundServiceStartNotAllowedException) {
        Timber.e(f)
    }*/

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, context.getString(R.string.app_name), importance)
            val notificationManager: NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        private const val NOTIFICATION_ID = 981
        private const val CHANNEL_ID = "ChannelsImport" //TODO update this after the merge with financial tips notifications // branch

        fun channelsImportRequest(): OneTimeWorkRequest {
            return OneTimeWorkRequestBuilder<ImportChannelsWorker>()
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .build()
        }
    }
}