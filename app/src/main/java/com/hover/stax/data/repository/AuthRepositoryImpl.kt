/*
 * Copyright 2022 Stax
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hover.stax.data.repository

import android.content.Context
import com.hover.sdk.api.Hover
import com.hover.stax.data.remote.StaxApi
import com.hover.stax.data.remote.dto.StaxUserDto
import com.hover.stax.data.remote.dto.UserUpdateDto
import com.hover.stax.data.remote.dto.UserUploadDto
import com.hover.stax.data.remote.dto.authorization.AuthRequest
import com.hover.stax.data.remote.dto.authorization.AuthResponse
import com.hover.stax.data.remote.dto.authorization.RevokeTokenRequest
import com.hover.stax.data.remote.dto.authorization.StaxUser
import com.hover.stax.data.remote.dto.authorization.TokenRequest
import com.hover.stax.data.remote.dto.authorization.TokenResponse
import com.hover.stax.domain.repository.AuthRepository
import com.hover.stax.ktor.EnvironmentProvider
import com.hover.stax.internal.datastore.DefaultTokenProvider
import com.hover.stax.internal.datastore.TokenProvider
import kotlinx.coroutines.flow.firstOrNull

private const val AUTHORIZATION = "authorization_code"
private const val RESPONSE_TYPE = "code"
private const val SCOPE = "read write"

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