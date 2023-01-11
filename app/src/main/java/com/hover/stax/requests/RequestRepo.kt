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
package com.hover.stax.requests

import android.content.Context
import androidx.lifecycle.LiveData
import com.hover.stax.R
import com.hover.stax.database.AppDatabase
import com.hover.stax.utils.AnalyticsUtil
import java.security.NoSuchAlgorithmException
import timber.log.Timber

class RequestRepo(db: AppDatabase) {
    private val requestDao: RequestDao = db.requestDao()

    val liveRequests: LiveData<List<Request>>
        get() = requestDao.liveUnmatched

    fun getLiveRequests(channelId: Int): LiveData<List<Request>> {
        return requestDao.getLiveUnmatchedByChannel(channelId)
    }

    val requests: List<Request>
        get() = requestDao.unmatched

    fun getRequest(id: Int): Request? {
        return requestDao[id]
    }

    fun decrypt(encrypted: String, c: Context): Request? {
        Timber.v("decrypting link")
        val removedBaseUrlString = encrypted.replace(c.getString(R.string.payment_root_url, ""), "")

        // Only old stax versions contains ( in the link
        return if (removedBaseUrlString.contains("("))
            decryptRequestForOldVersions(removedBaseUrlString)
        else decryptRequest(removedBaseUrlString, c)
    }

    private fun decryptRequest(param: String, c: Context): Request {
        return Request(Request.decryptBijective(param, c))
    }

    private fun decryptRequestForOldVersions(param: String): Request? {
        var params = param
        try {
            val e = Request.encryptionSettings.build()
            if (Request.isShortLink(params)) {
                params = Shortlink(params).expand()
            }
            return Request(e.decrypt(params.replace("[(]".toRegex(), "+")))
        } catch (e: NoSuchAlgorithmException) {
            AnalyticsUtil.logErrorAndReportToFirebase(TAG, "decryption failure", e)
        }
        return null
    }

    fun insert(request: Request?) {
        AppDatabase.databaseWriteExecutor.execute { requestDao.insertRequest(request) }
    }

    fun update(request: Request?) {
        AppDatabase.databaseWriteExecutor.execute { requestDao.updateRequest(request) }
    }

    fun delete(request: Request?) {
        AppDatabase.databaseWriteExecutor.execute { requestDao.deleteRequest(request) }
    }

    companion object {
        private val TAG = RequestRepo::class.java.simpleName
    }
}