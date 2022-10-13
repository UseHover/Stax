package com.hover.stax.data.local.channels

import androidx.lifecycle.LiveData
import com.hover.sdk.database.HoverRoomDatabase
import com.hover.sdk.sims.SimInfo
import com.hover.sdk.sims.SimInfoDao
import com.hover.stax.channels.Channel
import com.hover.stax.database.AppDatabase

class ChannelRepo(db: AppDatabase) {

    private val channelDao: ChannelDao = db.channelDao()

    val publishedNonTelecomChannels: LiveData<List<Channel>> = channelDao.publishedNonTelecomChannels
    suspend fun publishedTelecomChannels() : List<Channel> = channelDao.publishedTelecomChannels()

    fun getChannel(id: Int): Channel? { return channelDao.getChannel(id) }

    fun getLiveChannel(id: Int): LiveData<Channel> { return channelDao.getLiveChannel(id) }

    suspend fun getChannelByInstitution(institutionId : Int) : Channel? = channelDao.getChannelByInstitution(institutionId)

    fun getChannelsByIds(ids: List<Int>): List<Channel> = channelDao.getChannelsByIds(ids)

    fun getChannelsByIdsAsync(ids: List<Int>): List<Channel> = channelDao.getChannelsByIds(ids)

    fun getChannelsByCountry(channelIds: IntArray, countryCode: String): List<Channel> = channelDao.getChannels(countryCode, channelIds)

    fun getChannelsByCountry(countryCode: String): List<Channel> {
        return channelDao.getChannels(countryCode.uppercase())
    }

    fun update(channel: Channel) = channelDao.update(channel)

    fun insert(channel: Channel) = channelDao.insert(channel)

    fun update(channels: List<Channel>) = channelDao.updateAll(channels)
}