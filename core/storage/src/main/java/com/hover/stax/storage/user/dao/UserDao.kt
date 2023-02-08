package com.hover.stax.storage.user.dao

import androidx.room.Dao
import androidx.room.Query
import com.hover.stax.storage.BaseDao
import com.hover.stax.storage.user.entity.StaxUser
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao : BaseDao<StaxUser> {
    @Query("SELECT * FROM stax_users LIMIT 1")
    fun getUserAsync(): Flow<StaxUser>

    @Query("SELECT * FROM stax_users LIMIT 1")
    suspend fun getUser(): StaxUser?
}