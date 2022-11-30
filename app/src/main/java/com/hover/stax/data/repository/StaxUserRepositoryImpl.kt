package com.hover.stax.data.repository

import com.hover.stax.data.local.user.UserRepo
import com.hover.stax.domain.model.StaxUser
import com.hover.stax.domain.repository.StaxUserRepository
import kotlinx.coroutines.flow.Flow

class StaxUserRepositoryImpl(private val userRepo: UserRepo) : StaxUserRepository {

    override val staxUser: Flow<StaxUser>
        get() = userRepo.user

    override suspend fun saveUser(user: StaxUser) = userRepo.saveUser(user)

    override suspend fun deleteUser(user: StaxUser) = userRepo.deleteUser(user)
}