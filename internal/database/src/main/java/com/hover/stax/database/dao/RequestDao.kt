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
package com.hover.stax.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.hover.stax.database.models.Request

@Dao
interface RequestDao : BaseDao<Request> {

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