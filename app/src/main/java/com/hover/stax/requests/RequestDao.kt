package com.hover.stax.requests

import androidx.lifecycle.LiveData
import androidx.room.*
import com.hover.stax.data.local.BaseDao

@Dao
interface RequestDao : BaseDao<Request>{

    @get:Query("SELECT * FROM requests ORDER BY date_sent DESC")
    val all: LiveData<List<Request>>

    @get:Query("SELECT * FROM requests WHERE matched_transaction_uuid IS NULL ORDER BY date_sent DESC")
    val liveUnmatched: LiveData<List<Request>>

    @Query("SELECT * FROM requests WHERE matched_transaction_uuid IS NULL AND requester_institution_id = :channelId ORDER BY date_sent DESC")
    fun getLiveUnmatchedByChannel(channelId: Int): LiveData<List<Request>>

    @get:Query("SELECT * FROM requests WHERE matched_transaction_uuid IS NULL ORDER BY date_sent DESC")
    val unmatched: List<Request>

    @Query("SELECT * FROM requests WHERE id = :id")
    operator fun get(id: Int): Request?

    @Insert
    fun insertRequest(request: Request?)

    @Update
    fun updateRequest(request: Request?)

    @Delete
    fun deleteRequest(request: Request?)
}