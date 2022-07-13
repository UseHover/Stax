package com.hover.stax.bonus

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BonusDao {

    @get:Query("SELECT * FROM bonuses")
    val bonuses: Flow<List<Bonus>>

    @Query("SELECT * FROM bonuses WHERE purchase_channel = :purchaseChannelId")
    fun getBonusByPurchaseChannel(purchaseChannelId: Int): Bonus?

    @Query("SELECT * FROM bonuses WHERE user_channel = :userChannelId")
    fun getBonusByUserChannel(userChannelId: Int): Bonus?

    @Query("SELECT * FROM bonuses WHERE purchase_channel IN (:purchaseChannelIds) AND user_channel in (:userChannelIds)")
    fun getBonuses(purchaseChannelIds: List<Int>, userChannelIds: List<Int>): List<Bonus>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(bonus: Bonus)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(bonuses: List<Bonus>)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(bonus: Bonus)

    @Update
    fun updateAll(bonuses: List<Bonus>)

    @Delete
    fun delete(bonus: Bonus)

    @Query("DELETE FROM bonuses")
    fun deleteAll()

    @Transaction
    fun deleteAndSave(bonuses: List<Bonus>) {
        deleteAll()
        insertAll(bonuses)
    }
}