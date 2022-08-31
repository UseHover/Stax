package com.hover.stax.data.local.user

import androidx.room.*
import com.hover.stax.domain.model.StaxUser
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Query("SELECT * FROM stax_users LIMIT 1")
    fun getUserAsync(): Flow<StaxUser>

    @Query("SELECT * FROM stax_users LIMIT 1")
    suspend fun getUser(): StaxUser?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: StaxUser)

    @Update
    suspend fun update(user: StaxUser)

    @Delete
    suspend fun delete(user: StaxUser)
}