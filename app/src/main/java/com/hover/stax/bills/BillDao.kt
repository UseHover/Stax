package com.hover.stax.bills

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BillDao {

    @Query("SELECT * FROM bills ORDER BY name ASC")
    fun getBills(): Flow<List<Bill>>

    @Query("SELECT * FROM bills WHERE accountId = :accountId ORDER BY name ASC")
    fun getBillsByAccount(accountId: Int): List<Bill>

    @Query("SELECT * FROM bills WHERE id = :id")
    fun getBill(id: Int): Bill?
}