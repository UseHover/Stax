package com.hover.stax.ktor

import com.hover.stax.data.remote.dto.authorization.TokenRefresh
import com.hover.stax.data.remote.dto.authorization.TokenResponse
import com.hover.stax.preferences.DefaultTokenProvider
import com.hover.stax.preferences.TokenProvider
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
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