package com.hover.stax.data.remote

import com.hover.stax.data.remote.dto.StaxUserDto
import com.hover.stax.data.remote.dto.UserUpdateDto
import com.hover.stax.data.remote.dto.UserUploadDto
import com.hover.stax.data.remote.dto.authorization.AuthRequest
import com.hover.stax.data.remote.dto.authorization.AuthResponse
import com.hover.stax.data.remote.dto.authorization.RevokeTokenRequest
import com.hover.stax.data.remote.dto.authorization.TokenRefresh
import com.hover.stax.data.remote.dto.authorization.TokenRequest
import com.hover.stax.data.remote.dto.authorization.TokenResponse
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*

class StaxApi(
    private val client: HttpClient
) {

    suspend fun authorize(authRequest: AuthRequest): AuthResponse =
            client.post {
                url("${BASE_URL}authorize")
                setBody(authRequest)
            }.body()

    suspend fun fetchToken(tokenRequest: TokenRequest): TokenResponse =
            client.post {
                url("${BASE_URL}token")
                setBody(tokenRequest)
            }.body()

    suspend fun refreshToken(tokenRefresh: TokenRefresh): TokenResponse =
            client.post {
                url("${BASE_URL}token")
                setBody(tokenRefresh)
            }.body()

    suspend fun revokeToken(revokeToken: RevokeTokenRequest) =
            client.post {
                url("${BASE_URL}revoke")
                setBody(revokeToken)
            }

    suspend fun uploadUserToStax(userDTO: UserUploadDto): StaxUserDto =
            client.post {
                url("${BASE_URL}stax_users")
                setBody(userDTO)
            }.body()

    suspend fun updateUser(email: String, userDTO: UserUpdateDto): StaxUserDto =
            client.post {
                url("${BASE_URL}stax_users/$email")
                setBody(userDTO)
            }.body()

    suspend fun getRewardPoints(email: String): StaxUserDto =
            client.post {
                url("$BASE_URL/api/rewards/reward_points/$email")
            }.body()

    companion object {
        const val BASE_URL = "https://stage.usehover.com/stax_api/"
    }
}