package com.hover.stax.data.remote.dto.authorization

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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