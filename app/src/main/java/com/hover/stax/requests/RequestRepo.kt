package com.hover.stax.requests

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.hover.sdk.database.HoverRoomDatabase
import com.hover.stax.R
import com.hover.stax.database.AppDatabase
import com.hover.stax.utils.AnalyticsUtil
import com.hover.stax.utils.paymentLinkCryptography.Encryption
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

    fun getRequest(id: Int): Request {
        return requestDao[id]
    }

    fun decrypt(encrypted: String, c: Context): MutableLiveData<Request?> {
        val liveRequest: MutableLiveData<Request?> = MutableLiveData()
        liveRequest.value = null
        val removedBaseUrlString = encrypted.replace(c.getString(R.string.payment_root_url, ""), "")

        //Only old stax versions contains ( in the link
        if (removedBaseUrlString.contains("(")) decryptRequestForOldVersions(removedBaseUrlString, liveRequest)
        else liveRequest.postValue(decryptRequest(removedBaseUrlString, c))

        return liveRequest
    }

    private fun decryptRequest(param: String, c: Context): Request {
        return Request(Request.decryptBijective(param, c))
    }

    private fun decryptRequestForOldVersions(param: String, ld: MutableLiveData<Request?>) {
        var params = param
        try {
            val e = Request.getEncryptionSettings().build()
            if (Request.isShortLink(params)) {
                params = Shortlink(params).expand()
            }
            e.decryptAsync(params.replace("[(]".toRegex(), "+"), object : Encryption.Callback {
                override fun onSuccess(result: String) {
                    return ld.postValue(Request(result))
                }

                override fun onError(exception: Exception) {
                    AnalyticsUtil.logErrorAndReportToFirebase(TAG, "failed link decryption", exception)
                }
            })
        } catch (e: NoSuchAlgorithmException) {
            AnalyticsUtil.logErrorAndReportToFirebase(TAG, "decryption failure", e)
        }
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