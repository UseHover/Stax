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

class RequestRepo(db: AppDatabase, sdkDb: HoverRoomDatabase) {
    private val decryptedRequest: MutableLiveData<Request> = MutableLiveData()

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

    fun decrypt(encrypted: String, c: Context): LiveData<Request> {
        decryptedRequest.value = null
        val removedBaseUrlString = encrypted.replace(c.getString(R.string.payment_root_url, ""), "")

        //Only old stax versions contains ( in the link
        if (removedBaseUrlString.contains("(")) decryptRequestForOldVersions(removedBaseUrlString)
        else decryptRequest(removedBaseUrlString, c)
        return decryptedRequest
    }

    private fun decryptRequest(param: String, c: Context) {
        decryptedRequest.postValue(Request(Request.decryptBijective(param, c)))
    }

    private fun decryptRequestForOldVersions(param: String) {
        var params = param
        try {
            val e = Request.getEncryptionSettings().build()
            if (Request.isShortLink(params)) {
                params = Shortlink(params).expand()
            }
            e.decryptAsync(params.replace("[(]".toRegex(), "+"), object : Encryption.Callback {
                override fun onSuccess(result: String) {
                    decryptedRequest.postValue(Request(result))
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