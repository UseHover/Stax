package com.hover.stax.schedules

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ScheduleDao {
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

    @Insert
    fun insert(schedule: Schedule?)

    @Update
    fun update(schedule: Schedule?)

    @Delete
    fun delete(schedule: Schedule?)
}