package com.hover.stax.data.repository

import android.content.Context
import com.hover.sdk.api.Hover
import com.hover.stax.R
import com.hover.stax.data.remote.StaxApi
import com.hover.stax.data.remote.dto.StaxUserDto
import com.hover.stax.data.remote.dto.UserUpdateDto
import com.hover.stax.data.remote.dto.UserUploadDto
import com.hover.stax.data.remote.dto.authorization.AuthRequest
import com.hover.stax.data.remote.dto.authorization.AuthResponse
import com.hover.stax.data.remote.dto.authorization.RevokeTokenRequest
import com.hover.stax.data.remote.dto.authorization.StaxUser
import com.hover.stax.data.remote.dto.authorization.TokenRefresh
import com.hover.stax.data.remote.dto.authorization.TokenRequest
import com.hover.stax.data.remote.dto.authorization.TokenResponse
import com.hover.stax.domain.repository.AuthRepository
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
        private val tokenProvider: TokenProvider
) : AuthRepository {

    override suspend fun authorizeClient(idToken: String): AuthResponse {
        val authRequest = AuthRequest(
                clientId = context.getString(R.string.client_uid),
                redirectUri = context.getString(R.string.redirect_uri),
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
                clientId = context.getString(R.string.client_uid),
                clientSecret = context.getString(R.string.client_secret),
                code = code,
                grantType = AUTHORIZATION,
                redirectUri = context.getString(R.string.redirect_uri)
        )

        return staxApi.fetchToken(tokenRequest)
    }

    override suspend fun refreshTokenInfo(): TokenResponse {
        val tokenRequest = TokenRefresh(
                clientId = context.getString(R.string.client_uid),
                clientSecret = context.getString(R.string.client_secret),
                refreshToken = tokenProvider.fetch(DefaultTokenProvider.REFRESH_TOKEN).firstOrNull()
                        .toString(),
                grantType = REFRESH,
                redirectUri = context.getString(R.string.redirect_uri),
        )

        return staxApi.refreshToken(tokenRequest)
    }

    override suspend fun revokeToken() {
        val revokeToken = RevokeTokenRequest(
                clientId = context.getString(R.string.client_uid),
                clientSecret = context.getString(R.string.client_secret),
                token = tokenProvider.fetch(DefaultTokenProvider.ACCESS_TOKEN).firstOrNull().toString()
        )

        staxApi.revokeToken(revokeToken)
    }

    override suspend fun uploadUserToStax(userDTO: UserUploadDto): StaxUserDto =
            staxApi.uploadUserToStax(userDTO = userDTO)

    override suspend fun updateUser(email: String, userDTO: UserUpdateDto): StaxUserDto =
            staxApi.updateUser(email = email, userDTO = userDTO)
}