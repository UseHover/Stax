package com.hover.stax.database.repository

import com.hover.stax.database.entity.StaxUser
import kotlinx.coroutines.flow.Flow

interface StaxUserRepository {
    fun getUserAsync(): Flow<StaxUser>

    suspend fun saveUser(user: StaxUser)

    suspend fun deleteUser(user: StaxUser)
}