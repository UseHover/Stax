package com.hover.stax.settings

import android.content.Context
import com.hover.sdk.api.Hover
import com.hover.stax.R
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber
import java.io.IOException
import java.util.*

class LoginNetworking(private val context: Context) {

    private val client = OkHttpClient()
    private val url: String = context.getString(R.string.api_url) + context.getString(R.string.users_endpoint)

    fun uploadUserToStax(email: String, optedIn: Boolean): Map<Int, String?> {
        val json = getJson(email, optedIn)
        val resultMap: MutableMap<Int, String?> = HashMap()
        return try {
            val body: RequestBody = json.toString().toRequestBody("application/json".toMediaTypeOrNull())
            val request: Request = Request.Builder().url(url)
                    .addHeader("Authorization", "Token token=" + Hover.getApiKey(context))
                    .post(body)
                    .build()
            val response = client.newCall(request).execute()
            resultMap[response.code] = response.toString()
            resultMap
        } catch (e: IOException) {
            resultMap[0] = e.message
            resultMap
        }
    }

    private fun getJson(email: String, optedIn: Boolean): JSONObject {
        val root = JSONObject()
        try {
            val staxBountyHunter = JSONObject()
            staxBountyHunter.put("email", email)
            staxBountyHunter.put("device_id", Hover.getDeviceId(context))
            staxBountyHunter.put("is_mapper", true)
            staxBountyHunter.put("marketing_opted_in", optedIn)
            root.put("stax_user", staxBountyHunter)
            Timber.d("uploading %s", root)
        } catch (ignored: JSONException) {
        }
        return root
    }
}