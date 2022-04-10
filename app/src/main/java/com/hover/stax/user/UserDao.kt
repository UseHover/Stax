package com.hover.stax.user

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Query("SELECT * FROM stax_users LIMIT 1")
    fun getUserAsync(): Flow<StaxUser?>

    @Query("SELECT * FROM stax_users LIMIT 1")
    fun getUser(): StaxUser?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(user: StaxUser)

    @Update
    fun update(user: StaxUser)

    @Delete
    fun delete(user: StaxUser)
}