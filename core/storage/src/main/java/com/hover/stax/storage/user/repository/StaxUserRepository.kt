package com.hover.stax.storage.user.repository

import com.hover.stax.storage.user.entity.StaxUser
import kotlinx.coroutines.flow.Flow

interface StaxUserRepository {
    fun getUserAsync(): Flow<StaxUser>

    suspend fun saveUser(user: StaxUser)

    suspend fun deleteUser(user: StaxUser)
}