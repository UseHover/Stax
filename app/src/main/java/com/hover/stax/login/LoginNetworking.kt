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

    fun uploadUserToStax(email: String, username: String?, optedIn: Boolean, token: String?): Response {
        return post(context.getString(R.string.api_url) + context.getString(R.string.users_endpoint), getUserJson(email, username, optedIn, token))
    }

    fun uploadReferee(email: String, referralCode: String, name: String, phone: String, token: String?): Response {
        return put(context.getString(R.string.api_url) + context.getString(R.string.users_endpoint) + "/" + email, getReferralJson(email, referralCode, name, phone, token))
    }

    private fun getUserJson(email: String, username: String?, optedIn: Boolean, token: String?): JSONObject {
        val userJson = JSONObject()
            .apply {
                put("email", email)
                put("username", username)
                put("device_id", Hover.getDeviceId(context))
                put("is_mapper", true)
                put("marketing_opted_in", optedIn)
                put("token", token)
            }

        return wrapJson(userJson)
    }

    private fun getReferralJson(email: String, referralCode: String, name: String, phone: String, token: String?): JSONObject {
        val userJson = JSONObject()
            .apply {
                put("email", email)
                put("referee_id", referralCode)
                put("name", name)
                put("phone", phone)
                put("token", token)
            }
        return wrapJson(userJson)
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