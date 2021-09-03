package com.hover.stax.contacts

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface ContactDao {
    @get:Query("SELECT * FROM stax_contacts ORDER BY name, phone_number, last_used_timestamp")
    val all: LiveData<List<StaxContact>>

    @Query("SELECT * FROM stax_contacts WHERE id IN (:ids) ORDER BY name, phone_number, last_used_timestamp")
    operator fun get(ids: Array<String?>?): List<StaxContact>

    @Query("SELECT * FROM stax_contacts WHERE id IN (:ids)")
    fun getLive(ids: Array<String?>?): LiveData<List<StaxContact>>

    @Query("SELECT * FROM stax_contacts WHERE id  = :id LIMIT 1")
    operator fun get(id: String?): StaxContact?

    @Query("SELECT * FROM stax_contacts WHERE id  = :id LIMIT 1")
    suspend fun get_suspended(id: String?): StaxContact?



    @Query("SELECT * FROM stax_contacts WHERE lookup_key  = :lookupKey LIMIT 1")
    fun lookup(lookupKey: String?): StaxContact

    @Query("SELECT * FROM stax_contacts WHERE phone_number LIKE :phone LIMIT 1")
    fun getByPhone(phone: String?): StaxContact?

    @Query("SELECT * FROM stax_contacts WHERE id  = :id LIMIT 1")
    fun getLive(id: String?): LiveData<StaxContact>

    @Insert
    fun insert(contact: StaxContact?)

    @Update
    fun update(contact: StaxContact?)
}