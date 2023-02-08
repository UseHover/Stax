package com.hover.stax.storage.channel.repository

import androidx.lifecycle.LiveData
import com.hover.sdk.actions.HoverAction
import com.hover.sdk.sims.SimInfo
import com.hover.stax.storage.channel.entity.Channel

interface ChannelRepository {
    val publishedNonTelecomChannels: LiveData<List<Channel>>

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