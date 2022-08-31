package com.hover.stax.data.local.auth

import com.hover.stax.database.AppDatabase
import com.hover.stax.domain.model.TokenInfo

class AuthRepo(db: AppDatabase) {

    private val authDao = db.authDao()

    suspend fun getTokenInfo(): TokenInfo? = authDao.getTokenInfo()

    suspend fun saveTokenInfo(tokenInfo: TokenInfo) = authDao.saveTokenInfo(tokenInfo)

    suspend fun deleteTokenInfo() = authDao.delete()
}