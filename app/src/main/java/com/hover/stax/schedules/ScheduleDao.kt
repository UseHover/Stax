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
package com.hover.stax.schedules

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Update
import com.hover.stax.storage.BaseDao

@Dao
interface ScheduleDao : BaseDao<Schedule> {

    @get:Query("SELECT * FROM schedules")
    val all: LiveData<List<Schedule>>

    // Epoch ms minus one day
    @get:Query("SELECT * FROM schedules WHERE start_date > (strftime('%s','now')*1000 - 86400000) AND complete = 0 ORDER BY frequency, start_date DESC")
    val liveFuture: LiveData<List<Schedule>>

    @Query("SELECT * FROM schedules WHERE start_date > (strftime('%s','now')*1000 - 86400000) AND complete = 0 AND channel_id=:channelId ORDER BY frequency, start_date DESC")
    fun getLiveFutureByChannelId(channelId: Int): LiveData<List<Schedule>>

    @get:Query("SELECT * FROM schedules WHERE start_date > (strftime('%s','now')*1000 - 86400000) AND complete = 0 ORDER BY frequency, start_date DESC")
    val future: List<Schedule>

    @Query("SELECT * FROM schedules WHERE id = :id")
    fun get(id: Int): Schedule?

    @Update
    fun updateSchedule(schedule: Schedule?)

    @Delete
    fun deleteSchedule(schedule: Schedule?)
}