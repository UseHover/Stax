/*
 * Copyright 2023 Stax
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
package com.hover.stax.database.channel.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.hover.sdk.actions.HoverAction
import com.hover.sdk.sims.SimInfo
import com.hover.stax.database.R
import com.hover.stax.database.channel.dao.ChannelDao
import com.hover.stax.database.channel.entity.Channel
import com.hover.stax.database.sim.repository.SimInfoRepository
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber

internal class ChannelRepositoryImpl(
    private val channelDao: ChannelDao,
    private val simInfoRepository: SimInfoRepository
) : ChannelRepository {

    override val publishedNonTelecomChannels: LiveData<List<Channel>> =
        channelDao.publishedNonTelecomChannels

    override val allDataCount: Int
        get() = channelDao.allDataCount

    override val publishedTelecomDataCount: Int
        get() = channelDao.publishedTelecomDataCount

    override suspend fun getTelecom(hni: String): Channel? = channelDao.getTelecom(hni)

    override fun getChannel(id: Int): Channel? = channelDao.getChannel(id)

    override fun getLiveChannel(id: Int): LiveData<Channel> = channelDao.getLiveChannel(id)

    override suspend fun getChannelByInstitution(
        institutionId: Int
    ): Channel? = channelDao.getChannelByInstitution(institutionId)

    override fun getChannelsByIds(ids: List<Int>): List<Channel> = channelDao.getChannelsByIds(ids)

    override fun getChannelsByIdsAsync(ids: List<Int>): List<Channel> =
        channelDao.getChannelsByIds(ids)

    override fun getChannelsByCountry(
        channelIds: IntArray,
        countryCode: String
    ): List<Channel> = channelDao.getChannels(countryCode.lowercase(), channelIds)

    override fun getChannelsByCountry(countryCode: String): List<Channel> =
        channelDao.getChannels(countryCode)

    override suspend fun update(channel: Channel) = channelDao.update(channel)

    override fun insert(channel: Channel) = channelDao.insert(channel)
    override suspend fun presentSims(): List<SimInfo> = simInfoRepository.getPresentSims()

    override suspend fun filterChannels(
        countryCode: String,
        actions: List<HoverAction>
    ): List<Channel> {
        val ids = actions.asSequence().distinctBy { it.channel_id }.map { it.channel_id }.toList()
        return if (countryCode == CODE_ALL_COUNTRIES) getChunkedChannelsByIds(ids)
        else getChannelsByCountry(ids.toIntArray(), countryCode)
    }

    override suspend fun update(channels: List<Channel>): Int = channelDao.update(channels)

    private fun getChunkedChannelsByIds(ids: List<Int>): List<Channel> {
        val channels = mutableListOf<Channel>()

        ids.chunked(MAX_LOOKUP_COUNT).forEach { idList ->
            val results = getChannelsByIds(idList)
            channels.addAll(results)
        }

        return channels
    }

    companion object {
        const val CODE_ALL_COUNTRIES: String = "00"
        private const val MAX_LOOKUP_COUNT = 40
    }

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
}