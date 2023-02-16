package com.hover.stax.storage.user.dao

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Update

/**
 * BaseDao<T>
 *
 * This dao interface makes it easy to abstract commonly used room operations
 *
 * @param T takes in the data class
 */
interface BaseDao<T> {

    // TODO - Make me suspendable
    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insert(item: T)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAsync(item: T)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(vararg items: T)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(items: List<T>)

    @Update
    suspend fun update(item: T?): Int

    @Update
    suspend fun update(items: List<T>): Int

    @Delete
    suspend fun delete(item: T)
}