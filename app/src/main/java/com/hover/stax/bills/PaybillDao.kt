package com.hover.stax.bills

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PaybillDao {

    @Query("SELECT * FROM paybills ORDER BY name ASC")
    fun getBills(): Flow<List<Paybill>>

    @Query("SELECT * FROM paybills WHERE accountId = :accountId ORDER BY name ASC")
    fun getBillsByAccount(accountId: Int): List<Paybill>

    @Query("SELECT * FROM paybills WHERE id = :id")
    fun getBill(id: Int): Paybill?
}