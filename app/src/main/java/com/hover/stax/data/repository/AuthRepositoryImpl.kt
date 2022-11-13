package com.hover.stax.data.repository

import android.content.Context
import com.hover.sdk.api.Hover
import com.hover.stax.data.remote.StaxApi
import com.hover.stax.data.remote.dto.StaxUserDto
import com.hover.stax.data.remote.dto.UserUpdateDto
import com.hover.stax.data.remote.dto.UserUploadDto
import com.hover.stax.data.remote.dto.authorization.*
import com.hover.stax.domain.repository.AuthRepository
import com.hover.stax.ktor.EnvironmentProvider
import com.hover.stax.preferences.DefaultTokenProvider
import com.hover.stax.preferences.TokenProvider
import kotlinx.coroutines.flow.firstOrNull

private const val AUTHORIZATION = "authorization_code"
private const val REFRESH = "refresh_token"
private const val RESPONSE_TYPE = "code"
private const val SCOPE = "write"

class AuthRepositoryImpl(
    private val context: Context,
    private val staxApi: StaxApi,
    private val tokenProvider: TokenProvider,
    private val environmentProvider: EnvironmentProvider
) : AuthRepository {

    override suspend fun authorizeClient(idToken: String): AuthResponse {
        val authRequest = AuthRequest(
            clientId = environmentProvider.get().clientId,
            redirectUri = environmentProvider.get().redirectUri,
            responseType = RESPONSE_TYPE,
            scope = SCOPE,
            staxUser = StaxUser(
                deviceId = Hover.getDeviceId(context)
            ),
            token = idToken,
        )

        return staxApi.authorize(authRequest)
    }

    override suspend fun fetchTokenInfo(code: String): TokenResponse {
        val tokenRequest = TokenRequest(
            clientId = environmentProvider.get().clientId,
            clientSecret = environmentProvider.get().clientSecret,
            code = code,
            grantType = AUTHORIZATION,
            redirectUri = environmentProvider.get().redirectUri
        )

        return staxApi.fetchToken(tokenRequest)
    }

    override suspend fun refreshTokenInfo(): TokenResponse {
        val tokenRequest = TokenRefresh(
            clientId = environmentProvider.get().clientId,
            clientSecret = environmentProvider.get().clientSecret,
            refreshToken = tokenProvider.fetch(DefaultTokenProvider.REFRESH_TOKEN).firstOrNull()
                .toString(),
            grantType = REFRESH,
            redirectUri = environmentProvider.get().redirectUri
        )

        return staxApi.refreshToken(tokenRequest)
    }

    override suspend fun revokeToken() {
        val revokeToken = RevokeTokenRequest(
            clientId = environmentProvider.get().clientId,
            clientSecret = environmentProvider.get().clientSecret,
            token = tokenProvider.fetch(DefaultTokenProvider.ACCESS_TOKEN).firstOrNull().toString()
        )

        staxApi.revokeToken(revokeToken)
    }

    override suspend fun uploadUserToStax(userDTO: UserUploadDto): StaxUserDto =
        staxApi.uploadUserToStax(userDTO = userDTO)

    override suspend fun updateUser(email: String, userDTO: UserUpdateDto): StaxUserDto =
        staxApi.updateUser(email = email, userDTO = userDTO)
}