package com.hover.stax.login

import android.content.Context
import com.hover.sdk.api.Hover
import com.hover.stax.R
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response

import org.json.JSONObject
import timber.log.Timber

class LoginNetworking(private val context: Context) {

    private val client = OkHttpClient()

    fun uploadUserToStax(data: JSONObject): Response {
        return post(context.getString(R.string.api_url).plus(context.getString(R.string.users_endpoint)), wrapJson(data))
    }

    fun updateUser(email: String, data: JSONObject): Response {
        return put(context.getString(R.string.api_url) + context.getString(R.string.users_endpoint) + "/" + email, wrapJson(data))
    }

    private fun post(url: String, json: JSONObject): Response {
        val request: Request = getUploadRequest(url)
                .post(createBody(json))
                .build()
        return client.newCall(request).execute()
    }

    private fun put(url: String, json: JSONObject): Response {
        val request: Request = getUploadRequest(url)
                .put(createBody(json))
                .build()
        return client.newCall(request).execute()
    }

    private fun getUploadRequest(url: String): Request.Builder {
        return Request.Builder().url(url).addHeader("Authorization", "Token token=" + Hover.getApiKey(context))
    }

    private fun wrapJson(deets: JSONObject): JSONObject {
        val root = JSONObject()
        root.put("stax_user", deets)
        Timber.d("uploading %s", root)
        return root
    }

    private fun createBody(json: JSONObject): RequestBody {
        return json.toString().toRequestBody("application/json".toMediaTypeOrNull())
    }
}