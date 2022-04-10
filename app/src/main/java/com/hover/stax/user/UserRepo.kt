package com.hover.stax.user

import com.hover.stax.database.AppDatabase
import kotlinx.coroutines.flow.Flow

class UserRepo(db: AppDatabase) {

    private val userDao = db.userDao()

    val user: Flow<StaxUser?> = userDao.getUserAsync()

    fun saveUser(user: StaxUser) {
        if (userDao.getUser() == null)
            userDao.insert(user)
        else
            userDao.update(user)
    }

    fun updateUser(user: StaxUser) = userDao.update(user)

    fun deleteUser(user: StaxUser) = userDao.delete(user)
}