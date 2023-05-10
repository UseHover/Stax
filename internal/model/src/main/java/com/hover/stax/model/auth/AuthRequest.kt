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
package com.hover.stax.model.auth

import kotlinx.serialization.SerialName

@Serializable
data class AuthRequest(
    @SerialName("client_id")
    val clientId: String,
    @SerialName("redirect_uri")
    val redirectUri: String,
    @SerialName("response_type")
    val responseType: String,
    @SerialName("scope")
    val scope: String,
    @SerialName("stax_user")
    val staxUser: StaxUser,
    @SerialName("token")
    val token: String
)

@Serializable
data class StaxUser(
    @SerialName("device_id")
    val deviceId: String
)

@Serializable
data class AuthResponse(
    @SerialName("redirect_uri")
    val redirectUri: RedirectUri,
    @SerialName("status")
    val status: String
)

@Serializable
data class RedirectUri(
    @SerialName("code")
    val code: String,
    @SerialName("action")
    val action: String
)