package com.hover.stax.channels

import android.content.Context
import androidx.work.*
import com.hover.stax.BuildConfig
import com.hover.stax.R
import com.hover.stax.database.AppDatabase
import org.json.JSONArray
import org.json.JSONObject
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber
import java.io.IOException

class ImportChannelsWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params), KoinComponent {

    private var channelDao: ChannelDao? = null

    private val db: AppDatabase by inject()

    init {
        channelDao = db.channelDao()
    }

    override suspend fun doWork(): Result {
        Timber.i("Starting channel import")

        val hasChannels = channelDao!!.getChannelsAndAccounts().isNotEmpty()

        if (!hasChannels) {
            parseChannelJson()?.let {
                val channelsJson = JSONObject(it)
                val data: JSONArray = channelsJson.getJSONArray("data")
                ChannelUtil.updateChannels(data, applicationContext)

                Timber.i("Channels imported successfully")
                return Result.success()
            } ?: Timber.e("Error importing channels"); return Result.retry()
        } else {
            Timber.i("Has channels, nothing to do here")
            return Result.failure()
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

    companion object {
        fun channelsImportRequest(): OneTimeWorkRequest {
            return OneTimeWorkRequestBuilder<ImportChannelsWorker>().apply {
                setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            }.build()
        }
    }
}