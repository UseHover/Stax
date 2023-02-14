package com.hover.stax.channels

import android.content.Context
import com.hover.stax.R
import com.hover.stax.storage.channel.entity.Channel
import com.hover.stax.storage.channel.repository.ChannelRepository
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber

object ChannelUtil {
    private fun update(jsonObject: JSONObject, context: Context): Channel? {
        return try {
            Channel(
                id = jsonObject.getInt("id"),
                name = jsonObject.getString("name"),
                rootCode = jsonObject.getString("root_code"),
                countryAlpha2 = jsonObject.getString("country_alpha2"),
                currency = jsonObject.getString("currency"),
                hniList = jsonObject.getString("hni_list"),
                published = jsonObject.getBoolean("published"),
                logoUrl = context.getString(R.string.root_url) + jsonObject.getString("logo_url"),
                institutionId = jsonObject.getInt("institution_id"),
                primaryColorHex = jsonObject.getString("primary_color_hex"),
                secondaryColorHex = jsonObject.getString("secondary_color_hex"),
                institutionType = jsonObject.getString("institution_type")
            )
        } catch (e: JSONException) {
            Timber.d(e.localizedMessage)
            null
        }
    }

    suspend fun load(jsonArray: JSONArray, channelRepository: ChannelRepository, context: Context) {
        for (i in 0 until jsonArray.length()) {
            var channel = jsonArray.optJSONObject(i).optJSONObject("attributes")?.let {
                channelRepository.getChannel(it.optInt("id"))
            }

            channel?.let {
                update(
                    jsonArray.optJSONObject(i).getJSONObject("attributes"),
                    context
                )?.let { channel ->
                    channelRepository.update(channel = channel)
                }
            } ?: run {
                channel = jsonArray.getJSONObject(i).optJSONObject("attributes")?.let {
                    update(it, context)
                }
                channel?.let { channelRepository.insert(it) }
            }
        }
    }
}