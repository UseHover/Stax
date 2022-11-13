package com.hover.stax.domain.repository

import com.hover.stax.data.remote.dto.StaxUserDto
import com.hover.stax.data.remote.dto.UserUpdateDto
import com.hover.stax.data.remote.dto.UserUploadDto
import com.hover.stax.data.remote.dto.authorization.AuthResponse
import com.hover.stax.data.remote.dto.authorization.TokenResponse

interface AuthRepository {

    suspend fun authorizeClient(idToken: String): AuthResponse

    suspend fun fetchTokenInfo(code: String): TokenResponse

    suspend fun revokeToken()

    suspend fun uploadUserToStax(userDTO: UserUploadDto): StaxUserDto

    suspend fun updateUser(email: String, userDTO: UserUpdateDto): StaxUserDto
}