/*
 * Copyright 2022 Stax
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hover.stax.channels

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkerParameters
import com.hover.stax.R
import com.hover.stax.database.AppDatabase
import com.hover.stax.storage.channel.repository.ChannelRepository
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber

class ImportChannelsWorker(
    val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params), KoinComponent {

    private val channelRepository: ChannelRepository by inject()

    override suspend fun getForegroundInfo(): ForegroundInfo {
        return ForegroundInfo(NOTIFICATION_ID, createNotification())
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        Timber.i("Attempting to import channels from json file")
        if (channelRepository.allDataCount == 0 || channelRepository.publishedTelecomDataCount == 0) {
            initNotification()

            parseChannelJson()?.let {
                val channelsJson = JSONObject(it)
                val data: JSONArray = channelsJson.getJSONArray("data")
                ChannelUtil.load(data, channelRepository, applicationContext)

                Timber.i("Channels imported successfully")
                Result.success()
            } ?: Timber.e("Error importing channels"); Result.retry()
        } else {
            Timber.i("DB is either on par or has more updates compared to json file")
            Result.failure()
        }
    }

    private fun parseChannelJson(): String? {
        var channelsString: String? = null
        try {
            val inputStream = applicationContext.assets.open("channels.json")
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
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentTitle(context.getString(R.string.importing_channels))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)

        return builder.build()
    }

    private suspend fun initNotification() = try {
        setForeground(getForegroundInfo())
    } catch (e: IllegalArgumentException) {
        Timber.e(e)
    }

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
        private const val CHANNEL_ID = "ChannelsImport" // TODO update this after the merge with financial tips notifications // branch

        fun channelsImportRequest(): OneTimeWorkRequest {
            return OneTimeWorkRequestBuilder<ImportChannelsWorker>()
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .build()
        }
    }
}