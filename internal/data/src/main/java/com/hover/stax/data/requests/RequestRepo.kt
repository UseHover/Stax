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
package com.hover.stax.data.requests

import android.content.Context
import androidx.lifecycle.LiveData
import com.hover.stax.R
import com.hover.stax.database.dao.RequestDao
import com.hover.stax.database.models.Request
import com.hover.stax.utils.AnalyticsUtil
import timber.log.Timber
import java.security.NoSuchAlgorithmException
import javax.inject.Inject

interface RequestRepository {

    val liveRequests: LiveData<List<Request>>

    fun getLiveRequests(channelId: Int): LiveData<List<Request>>

    val requests: List<Request>

    fun getRequest(id: Int): Request?

    fun insert(request: Request?)

    fun update(request: Request?)

    fun delete(request: Request?)
}

class RequestRepo @Inject constructor(
    private val requestDao: RequestDao
) : RequestRepository {

    override val liveRequests: LiveData<List<Request>>
        get() = requestDao.liveUnmatched

    override fun getLiveRequests(channelId: Int): LiveData<List<Request>> {
        return requestDao.getLiveUnmatchedByChannel(channelId)
    }

    override val requests: List<Request>
        get() = requestDao.unmatched

    override fun getRequest(id: Int): Request? {
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

    override fun insert(request: Request?) {
        requestDao.insertRequest(request)
    }

    override fun update(request: Request?) {
        requestDao.updateRequest(request)
    }

    override fun delete(request: Request?) {
        requestDao.deleteRequest(request)
    }

    companion object {
        private val TAG = RequestRepo::class.java.simpleName
    }
}