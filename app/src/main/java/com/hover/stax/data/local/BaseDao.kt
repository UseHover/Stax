package com.hover.stax.data.local

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
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
    @Insert(onConflict = REPLACE)
    fun insert(item: T)

    @Insert(onConflict = REPLACE)
    suspend fun insertAsync(item: T)

    @Insert(onConflict = REPLACE)
    suspend fun insert(vararg items: T)

    @Insert(onConflict = REPLACE)
    suspend fun insert(items: List<T>)

    @Update(onConflict = REPLACE)
    suspend fun update(item: T?): Int

    @Update(onConflict = REPLACE)
    suspend fun update(items: List<T>): Int

    @Delete
    suspend fun delete(item: T)
}