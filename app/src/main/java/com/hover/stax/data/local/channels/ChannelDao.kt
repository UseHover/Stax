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
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.hover.stax.channels.Channel
import com.hover.stax.database.dao.BaseDao

@Dao
interface ChannelDao : BaseDao<Channel> {

    @get:Query("SELECT * FROM channels WHERE published = 1 AND institution_type != 'telecom' ORDER BY isFavorite DESC, name ASC")
    val publishedNonTelecomChannels: LiveData<List<Channel>>

    @Query("SELECT * FROM channels WHERE published = 1 AND institution_type = 'telecom' AND hni_list LIKE '%' || :hni || '%'")
    suspend fun getTelecom(hni: String): Channel?

    @Query("SELECT * FROM channels WHERE institution_id = :fromInstitutionId AND published = 1")
    suspend fun getChannelByInstitution(fromInstitutionId: Int): Channel?

    @get:Query("SELECT * FROM channels WHERE institution_type != 'telecom' ORDER BY name ASC")
    val allChannels: LiveData<List<Channel>>

    @Query("SELECT * FROM channels WHERE id IN (:channel_ids) ORDER BY name ASC")
    fun getChannelsByIds(channel_ids: List<Int>): List<Channel>

    @Query("SELECT * FROM channels WHERE id IN (:channel_ids) ORDER BY name ASC")
    fun getChannels(channel_ids: IntArray): LiveData<List<Channel>>

    @Query("SELECT * FROM channels WHERE country_alpha2 = :countryCode ORDER BY name ASC")
    fun getChannels(countryCode: String): List<Channel>

    @Query("SELECT * FROM channels WHERE country_alpha2 = :countryCode AND id IN (:channel_ids) ORDER BY name ASC")
    fun getChannels(countryCode: String, channel_ids: IntArray): List<Channel>

    @Query("SELECT * FROM channels WHERE id = :id LIMIT 1")
    fun getChannel(id: Int): Channel?

    @Query("SELECT * FROM channels WHERE id = :id LIMIT 1")
    fun getLiveChannel(id: Int): LiveData<Channel>

    @get:Query("SELECT * FROM channels")
    val channels: List<Channel>

    @get:Query("SELECT COUNT(id) FROM channels")
    val allDataCount: Int

    @get:Query("SELECT COUNT(id) FROM channels WHERE institution_type == 'telecom' AND published = 1")
    val publishedTelecomDataCount: Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(channel: Channel?)

    @Update
    fun update(channel: Channel)

    @Query("DELETE FROM channels")
    fun deleteAll()
}