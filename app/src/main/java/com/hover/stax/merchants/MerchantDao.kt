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
package com.hover.stax.merchants

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import com.hover.stax.database.dao.BaseDao

@Dao
interface MerchantDao : BaseDao<Merchant> {

    @get:Query("SELECT * FROM merchants ORDER BY last_used_timestamp ASC")
    val all: LiveData<List<Merchant>>

    @Query("SELECT * FROM merchants WHERE till_no = :till AND channelId = :channelId LIMIT 1")
    fun getMerchantsByNo(till: String, channelId: Int): Merchant?

    @Query("SELECT * FROM merchants WHERE till_no = :till AND channelId = :channelId LIMIT 1")
    fun getLiveMerchantsByNo(till: String, channelId: Int): LiveData<Merchant?>

    @Query("SELECT * FROM merchants WHERE id = :id LIMIT 1")
    fun getMerchant(id: Int): Merchant?

    @Query("DELETE FROM merchants")
    fun deleteAll()
}