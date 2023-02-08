package com.hover.stax.storage.channel.repository

import androidx.lifecycle.LiveData
import com.hover.stax.storage.channel.dao.ChannelDao
import com.hover.stax.storage.channel.entity.Channel

internal class ChannelRepositoryImpl(
    private val channelDao: ChannelDao
) : ChannelRepository {
    override val publishedNonTelecomChannels: LiveData<List<Channel>> =
        channelDao.publishedNonTelecomChannels

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

    override suspend fun update(channels: List<Channel>): Int = channelDao.update(channels)
}