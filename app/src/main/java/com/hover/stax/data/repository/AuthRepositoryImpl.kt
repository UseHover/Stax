package com.hover.stax.data.repository

import android.content.Context
import com.hover.stax.R
import com.hover.stax.data.local.auth.AuthRepo
import com.hover.stax.data.remote.StaxApi
import com.hover.stax.data.remote.dto.authorization.TokenRefreshRequest
import com.hover.stax.data.remote.dto.authorization.TokenRequest
import com.hover.stax.data.remote.dto.authorization.toTokenInfo
import com.hover.stax.domain.model.TokenInfo
import com.hover.stax.domain.repository.AuthRepository

private const val AUTHORIZATION = "authorization_code"
private const val REFRESH = "refresh_token"

class AuthRepositoryImpl(private val authRepo: AuthRepo, private val staxApi: StaxApi, private val context: Context) : AuthRepository {

    override suspend fun fetchTokenInfo(code: String): TokenInfo {
        val tokenRequest = TokenRequest(
            code = code,
            grantType = AUTHORIZATION,
            clientSecret = context.getString(R.string.client_secret),
            redirectUri = context.getString(R.string.redirect_uri),
            clientId = context.getString(R.string.client_uid)
        )

        val tokenResponse = staxApi.fetchToken(tokenRequest)
        val tokenInfo = tokenResponse.toTokenInfo().also {
            saveTokenInfo(it)
        }

        return tokenInfo
    }

    override suspend fun refreshTokenInfo(): TokenInfo {
        val tokenRequest = TokenRefreshRequest(
            refreshToken = authRepo.getTokenInfo()!!.refreshToken,
            grantType = REFRESH,
            clientSecret = context.getString(R.string.client_secret),
            redirectUri = context.getString(R.string.redirect_uri),
            clientId = context.getString(R.string.client_uid)
        )

        val tokenResponse = staxApi.refreshToken(tokenRequest)
        val tokenInfo = tokenResponse.toTokenInfo().also {
            saveTokenInfo(it)
        }

        return tokenInfo
    }

    override suspend fun getTokenInfo(): TokenInfo? = authRepo.getTokenInfo()

    override suspend fun saveTokenInfo(tokenInfo: TokenInfo) = authRepo.saveTokenInfo(tokenInfo)

    override suspend fun deleteTokenInfo() = authRepo.deleteTokenInfo()
}