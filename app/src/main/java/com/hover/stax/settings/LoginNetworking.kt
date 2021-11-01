package com.hover.stax.settings

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

    fun uploadUserToStax(email: String, optedIn: Boolean): Response {
        return upload(context.getString(R.string.api_url) + context.getString(R.string.users_endpoint), getUserJson(email, optedIn))
    }

    fun uploadReferee(email: String, referralCode: String): Response {
        return upload(context.getString(R.string.api_url) + context.getString(R.string.referee_endpoint), getReferralJson(email, referralCode))
    }

    private fun getUserJson(email: String, optedIn: Boolean): JSONObject {
        val userJson = JSONObject()
        userJson.put("email", email)
        userJson.put("device_id", Hover.getDeviceId(context))
        userJson.put("is_mapper", true)
        userJson.put("marketing_opted_in", optedIn)
        return wrapJson(userJson)
    }

    private fun getReferralJson(email: String, referralCode: String): JSONObject {
        val userJson = JSONObject()
        userJson.put("email", email)
        userJson.put("referee_id", referralCode)
        return wrapJson(userJson)
    }

    private fun upload(url: String, json: JSONObject): Response {
        val request: Request = Request.Builder().url(url)
                .addHeader("Authorization", "Token token=" + Hover.getApiKey(context))
                .post(createBody(json))
                .build()
        return client.newCall(request).execute()
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