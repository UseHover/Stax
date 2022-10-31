package com.hover.stax.data.remote

import com.hover.stax.data.remote.dto.authorization.TokenRefreshRequest
import com.hover.stax.data.remote.dto.authorization.TokenRequest
import com.hover.stax.ktor.dataResultSafeApiCall
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*

class AuthApi(
    private val client: HttpClient
) {

    suspend fun authorize(authRequest: NAuthRequest): DataResult<NAuthResponse> =
        dataResultSafeApiCall {
            client.post {
                url("https://stage.usehover.com/stax_api/authorize")
                setBody(authRequest)
            }.body()
        }

    suspend fun fetchToken(tokenRequest: TokenRequest) = dataResultSafeApiCall {
        client.post {
            url("https://stage.usehover.com/stax_api/token")
            setBody(tokenRequest)
        }.body()
    }

    suspend fun refreshToken(refreshRequest: TokenRefreshRequest) = dataResultSafeApiCall {
        client.post {
            url("https://stage.usehover.com/stax_api/token")
            setBody(refreshRequest)
        }.body()
    }
}