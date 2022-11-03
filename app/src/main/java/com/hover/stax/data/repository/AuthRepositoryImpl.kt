package com.hover.stax.data.repository

import android.content.Context
import com.hover.sdk.api.Hover
import com.hover.stax.R
import com.hover.stax.data.remote.DataResult
import com.hover.stax.data.remote.auth.StaxApi
import com.hover.stax.data.remote.dto.StaxUserDto
import com.hover.stax.data.remote.dto.UserUpdateDto
import com.hover.stax.data.remote.dto.UserUploadDto
import com.hover.stax.data.remote.dto.authorization.NAuthRequest
import com.hover.stax.data.remote.dto.authorization.NAuthResponse
import com.hover.stax.data.remote.dto.authorization.NRevokeTokenRequest
import com.hover.stax.data.remote.dto.authorization.NStaxUser
import com.hover.stax.data.remote.dto.authorization.NTokenRefresh
import com.hover.stax.data.remote.dto.authorization.NTokenRequest
import com.hover.stax.data.remote.dto.authorization.NTokenResponse
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

    override suspend fun authorizeClient(idToken: String): DataResult<NAuthResponse> {
        val authRequest = NAuthRequest(
            clientId = context.getString(R.string.client_uid),
            redirectUri = context.getString(R.string.redirect_uri),
            responseType = RESPONSE_TYPE,
            scope = SCOPE,
            staxUser = NStaxUser(
                deviceId = Hover.getDeviceId(context)
            ),
            token = idToken,
        )

        return staxApi.authorize(authRequest)
    }

    override suspend fun fetchTokenInfo(code: String): DataResult<NTokenResponse> {
        val tokenRequest = NTokenRequest(
            clientId = context.getString(R.string.client_uid),
            clientSecret = context.getString(R.string.client_secret),
            code = code,
            grantType = AUTHORIZATION,
            redirectUri = context.getString(R.string.redirect_uri)
        )

        return staxApi.fetchToken(tokenRequest)
    }

    override suspend fun refreshTokenInfo(): DataResult<NTokenResponse> {
        val tokenRequest = NTokenRefresh(
            clientId = context.getString(R.string.client_uid),
            clientSecret = context.getString(R.string.client_secret),
            refreshToken = tokenProvider.fetch(DefaultTokenProvider.REFRESH_TOKEN).firstOrNull()
                .toString(),
            grantType = REFRESH,
            redirectUri = context.getString(R.string.redirect_uri),
        )

        return staxApi.refreshToken(tokenRequest)
    }

    override suspend fun revokeToken(): DataResult<NTokenResponse> {
        val revokeToken = NRevokeTokenRequest(
            clientId = context.getString(R.string.client_uid),
            clientSecret = context.getString(R.string.client_secret),
            token = tokenProvider.fetch(DefaultTokenProvider.ACCESS_TOKEN).firstOrNull().toString()
        )

        return staxApi.revokeToken(revokeToken)
    }

    override suspend fun uploadUserToStax(userDTO: UserUploadDto): DataResult<StaxUserDto> =
        staxApi.uploadUserToStax(userDTO = userDTO)

    override suspend fun updateUser(email: String, userDTO: UserUpdateDto): DataResult<StaxUserDto>  = staxApi.updateUser(
        email = email,
        userDTO = userDTO
    )
}