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
package com.hover.stax.ktor

import com.hover.stax.data.remote.dto.authorization.TokenRefresh
import com.hover.stax.data.remote.dto.authorization.TokenResponse
import com.hover.stax.preferences.DefaultTokenProvider
import com.hover.stax.preferences.TokenProvider
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.addDefaultResponseValidation
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.SIMPLE
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.serialization.json.Json

private const val REFRESH = "refresh_token"

class KtorClientFactory(
    private val tokenProvider: TokenProvider,
    private val environmentProvider: EnvironmentProvider
) {

    fun create(engine: HttpClientEngine) = HttpClient(engine) {

        install(ContentNegotiation) {
            json(
                Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                }
            )
        }

        install(Logging) {
            logger = Logger.SIMPLE
            level = LogLevel.ALL
        }

        expectSuccess = true

        install(DefaultRequest) {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
        }

        addDefaultResponseValidation()

        install(Auth) {
            bearer {

                loadTokens {

                    // Load tokens from datastore
                    val accessToken: String =
                        tokenProvider.fetch(DefaultTokenProvider.ACCESS_TOKEN).firstOrNull()
                            .toString()

                    val refreshToken =
                        tokenProvider.fetch(DefaultTokenProvider.REFRESH_TOKEN).firstOrNull()
                            .toString()

                    BearerTokens(accessToken, refreshToken)
                }

                refreshTokens {

                    // Refresh token from API
                    val response: TokenResponse =
                        client.post {
                            url("${environmentProvider.get().baseUrl}token")
                            markAsRefreshTokenRequest()
                            setBody(
                                TokenRefresh(
                                    clientId = environmentProvider.get().clientId,
                                    clientSecret = environmentProvider.get().clientSecret,
                                    refreshToken = tokenProvider.fetch(DefaultTokenProvider.REFRESH_TOKEN)
                                        .firstOrNull()
                                        .toString(),
                                    grantType = REFRESH,
                                    redirectUri = environmentProvider.get().redirectUri
                                )
                            )
                        }.body()

                    // Save tokens to datastore
                    tokenProvider.update(
                        key = DefaultTokenProvider.ACCESS_TOKEN,
                        token = response.accessToken
                    )

                    tokenProvider.update(
                        key = DefaultTokenProvider.REFRESH_TOKEN,
                        token = response.refreshToken.toString()
                    )

                    // Load tokens from datastore
                    val accessToken: String =
                        tokenProvider.fetch(DefaultTokenProvider.ACCESS_TOKEN).firstOrNull()
                            .toString()

                    val refreshToken =
                        tokenProvider.fetch(DefaultTokenProvider.REFRESH_TOKEN).firstOrNull()
                            .toString()

                    BearerTokens(accessToken, refreshToken)
                }
            }
        }
    }
}