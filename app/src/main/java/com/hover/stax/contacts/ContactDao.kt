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
package com.hover.stax.contacts

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update
import com.hover.stax.database.dao.BaseDao

@Dao
interface ContactDao : BaseDao<StaxContact> {
    @get:Query("SELECT * FROM stax_contacts ORDER BY name, phone_number, last_used_timestamp")
    val all: LiveData<List<StaxContact>>

    @Query("SELECT * FROM stax_contacts WHERE id IN (:ids) ORDER BY name, phone_number, last_used_timestamp")
    operator fun get(ids: Array<String>): List<StaxContact>

    @Query("SELECT * FROM stax_contacts WHERE id IN (:ids)")
    fun getLive(ids: Array<String>): LiveData<List<StaxContact>>

    @Query("SELECT * FROM stax_contacts WHERE id  = :id LIMIT 1")
    operator fun get(id: String?): StaxContact?

    @Query("SELECT * FROM stax_contacts WHERE id  = :id LIMIT 1")

    suspend fun getAsync(id: String?): StaxContact?

    @Query("SELECT * FROM stax_contacts WHERE lookup_key  = :lookupKey LIMIT 1")
    fun lookup(lookupKey: String?): StaxContact

    @Query("SELECT * FROM stax_contacts WHERE phone_number LIKE :phone LIMIT 1")
    fun getByPhone(phone: String?): StaxContact?

    @Query("SELECT * FROM stax_contacts WHERE id  = :id LIMIT 1")
    fun getLive(id: String?): LiveData<StaxContact>

    // Need to rework the implementation on ContactRepo
    @Update
    fun updateStaxContact(contact: StaxContact?)
}