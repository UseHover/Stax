package com.hover.stax.data.local.user

import androidx.room.*
import com.hover.stax.data.local.BaseDao
import com.hover.stax.domain.model.StaxUser
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao : BaseDao<StaxUser> {

    @Query("SELECT * FROM stax_users LIMIT 1")
    fun getUserAsync(): Flow<StaxUser>

    @Query("SELECT * FROM stax_users LIMIT 1")
    suspend fun getUser(): StaxUser?
}