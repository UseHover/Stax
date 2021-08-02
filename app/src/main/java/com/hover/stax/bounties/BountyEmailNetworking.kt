package com.hover.stax.bounties

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

internal class BountyEmailNetworking(private val context: Context) {
    private val client = OkHttpClient()
    private val url: String = context.getString(R.string.api_url) + context.getString(R.string.bounty_endpoint)

    fun uploadBountyUser(email: String): Map<Int, String?> {

        val json = getJson(email)
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

   private fun getJson(email: String): JSONObject {
        val root = JSONObject()
        try {
            val stax_bounty_hunter = JSONObject()
            stax_bounty_hunter.put("email", email)
            stax_bounty_hunter.put("device_id", Hover.getDeviceId(context))
            root.put("stax_bounty_hunter", stax_bounty_hunter)
            Timber.d("uploading %s", root)
        } catch (ignored: JSONException) {
        }
        return root
    }

    companion object {
        private const val TAG = "BountyEmailViewModel"
    }
}