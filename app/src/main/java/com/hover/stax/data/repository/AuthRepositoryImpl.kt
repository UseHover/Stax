package com.hover.stax.data.repository

import android.content.Context
import com.hover.sdk.api.Hover
import com.hover.stax.R
import com.hover.stax.data.local.auth.AuthRepo
import com.hover.stax.data.remote.AuthApi
import com.hover.stax.data.remote.DataResult
import com.hover.stax.data.remote.NAuthRequest
import com.hover.stax.data.remote.NAuthResponse
import com.hover.stax.data.remote.NStaxUser
import com.hover.stax.data.remote.NTokenRefresh
import com.hover.stax.data.remote.NTokenRequest
import com.hover.stax.data.remote.NTokenResponse
import com.hover.stax.domain.model.TokenInfo
import com.hover.stax.domain.repository.AuthRepository

private const val AUTHORIZATION = "authorization_code"
private const val REFRESH = "refresh_token"
private const val RESPONSE_TYPE = "code"
private const val SCOPE = "write"

class AuthRepositoryImpl(
    private val context: Context,
    private val authApi: AuthApi,
    private val authRepo: AuthRepo
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

        return authApi.authorize(authRequest)
    }

    override suspend fun fetchTokenInfo(code: String): DataResult<NTokenResponse> {
        val tokenRequest = NTokenRequest(
            clientId = context.getString(R.string.client_uid),
            clientSecret = context.getString(R.string.client_secret),
            code = code,
            grantType = AUTHORIZATION,
            redirectUri = context.getString(R.string.redirect_uri)
        )

        return authApi.fetchToken(tokenRequest)
    }

    override suspend fun refreshTokenInfo(): DataResult<NTokenResponse> {
        val tokenRequest = NTokenRefresh(
            clientId = context.getString(R.string.client_uid),
            clientSecret = context.getString(R.string.client_secret),
            refreshToken = authRepo.getTokenInfo()!!.refreshToken,
            grantType = REFRESH,
            redirectUri = context.getString(R.string.redirect_uri),
        )

        return authApi.refreshToken(tokenRequest)
    }

    override suspend fun getTokenInfo(): TokenInfo? = authRepo.getTokenInfo()

    override suspend fun saveTokenInfo(tokenInfo: TokenInfo) = authRepo.saveTokenInfo(tokenInfo)

    override suspend fun deleteTokenInfo() = authRepo.deleteTokenInfo()
}