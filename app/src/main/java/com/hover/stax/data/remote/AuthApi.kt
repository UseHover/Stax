package com.hover.stax.data.remote

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
                url("${BASE_URL}authorize")
                setBody(authRequest)
            }.body()
        }
 
    suspend fun fetchToken(tokenRequest: NTokenRequest): DataResult<NTokenResponse> =
        dataResultSafeApiCall {
            client.post {
                url("${BASE_URL}token")
                setBody(tokenRequest)
            }.body()
        }

    suspend fun refreshToken(tokenRefresh: NTokenRefresh): DataResult<NTokenResponse> =
        dataResultSafeApiCall {
            client.post {
                url("${BASE_URL}token")
                setBody(tokenRefresh)
            }.body()
        }

    companion object {
        const val BASE_URL = "https://stage.usehover.com/stax_api/"
    }
}