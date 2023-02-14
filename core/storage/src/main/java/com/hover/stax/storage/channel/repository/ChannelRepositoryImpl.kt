package com.hover.stax.storage.channel.repository

import androidx.lifecycle.LiveData
import com.hover.sdk.actions.HoverAction
import com.hover.sdk.sims.SimInfo
import com.hover.stax.storage.channel.dao.ChannelDao
import com.hover.stax.storage.channel.entity.Channel
import com.hover.stax.storage.sim.SimInfoRepository

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
}