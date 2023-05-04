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
package com.hover.stax.data.remote

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.appmattus.kotlinfixture.decorator.nullability.NeverNullStrategy
import com.appmattus.kotlinfixture.decorator.nullability.nullabilityStrategy
import com.appmattus.kotlinfixture.decorator.optional.NeverOptionalStrategy
import com.appmattus.kotlinfixture.decorator.optional.optionalStrategy
import com.appmattus.kotlinfixture.kotlinFixture
import com.google.common.truth.Truth.assertThat
import com.hover.stax.data.remote.dto.Attributes
import com.hover.stax.data.remote.dto.Data
import com.hover.stax.data.remote.dto.StaxUserDto
import com.hover.stax.data.remote.dto.UserUpdateDto
import com.hover.stax.data.remote.dto.UserUploadDto
import com.hover.stax.data.remote.dto.authorization.AuthRequest
import com.hover.stax.data.remote.dto.authorization.AuthResponse
import com.hover.stax.data.remote.dto.authorization.RedirectUri
import com.hover.stax.data.remote.dto.authorization.RevokeTokenRequest
import com.hover.stax.data.remote.dto.authorization.TokenRequest
import com.hover.stax.data.remote.dto.authorization.TokenResponse
import com.hover.stax.ktor.EnvironmentProvider
import com.hover.stax.ktor.KtorClientFactory
import com.hover.stax.datastore.DefaultTokenProvider
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.mockk.mockk
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Test

class AuthApiTest {

    private val fixture = kotlinFixture {
        nullabilityStrategy(NeverNullStrategy)
        optionalStrategy(NeverOptionalStrategy)
    }

    private var testDataStore: DataStore<Preferences> = mockk(relaxed = true)
    private var environmentProvider: EnvironmentProvider = mockk(relaxed = true)

    private val userDTO = fixture<UserUploadDto>()

    @Test(expected = ServerResponseException::class)
    fun `test server error is thrown when a server exception occurs`() {
        val authRequest = fixture<AuthRequest>()
        val mockEngine = MockEngine {
            delay(500)
            respondError(HttpStatusCode.InternalServerError)
        }

        val api = StaxApi(
            KtorClientFactory(
                DefaultTokenProvider(testDataStore),
                environmentProvider
            ).create(mockEngine),
            environmentProvider
        )

        runBlocking { api.authorize(authRequest) }
    }

    @Test
    fun `test authorization is successful when google token is correct`() {
        val authRequest = fixture<AuthRequest>()
        val authResponse = AuthResponse(
            redirectUri = RedirectUri(
                code = "76233958-77a5-43fc-9b3f-ca2d0d0ce54f",
                action = "896fa22f-d273-475b-80ee-e7553d9f9a15"
            ),
            status = "152b1eff-f55a-4b57-9f45-0eaf5314d3c5"
        )
        val response = """{
            "redirect_uri" : {
                "code" : "76233958-77a5-43fc-9b3f-ca2d0d0ce54f",
                "action" : "896fa22f-d273-475b-80ee-e7553d9f9a15"
            },
            "status" : "152b1eff-f55a-4b57-9f45-0eaf5314d3c5"
        }
        """.trimIndent()

        val mockEngine = MockEngine {
            respond(
                content = response,
                status = HttpStatusCode.OK,
                headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val api = StaxApi(
            KtorClientFactory(
                DefaultTokenProvider(testDataStore),
                environmentProvider
            ).create(mockEngine),
            environmentProvider
        )

        val actual = runBlocking { api.authorize(authRequest) }

        assertThat(actual).isEqualTo(authResponse)
    }

    @Test
    fun `test token request is successful when authorization code is correct`() {
        val tokenRequest = fixture<TokenRequest>()
        val tokenResponse = TokenResponse(
            accessToken = "eyJpc3MiOiJTdGF4IE1vYmlsZSIsImlhdCI6MTY2MTc4NTkxMSwianRpIjoiZDFiYmEwZGItOWNiOC00OWUxLWEzNTItODk3NzYxMDhjYWZkIiwidXNlciI6IntcImRhdGFcIjp7X",
            refreshToken = "A3_rBRCZUtL3i0h-y2_HWtw1icW9ZUaeH9Fq7R42GXg",
            scope = "write",
            createdAt = 1661785911,
            tokenType = "Bearer",
            expiresIn = 7200
        )
        val response = """{
                "access_token" : "eyJpc3MiOiJTdGF4IE1vYmlsZSIsImlhdCI6MTY2MTc4NTkxMSwianRpIjoiZDFiYmEwZGItOWNiOC00OWUxLWEzNTItODk3NzYxMDhjYWZkIiwidXNlciI6IntcImRhdGFcIjp7X",
                "refresh_token" : "A3_rBRCZUtL3i0h-y2_HWtw1icW9ZUaeH9Fq7R42GXg",
                "scope" : "write",
                "created_at" : 1661785911,
                "token_type" : "Bearer",
                "expires_in" :  7200
        }
        """.trimIndent()

        val mockEngine = MockEngine {
            respond(
                content = response,
                status = HttpStatusCode.OK,
                headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val api = StaxApi(
            KtorClientFactory(
                DefaultTokenProvider(testDataStore),
                environmentProvider
            ).create(mockEngine),
            environmentProvider
        )

        val actual = runBlocking { api.fetchToken(tokenRequest) }

        assertThat(actual).isEqualTo(tokenResponse)
    }

    @Test
    fun `test token revoke is successful when revoke token is correct`() {
        val tokenRevoke = fixture<RevokeTokenRequest>()

        val response = """{}""".trimIndent()

        val mockEngine = MockEngine {
            respond(
                content = response,
                status = HttpStatusCode.OK,
                headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val api = StaxApi(
            KtorClientFactory(
                DefaultTokenProvider(testDataStore),
                environmentProvider
            ).create(mockEngine),
            environmentProvider
        )

        val actual = runBlocking { api.revokeToken(tokenRevoke) }

        assertThat(actual).isInstanceOf(HttpResponse::class.java)
    }

    @Test
    fun `test upload user is successful when passing correct user details`() {
        val staxUserDto = StaxUserDto(
            data = Data(
                attributes = Attributes(
                    bountyTotal = 0,
                    refereeId = null,
                    devices = listOf(
                        "2c4d31caa083bd2d",
                        "36eb3bf6f050f1de",
                        "a326a52fbed87a5b",
                        "fc4f493826aa55fc"
                    ),
                    transactionCount = 1,
                    createdAt = "2021-10-07T11:29:42Z",
                    id = 686487,
                    isVerifiedMapper = false,
                    email = "juma@usehover.com",
                    username = "Juma Allan",
                    marketingOptedIn = false,
                    totalPoints = 0
                ),
                id = "686487",
                type = "stax_user"
            )
        )
        val response = """{
            "data": {
                "id": "686487",
                "type": "stax_user",
                "attributes": {
                    "id": 686487,
                    "email": "juma@usehover.com",
                    "username": "Juma Allan",
                    "devices": [
                        "2c4d31caa083bd2d",
                        "36eb3bf6f050f1de",
                        "a326a52fbed87a5b",
                        "fc4f493826aa55fc"
                    ],
                    "is_verified_mapper": false,
                    "marketing_opted_in": false,
                    "transaction_count": 1,
                    "bounty_total": 0,
                    "created_at": "2021-10-07T11:29:42Z",
                    "referee_id": null,
                    "total_points" : 0
                }
            }
        }
        """.trimIndent()

        val mockEngine = MockEngine {
            respond(
                content = response,
                status = HttpStatusCode.OK,
                headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val api = StaxApi(
            KtorClientFactory(
                DefaultTokenProvider(testDataStore),
                environmentProvider
            ).create(mockEngine),
            environmentProvider
        )

        val actual = runBlocking { api.uploadUserToStax(userDTO) }

        assertThat(actual).isEqualTo(staxUserDto)
    }

    @Test(expected = ServerResponseException::class)
    fun `test server error is thrown when a server exception occurs during user upload`() {
        val mockEngine = MockEngine {
            delay(500)
            respondError(HttpStatusCode.InternalServerError)
        }

        val api = StaxApi(
            KtorClientFactory(
                DefaultTokenProvider(testDataStore),
                environmentProvider
            ).create(mockEngine),
            environmentProvider
        )

        runBlocking { api.uploadUserToStax(userDTO) }
    }

    @Test
    fun `test update user is successful when passing correct user details`() {
        val userDTO = fixture<UserUpdateDto>()
        val email = fixture<String>()
        val staxUserDto = StaxUserDto(
            data = Data(
                attributes = Attributes(
                    bountyTotal = 0,
                    refereeId = null,
                    devices = listOf(
                        "2c4d31caa083bd2d",
                        "36eb3bf6f050f1de",
                        "a326a52fbed87a5b",
                        "fc4f493826aa55fc"
                    ),
                    transactionCount = 1,
                    createdAt = "2021-10-07T11:29:42Z",
                    id = 686487,
                    isVerifiedMapper = false,
                    email = "juma@usehover.com",
                    username = "Juma Allan",
                    marketingOptedIn = false,
                    totalPoints = 0
                ),
                id = "686487",
                type = "stax_user"
            )
        )
        val response = """{
            "data": {
                "id": "686487",
                "type": "stax_user",
                "attributes": {
                    "id": 686487,
                    "email": "juma@usehover.com",
                    "username": "Juma Allan",
                    "devices": [
                        "2c4d31caa083bd2d",
                        "36eb3bf6f050f1de",
                        "a326a52fbed87a5b",
                        "fc4f493826aa55fc"
                    ],
                    "is_verified_mapper": false,
                    "marketing_opted_in": false,
                    "transaction_count": 1,
                    "bounty_total": 0,
                    "created_at": "2021-10-07T11:29:42Z",
                    "referee_id": null,
                    "total_points" : 0
                }
            }
        }
        """.trimIndent()

        val mockEngine = MockEngine {
            respond(
                content = response,
                status = HttpStatusCode.OK,
                headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val api = StaxApi(
            KtorClientFactory(
                DefaultTokenProvider(testDataStore),
                environmentProvider
            ).create(mockEngine),
            environmentProvider
        )

        val actual = runBlocking { api.updateUser(email = email, userDTO = userDTO) }

        assertThat(actual).isEqualTo(staxUserDto)
    }
}