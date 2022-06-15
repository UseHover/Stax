package com.hover.stax.channels

import androidx.lifecycle.LiveData
import com.hover.sdk.actions.HoverAction
import com.hover.sdk.database.HoverRoomDatabase
import com.hover.sdk.sims.SimInfo
import com.hover.sdk.sims.SimInfoDao
import com.hover.stax.accounts.ChannelWithAccounts
import com.hover.stax.database.AppDatabase

class ChannelRepo(db: AppDatabase, sdkDb: HoverRoomDatabase) {

    private val simDao: SimInfoDao = sdkDb.simDao()
    private val channelDao: ChannelDao = db.channelDao()

    // SIMs
    val presentSims: List<SimInfo>
        get() = simDao.present

    val publishedChannels: LiveData<List<Channel>> = channelDao.publishedChannels

    val selected: LiveData<List<Channel>> = channelDao.getSelected(true)

    fun getChannel(id: Int): Channel? {
        return channelDao.getChannel(id)
    }

    fun getLiveChannel(id: Int): LiveData<Channel> {
        return channelDao.getLiveChannel(id)
    }

    fun getChannelsByIds(ids: List<Int>): List<Channel> = channelDao.getChannelsByIds(ids)

    suspend fun getChannelsByIdsAsync(ids: List<Int>): List<Channel> = channelDao.getChannelsByIds(ids)

    fun getChannelsByCountry(channelIds: IntArray, countryCode: String): LiveData<List<Channel>> {
        return channelDao.getChannels(countryCode.uppercase(), channelIds)
    }

    fun getChannelsByCountry(countryCode: String): List<Channel> {
        return channelDao.getChannels(countryCode.uppercase())
    }

    fun update(channel: Channel) = channelDao.update(channel)

    fun insert(channel: Channel) = channelDao.insert(channel)

    fun update(channels: List<Channel>) = channelDao.updateAll(channels)
}