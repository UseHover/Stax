package com.hover.stax.domain.repository

import com.hover.stax.data.remote.dto.StaxUserDto
import com.hover.stax.data.remote.dto.UserUpdateDto
import com.hover.stax.data.remote.dto.UserUploadDto
import com.hover.stax.data.remote.dto.authorization.NAuthResponse
import com.hover.stax.data.remote.dto.authorization.NTokenResponse

interface AuthRepository {

    suspend fun authorizeClient(idToken: String): NAuthResponse

    suspend fun fetchTokenInfo(code: String): NTokenResponse

    suspend fun refreshTokenInfo(): NTokenResponse

    suspend fun revokeToken(): NTokenResponse

    suspend fun uploadUserToStax(userDTO: UserUploadDto): StaxUserDto

    suspend fun updateUser(email: String, userDTO: UserUpdateDto): StaxUserDto
}