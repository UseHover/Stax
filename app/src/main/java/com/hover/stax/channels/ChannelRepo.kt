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

    fun getSims(hnis: Array<String?>?): List<SimInfo> {
        return simDao.getPresentByHnis(hnis)
    }

    val publishedChannels: LiveData<List<Channel>> = channelDao.publishedChannels
    val allChannels = channelDao.allChannels

    val selected: LiveData<List<Channel>> = channelDao.getSelected(true)

    fun getChannelsAndAccounts(): List<ChannelWithAccounts> = channelDao.getChannelsAndAccounts()

    fun getChannelAndAccounts(id: Int): ChannelWithAccounts? = channelDao.getChannelAndAccounts(id)

    fun getChannel(id: Int): Channel? {
        return channelDao.getChannel(id)
    }

    fun getLiveChannel(id: Int): LiveData<Channel> {
        return channelDao.getLiveChannel(id)
    }

    fun getChannels(ids: IntArray): LiveData<List<Channel>> {
        return channelDao.getChannels(ids)
    }

    fun getChannelsByIds(ids: List<Int>): List<Channel> = channelDao.getChannelsByIds(ids)

    fun getChannelsByCountry(channelIds: IntArray, countryCode: String): LiveData<List<Channel>> {
        return channelDao.getChannels(countryCode.uppercase(), channelIds)
    }

    fun getChannelsByCountry(countryCode: String): List<Channel> {
        return channelDao.getChannels(countryCode.uppercase())
    }

    fun update(channel: Channel?) = AppDatabase.databaseWriteExecutor.execute { channelDao.update(channel) }

    fun insert(channel: Channel) = AppDatabase.databaseWriteExecutor.execute { channelDao.insert(channel) }
}