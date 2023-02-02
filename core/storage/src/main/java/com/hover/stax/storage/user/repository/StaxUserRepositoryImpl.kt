package com.hover.stax.database.repository

import com.hover.stax.database.dao.user.UserDao
import com.hover.stax.database.entity.StaxUser
import kotlinx.coroutines.flow.Flow

internal class StaxUserRepositoryImpl(
    private val userDao: UserDao
) : StaxUserRepository {
    override fun getUserAsync(): Flow<StaxUser> = userDao.getUserAsync()

    override suspend fun saveUser(user: StaxUser) {
        userDao.getUser()?.let {
            userDao.insertAsync(user)
        } ?: userDao.update(user)
    }

    override suspend fun deleteUser(user: StaxUser) = userDao.delete(user)
}