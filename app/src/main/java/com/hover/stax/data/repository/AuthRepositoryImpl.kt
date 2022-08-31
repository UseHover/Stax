package com.hover.stax.data.repository

import com.hover.stax.data.local.auth.AuthRepo
import com.hover.stax.data.remote.StaxApi
import com.hover.stax.data.remote.dto.authorization.TokenRequest
import com.hover.stax.data.remote.dto.authorization.toTokenInfo
import com.hover.stax.domain.model.TokenInfo
import com.hover.stax.domain.repository.AuthRepository

class AuthRepositoryImpl(private val authRepo: AuthRepo, private val staxApi: StaxApi) : AuthRepository {

    override suspend fun fetchTokenInfo(tokenRequest: TokenRequest): TokenInfo {
        val tokenResponse = staxApi.fetchToken(tokenRequest)
        val tokenInfo = tokenResponse.toTokenInfo().also {
            saveTokenInfo(it)
        }

        return tokenInfo
    }

    override suspend fun getTokenInfo(): TokenInfo? = authRepo.getTokenInfo()

    override suspend fun saveTokenInfo(tokenInfo: TokenInfo) = authRepo.saveTokenInfo(tokenInfo)

    override suspend fun deleteTokenInfo() = authRepo.deleteTokenInfo()
}