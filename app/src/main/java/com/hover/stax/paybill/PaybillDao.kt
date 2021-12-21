package com.hover.stax.paybill

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PaybillDao {

    @get:Query("SELECT * FROM paybills ORDER BY name ASC")
    val allBills: Flow<List<Paybill>>

    @Query("SELECT * FROM paybills WHERE accountId = :accountId ORDER BY name ASC")
    fun getPaybillsByAccount(accountId: Int): Flow<List<Paybill>>

    @Query("SELECT * FROM paybills WHERE accountId = :accountId and isSaved = 1 ORDER BY name ASC")
    fun getSavedPaybills(accountId: Int): Flow<List<Paybill>>

    @Query("SELECT * FROM paybills WHERE id = :id")
    fun getPaybill(id: Int): Paybill?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(vararg paybill: Paybill?)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(paybill: Paybill)

    @Update
    fun update(paybill: Paybill)

    @Update
    fun update(paybill: List<Paybill>)

    @Delete
    fun delete(paybill: Paybill)

    @Query("DELETE FROM paybills")
    fun deleteAll()
}