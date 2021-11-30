package com.hover.stax.channels

import android.content.Context
import androidx.lifecycle.viewModelScope
import androidx.work.*
import androidx.work.CoroutineWorker
import com.hover.stax.BuildConfig
import com.hover.stax.R
import com.hover.stax.database.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber
import java.io.IOException
import java.util.concurrent.TimeUnit

class UpdateChannelsWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    private val client = OkHttpClient()

    override fun doWork(): Result {
        Timber.v("Downloading channels")

        try {
            val channelsJson = downloadChannels(url)
            channelsJson?.let {
                val data: JSONArray = it.getJSONArray("data")
                ChannelUtil.updateChannels(data, applicationContext)

                Timber.i("Successfully downloaded and saved channels.")
                return Result.success()
            }

            Timber.e("Error parsing channel data")
            return Result.failure()
        } catch (e: JSONException) {
            Timber.e(e, "Error parsing channel data")
            return Result.failure()
        } catch(e: NullPointerException) {
            Timber.e(e, "Error parsing channel data")
            return Result.failure()
        } catch(e: IOException){
            Timber.e(e, "Timeout downloading channel data, will try again.")
            return Result.retry()
        }
    }

    private val url get() = applicationContext.getString(R.string.api_url).plus(applicationContext.getString(R.string.channels_endpoint))

    @Throws(IOException::class, JSONException::class)
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
            return PeriodicWorkRequest.Builder(UpdateChannelsWorker::class.java, 24, TimeUnit.HOURS)
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