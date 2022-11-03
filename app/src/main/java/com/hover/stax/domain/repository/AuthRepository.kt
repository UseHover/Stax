package com.hover.stax.domain.repository

import com.hover.stax.data.remote.DataResult
import com.hover.stax.data.remote.dto.StaxUserDto
import com.hover.stax.data.remote.dto.UserUpdateDto
import com.hover.stax.data.remote.dto.UserUploadDto
import com.hover.stax.data.remote.dto.authorization.NAuthResponse
import com.hover.stax.data.remote.dto.authorization.NTokenResponse

interface AuthRepository {

    suspend fun authorizeClient(idToken: String): DataResult<NAuthResponse>

    suspend fun fetchTokenInfo(code: String): DataResult<NTokenResponse>

    suspend fun refreshTokenInfo(): DataResult<NTokenResponse>

    suspend fun revokeToken(): DataResult<NTokenResponse>

    suspend fun uploadUserToStax(userDTO: UserUploadDto): DataResult<StaxUserDto>

    suspend fun updateUser(email: String, userDTO: UserUpdateDto): DataResult<StaxUserDto>
}