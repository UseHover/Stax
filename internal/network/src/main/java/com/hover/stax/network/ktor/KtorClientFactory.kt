package com.hover.stax.network.ktor

import com.hover.stax.datastore.DefaultTokenProvider
import com.hover.stax.datastore.TokenProvider
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