/*
 * Copyright 2022 Stax
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
package com.hover.stax.data.local.channels

import androidx.lifecycle.LiveData
import com.hover.stax.channels.Channel
import com.hover.stax.database.AppDatabase
import com.hover.stax.domain.model.USSDAccount

class ChannelRepo(db: AppDatabase) {

    private val channelDao: ChannelDao = db.channelDao()

    val publishedNonTelecomChannels: LiveData<List<Channel>> =
        channelDao.publishedNonTelecomChannels

    suspend fun getTelecom(hni: String): Channel? = channelDao.getTelecom(hni)

    fun getChannel(id: Int): Channel? {
        return channelDao.getChannel(id)
    }

    fun getLiveChannel(id: Int): LiveData<Channel> {
        return channelDao.getLiveChannel(id)
    }

    suspend fun getChannelByInstitution(institutionId: Int): Channel? =
        channelDao.getChannelByInstitution(institutionId)

    fun getChannelsByIds(ids: List<Int>): List<Channel> = channelDao.getChannelsByIds(ids)

    fun getChannelsByIdsAsync(ids: List<Int>): List<Channel> = channelDao.getChannelsByIds(ids)

    fun getChannelsByCountry(channelIds: IntArray, countryCode: String): List<Channel> =
        channelDao.getChannels(countryCode.lowercase(), channelIds)

    fun getChannelsByCountry(countryCode: String): List<Channel> {
        return channelDao.getChannels(countryCode.lowercase())
    }

    suspend fun update(channel: Channel) = channelDao.update(channel)

    fun insert(channel: Channel) = channelDao.insert(channel)

    suspend fun update(channels: List<Channel>) = channelDao.update(channels)
}