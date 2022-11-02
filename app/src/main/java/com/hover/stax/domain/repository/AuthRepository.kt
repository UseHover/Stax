package com.hover.stax.domain.repository

import com.hover.stax.data.remote.DataResult
import com.hover.stax.data.remote.NAuthResponse
import com.hover.stax.data.remote.NTokenResponse
import com.hover.stax.data.remote.dto.authorization.AuthResponse
import com.hover.stax.domain.model.TokenInfo

interface AuthRepository {

    suspend fun authorizeClient(idToken: String): DataResult<NAuthResponse>

    suspend fun fetchTokenInfo(code: String): DataResult<NTokenResponse>

    suspend fun refreshTokenInfo(): DataResult<NTokenResponse>

    suspend fun getTokenInfo(): TokenInfo?

    suspend fun saveTokenInfo(tokenInfo: TokenInfo)

    suspend fun deleteTokenInfo()
}