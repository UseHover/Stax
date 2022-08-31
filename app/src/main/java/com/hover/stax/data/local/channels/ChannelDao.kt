package com.hover.stax.data.local.channels

import androidx.lifecycle.LiveData
import androidx.room.*
import com.hover.stax.accounts.ChannelWithAccounts
import com.hover.stax.channels.Channel

@Dao
interface ChannelDao {

    @get:Query("SELECT * FROM channels WHERE published = 1 AND institution_type != 'telecom' ORDER BY isFavorite DESC, name ASC")
    val publishedNonTelecomChannels: LiveData<List<Channel>>

    @Query("SELECT * FROM channels WHERE institution_type == 'telecom' AND published = 1")
    suspend fun publishedTelecomChannels(): List<Channel>

    @get:Query("SELECT * FROM channels WHERE institution_type != 'telecom' ORDER BY name ASC")
    val allChannels: LiveData<List<Channel>>

    @Query("SELECT * FROM channels WHERE selected = :selected AND institution_type != 'telecom' ORDER BY defaultAccount DESC, name ASC")
    fun getSelected(selected: Boolean): LiveData<List<Channel>>

    @Query("SELECT * FROM channels WHERE id IN (:channel_ids) ORDER BY name ASC")
    fun getChannelsByIds(channel_ids: List<Int>): List<Channel>

    @Query("SELECT * FROM channels WHERE id IN (:channel_ids) ORDER BY name ASC")
    fun getChannels(channel_ids: IntArray): LiveData<List<Channel>>

    @Query("SELECT * FROM channels WHERE country_alpha2 = :countryCode ORDER BY name ASC")
    fun getChannels(countryCode: String): List<Channel>

//    @Query("SELECT * FROM channels WHERE country_alpha2 = :countryCode AND id IN (:channel_ids) ORDER BY name ASC")
//    fun getChannels(countryCode: String, channel_ids: IntArray): LiveData<List<Channel>>

    @Query("SELECT * FROM channels WHERE country_alpha2 = :countryCode AND id IN (:channel_ids) ORDER BY name ASC")
    fun getChannels(countryCode: String, channel_ids: IntArray): List<Channel>

    @Query("SELECT * FROM channels WHERE id = :id LIMIT 1")
    fun getChannel(id: Int): Channel?

    @Query("SELECT * FROM channels WHERE id = :id LIMIT 1")
    fun getLiveChannel(id: Int): LiveData<Channel>

    @get:Query("SELECT * FROM channels")
    val channels: List<Channel>

    @Transaction
    @Query("SELECT * FROM channels where selected = 1 AND institution_type != 'telecom' ORDER BY name ASC")
    fun getChannelsAndAccounts(): List<ChannelWithAccounts>

    @Transaction
    @Query("SELECT * FROM channels where id = :id ORDER BY name ASC")
    fun getChannelAndAccounts(id: Int): ChannelWithAccounts?

    @get:Query("SELECT COUNT(id) FROM channels")
    val allDataCount: Int

    @get:Query("SELECT COUNT(id) FROM channels WHERE institution_type == 'telecom' AND published = 1")
    val publishedTelecomDataCount: Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAll(vararg channels: Channel?)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(channel: Channel?)

    @Update
    fun update(channel: Channel)

    @Update
    fun updateAll(channel: List<Channel>)

    @Delete
    fun delete(channel: Channel)

    @Query("DELETE FROM channels")
    fun deleteAll()

}