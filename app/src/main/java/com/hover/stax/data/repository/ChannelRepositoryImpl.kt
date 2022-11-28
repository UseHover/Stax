package com.hover.stax.data.repository

import com.hover.sdk.actions.HoverAction
import com.hover.sdk.sims.SimInfo
import com.hover.stax.channels.Channel
import com.hover.stax.countries.CountryAdapter
import com.hover.stax.data.local.SimRepo
import com.hover.stax.data.local.channels.ChannelRepo
import com.hover.stax.domain.repository.ChannelRepository

private const val MAX_LOOKUP_COUNT = 40

class ChannelRepositoryImpl(val channelRepo: ChannelRepo,val simRepo: SimRepo) : ChannelRepository {

    override suspend fun presentSims(): List<SimInfo> = simRepo.getPresentSims()

    override suspend fun getChannelsByIds(ids: List<Int>): List<Channel> = channelRepo.getChannelsByIds(ids)

    override suspend fun getChannelsByCountryCode(ids: IntArray, countryCode: String): List<Channel> = channelRepo.getChannelsByCountry(ids, countryCode)

    override suspend fun filterChannels(countryCode: String, actions: List<HoverAction>): List<Channel> {
        val ids = actions.asSequence().distinctBy { it.channel_id }.map { it.channel_id }.toList()

        return if (countryCode == CountryAdapter.CODE_ALL_COUNTRIES)
            getChunkedChannelsByIds(ids)
        else
            getChannelsByCountryCode(ids.toIntArray(), countryCode)
    }

    private fun getChunkedChannelsByIds(ids: List<Int>): List<Channel> {
        val channels = mutableListOf<Channel>()

        ids.chunked(MAX_LOOKUP_COUNT).forEach { idList ->
            val results = channelRepo.getChannelsByIds(idList)
            channels.addAll(results)
        }

        return channels
    }

}