package com.hover.stax.requests

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.hover.sdk.database.HoverRoomDatabase
import com.hover.stax.R
import com.hover.stax.database.AppDatabase
import com.hover.stax.utils.AnalyticsUtil
import com.hover.stax.utils.paymentLinkCryptography.Encryption
import timber.log.Timber
import java.security.NoSuchAlgorithmException

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

        //Only old stax versions contains ( in the link
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
        AppDatabase.databaseWriteExecutor.execute { requestDao.insert(request) }
    }

    fun update(request: Request?) {
        AppDatabase.databaseWriteExecutor.execute { requestDao.update(request) }
    }

    fun delete(request: Request?) {
        AppDatabase.databaseWriteExecutor.execute { requestDao.delete(request) }
    }

    companion object {
        private val TAG = RequestRepo::class.java.simpleName
    }
}