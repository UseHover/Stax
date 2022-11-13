package com.hover.stax.data.remote

import com.hover.stax.data.remote.dto.StaxUserDto
import com.hover.stax.data.remote.dto.UserUpdateDto
import com.hover.stax.data.remote.dto.UserUploadDto
import com.hover.stax.data.remote.dto.authorization.*
import com.hover.stax.ktor.EnvironmentProvider
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*

class StaxApi(
    private val client: HttpClient,
    private val environmentProvider: EnvironmentProvider
) {

    suspend fun authorize(authRequest: AuthRequest): AuthResponse =
        client.post {
            url("${environmentProvider.get().baseUrl}authorize")
            setBody(authRequest)
        }.body()

    suspend fun fetchToken(tokenRequest: TokenRequest): TokenResponse =
        client.post {
            url("${environmentProvider.get().baseUrl}token")
            setBody(tokenRequest)
        }.body()

    suspend fun revokeToken(revokeToken: RevokeTokenRequest) =
        client.post {
            url("${environmentProvider.get().baseUrl}revoke")
            setBody(revokeToken)
        }

    suspend fun uploadUserToStax(userDTO: UserUploadDto): StaxUserDto =
        client.post {
            url("${environmentProvider.get().baseUrl}stax_users")
            setBody(userDTO)
        }.body()

    suspend fun updateUser(email: String, userDTO: UserUpdateDto): StaxUserDto =
        client.post {
            url("${environmentProvider.get().baseUrl}stax_users/$email")
            setBody(userDTO)
        }.body()

    suspend fun getRewardPoints(email: String): StaxUserDto =
        client.post {
            url("${environmentProvider.get().baseUrl}/api/rewards/reward_points/$email")
        }.body()
}