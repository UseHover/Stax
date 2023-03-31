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
package com.hover.stax.data.remote.workers

import android.content.Context
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.PeriodicWorkRequest
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.hover.sdk.api.Hover
import com.hover.sdk.transactions.Transaction
import com.hover.stax.R
import com.hover.stax.channels.UpdateChannelsWorker
import com.hover.stax.transactions.StaxTransaction
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber

class UpdateBountyTransactionsWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    companion object {
        const val TAG = "BountyTransactionWorker"
        const val BOUNTY_TRANSACTION_WORK_ID = "BOUNTY_TRANSACTION"

        fun makeToil(): PeriodicWorkRequest {
            return PeriodicWorkRequest.Builder(UpdateChannelsWorker::class.java, 24, TimeUnit.HOURS)
                .setConstraints(netConstraint())
                .build()
        }

        fun makeWork(): OneTimeWorkRequest {
            return OneTimeWorkRequest.Builder(UpdateBountyTransactionsWorker::class.java)
                .setConstraints(netConstraint())
                .build()
        }

        private fun netConstraint(): Constraints {
            return Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
        }
    }

    private val client = OkHttpClient()
    private val transactionDao = AppDatabase.getInstance(context).transactionDao()

    override fun doWork(): Result {
        try {

            val bountyResultJson = downloadBountyResult(getUrl())
            Timber.i("url is ${getUrl()}")
            Timber.i("Transaction returns result $bountyResultJson")

            val succeeded: JSONArray = bountyResultJson.getJSONArray("succeeded")
            val failed = bountyResultJson.getJSONArray("failed")

            CoroutineScope(Dispatchers.IO).launch {
                for (i in 0 until succeeded.length()) {
                    val uuid = succeeded.getString(i)
                    updateTransaction(uuid, true)
                }

                for (i in 0 until failed.length()) {
                    val uuid = failed.getString(i)
                    updateTransaction(uuid, false)
                }
            }

            return Result.success()
        } catch (e: JSONException) {
            Timber.e(e, "Error parsing bounty result data.")
            return Result.failure()
        } catch (e: IOException) {
            Timber.e(e, "Timeout downloading bounty result data, will try again.")

            return Result.retry()
        }
    }

    private suspend fun updateTransaction(uuid: String, succeeded: Boolean) {
        val transaction: StaxTransaction? = transactionDao.getTransactionSuspended(uuid)
        transaction?.let {
            transaction.status = if (succeeded) Transaction.SUCCEEDED else Transaction.FAILED
            transactionDao.update(transaction)
        }
    }

    private fun getUrl(): String {
        val deviceId: String = Hover.getDeviceId(applicationContext)
        return with(applicationContext) { getString(R.string.api_url) + getString(R.string.bounty_transactions_endpoint, deviceId) }
    }

    private fun downloadBountyResult(url: String): JSONObject {
        val request: Request = Request.Builder().url(url).build()
        val response: Response = client.newCall(request).execute()
        return JSONObject(response.body!!.string())
    }
}