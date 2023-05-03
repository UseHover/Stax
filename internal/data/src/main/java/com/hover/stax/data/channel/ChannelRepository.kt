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
package com.hover.stax.data.channel

import androidx.lifecycle.LiveData
import com.hover.sdk.actions.HoverAction
import com.hover.sdk.sims.SimInfo
import com.hover.stax.database.models.Channel

interface ChannelRepository {

    val publishedNonTelecomChannels: LiveData<List<Channel>>

    val allDataCount: Int

    val publishedTelecomDataCount: Int

    suspend fun getTelecom(hni: String): Channel?

    fun getChannel(id: Int): Channel?

    fun getLiveChannel(id: Int): LiveData<Channel>

    suspend fun getChannelByInstitution(institutionId: Int): Channel?

    fun getChannelsByIds(ids: List<Int>): List<Channel>

    fun getChannelsByIdsAsync(ids: List<Int>): List<Channel>

    fun getChannelsByCountry(channelIds: IntArray, countryCode: String): List<Channel>

    fun getChannelsByCountry(countryCode: String): List<Channel>

    suspend fun update(channel: Channel)

    fun insert(channel: Channel)

    suspend fun update(channels: List<Channel>): Int

    suspend fun presentSims(): List<SimInfo>

    suspend fun filterChannels(countryCode: String, actions: List<HoverAction>): List<Channel>
}