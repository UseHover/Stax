package com.hover.stax.ktor

import com.hover.stax.preferences.DefaultTokenProvider
import com.hover.stax.preferences.TokenProvider
import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.observer.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.serialization.json.Json
import timber.log.Timber

class KtorClientFactory(
    private val tokenProvider: TokenProvider
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

        install(ResponseObserver) {
            onResponse { response ->
                Timber.d("HTTP status:", "${response.status.value}")
            }
        }

        install(DefaultRequest) {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
        }

        expectSuccess = true

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
            }
        }
    }
}