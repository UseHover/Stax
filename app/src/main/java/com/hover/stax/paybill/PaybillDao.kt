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
package com.hover.stax.paybill

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.hover.stax.database.BaseDao
import kotlinx.coroutines.flow.Flow

@Dao
interface PaybillDao : BaseDao<Paybill> {

    @get:Query("SELECT * FROM paybills ORDER BY name ASC")
    val allBills: Flow<List<Paybill>>

    @Query("SELECT * FROM paybills WHERE accountId = :accountId ORDER BY name ASC")
    fun getPaybillsByAccount(accountId: Int): Flow<List<Paybill>>

    @Query("SELECT * FROM paybills WHERE id = :id")
    fun getPaybill(id: Int): Paybill?

    @Query("SELECT * FROM paybills WHERE business_no = :bizNo AND channelId = :channelId")
    fun getPaybill(bizNo: String, channelId: Int): Paybill?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(vararg paybill: Paybill?)
}