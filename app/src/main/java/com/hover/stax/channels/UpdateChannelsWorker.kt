package com.hover.stax.channels

import android.content.Context
import androidx.work.*
import com.hover.stax.R
import com.hover.stax.database.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber
import java.io.IOException
import java.util.concurrent.TimeUnit

class UpdateChannelsWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    private val client = OkHttpClient()
    private val channelDao = AppDatabase.getInstance(context).channelDao()

    override fun doWork(): Result {
        Timber.e("Updating channels")
        return try {
                val channelsJson = downloadChannels(url)
                if (channelsJson != null) {
                    val data: JSONArray = channelsJson.getJSONArray("data")
                    Channel.load(data, channelDao, applicationContext)
                    Timber.e("Successfully Updated channels")
                    Result.success()
                } else {
                    Timber.e("Failed to update channels")
                    Result.failure()
                }
            } catch (e: JSONException) {
                Result.failure()
            } catch (e: NullPointerException) {
                Result.failure()
            } catch (e: IOException) {
                Result.retry()
            }
    }

    private val url get() = applicationContext.getString(R.string.maathai_api_url).plus(applicationContext.getString(R.string.channels_endpoint))

    private fun downloadChannels(url: String): JSONObject? {
        val request: Request = Request.Builder().url(url).build()
        val response: Response = client.newCall(request).execute()
        return response.body?.let { JSONObject(it.string()) }
    }

    companion object {
        const val CHANNELS_WORK_ID = "CHANNELS"
        const val TAG = "UpdateChannelsWorker"

        private val netConstraint = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()

        fun makeToil(): PeriodicWorkRequest {
            return PeriodicWorkRequest.Builder(UpdateChannelsWorker::class.java, 7, TimeUnit.DAYS)
                    .setConstraints(netConstraint)
                    .build()
        }

        fun makeWork(): OneTimeWorkRequest {
            return OneTimeWorkRequest.Builder(UpdateChannelsWorker::class.java)
                    .setConstraints(netConstraint)
                    .build()
        }
    }
}