package com.hover.stax.paybill

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PaybillDao {

    @get:Query("SELECT * FROM paybills ORDER BY name ASC")
    val allBills: Flow<List<Paybill>>

    @Query("SELECT * FROM paybills WHERE accountId = :accountId ORDER BY name ASC")
    fun getBillsByAccount(accountId: Int): Flow<List<Paybill>>

    @Query("SELECT * FROM paybills WHERE id = :id")
    fun getBill(id: Int): Paybill?
}