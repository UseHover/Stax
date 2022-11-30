package com.hover.stax.domain.repository

import com.hover.stax.domain.model.StaxUser
import kotlinx.coroutines.flow.Flow

interface StaxUserRepository {

    val staxUser: Flow<StaxUser>

    suspend fun saveUser(user: StaxUser)

    suspend fun deleteUser(user: StaxUser)

}