package com.hover.stax.data.local.user

import com.hover.stax.database.AppDatabase
import com.hover.stax.domain.model.StaxUser
import kotlinx.coroutines.flow.Flow

class UserRepo(db: AppDatabase) {

    private val userDao = db.userDao()

    val user: Flow<StaxUser> = userDao.getUserAsync()

    suspend fun saveUser(user: StaxUser) {
        if (userDao.getUser() == null)
            userDao.insertAsync(user)
        else
            userDao.update(user)
    }

    suspend fun deleteUser(user: StaxUser) = userDao.delete(user)
}