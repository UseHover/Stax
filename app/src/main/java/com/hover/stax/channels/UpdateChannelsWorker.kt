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

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkerParameters
import com.hover.stax.R
import com.hover.stax.database.channel.repository.ChannelRepository
import com.hover.stax.data.channel.ChannelRepositoryImpl
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

class UpdateChannelsWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params), KoinComponent {

    private val client = OkHttpClient()
    private val channelRepository: ChannelRepository by inject()

    override suspend fun doWork(): Result {
        return try {
            val channelsJson = downloadChannels(url)
            if (channelsJson != null) {
                val data: JSONArray = channelsJson.getJSONArray("data")
                com.hover.stax.data.channel.ChannelRepositoryImpl.ChannelUtil.load(
                    data,
                    channelRepository,
                    applicationContext
                )
                Timber.v("Successfully Updated channels")
                Result.success()
            } else {
                Timber.d("Failed to update channels")
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

    private val url
        get() = applicationContext.getString(R.string.maathai_api_url)
            .plus(applicationContext.getString(R.string.channels_endpoint))

    private fun downloadChannels(url: String): JSONObject? {
        val request: Request = Request.Builder().url(url).build()
        val response: Response = client.newCall(request).execute()
        return response.body?.let { JSONObject(it.string()) }
    }

    companion object {
        const val CHANNELS_WORK_ID = "CHANNELS"
        const val TAG = "UpdateChannelsWorker"

        private val netConstraint =
            Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()

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