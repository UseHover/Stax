package com.hover.stax.user

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @get:Query("SELECT * FROM stax_users LIMIT 1")
    val user: Flow<StaxUser>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(user: StaxUser)

    @Update
    fun update(user: StaxUser)

    @Delete
    fun delete(user: StaxUser)
}