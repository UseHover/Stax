package com.hover.stax.paybill

import androidx.room.*
import com.hover.stax.data.local.BaseDao
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