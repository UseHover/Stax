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

class UpdateChannelsWorker(context: Context, params: WorkerParameters) : Worker(context, params), KoinComponent {

    private val client = OkHttpClient()
    private var channelDao: ChannelDao? = null

    private val db: AppDatabase by inject()

    init {
        channelDao = db.channelDao()
    }

    override fun doWork(): Result {
        Timber.v("Downloading channels")

        importChannels()

        try {
            val channelsJson = downloadChannels(url)
            channelsJson?.let {
                val data: JSONArray = it.getJSONArray("data")
                updateChannels(data)

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

    private fun updateChannels(data: JSONArray){
        for (j in 0 until data.length()) {
            var channel = channelDao!!.getChannel(data.getJSONObject(j).getJSONObject("attributes").getInt("id"))
            if (channel == null) {
                channel = Channel(data.getJSONObject(j).getJSONObject("attributes"), applicationContext.getString(R.string.root_url))
                channelDao!!.insert(channel)
            } else channelDao!!.update(channel.update(data.getJSONObject(j).getJSONObject("attributes"), applicationContext.getString(R.string.root_url)))
        }
    }

    private val url get() = applicationContext.getString(R.string.api_url).plus(applicationContext.getString(R.string.channels_endpoint))

    @Throws(IOException::class, JSONException::class)
    private fun downloadChannels(url: String): JSONObject? {
        val request: Request = Request.Builder().url(url).build()
        val response: Response = client.newCall(request).execute()
        return response.body?.let { JSONObject(it.string()) }
    }

    fun importChannels() = CoroutineScope(Dispatchers.IO).launch {
        val hasChannels = channelDao!!.getChannelsAndAccounts().isNotEmpty()

        if (!hasChannels) {
            parseChannelJson()?.let {
                val channelsJson = JSONObject(it)
                val data: JSONArray = channelsJson.getJSONArray("data")
                updateChannels(data)

                Timber.i("Channels imported successfully")
            } ?: Timber.e("Error importing channels")
        } else {
            Timber.i("Has channels, nothing to do here")
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