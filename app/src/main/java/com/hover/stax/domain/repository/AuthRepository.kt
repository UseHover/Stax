package com.hover.stax.domain.repository

import com.hover.stax.data.remote.dto.authorization.TokenRequest
import com.hover.stax.domain.model.TokenInfo

interface AuthRepository {

    suspend fun fetchTokenInfo(tokenRequest: TokenRequest): TokenInfo

    suspend fun getTokenInfo(): TokenInfo?

    suspend fun saveTokenInfo(tokenInfo: TokenInfo)

    suspend fun deleteTokenInfo()
}