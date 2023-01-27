package com.hover.stax.database.dao.user

import androidx.room.Dao
import androidx.room.Query
import com.hover.stax.database.dao.BaseDao
import com.hover.stax.database.entity.StaxUser
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao : BaseDao<StaxUser> {
    @Query("SELECT * FROM stax_users LIMIT 1")
    fun getUserAsync(): Flow<StaxUser>

    @Query("SELECT * FROM stax_users LIMIT 1")
    suspend fun getUser(): StaxUser?
}