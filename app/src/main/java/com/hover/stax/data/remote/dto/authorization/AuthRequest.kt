package com.hover.stax.data.remote.dto.authorization

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NAuthRequest(
    @SerialName("client_id")
    val clientId: String,
    @SerialName("redirect_uri")
    val redirectUri: String,
    @SerialName("response_type")
    val responseType: String,
    @SerialName("scope")
    val scope: String,
    @SerialName("stax_user")
    val staxUser: NStaxUser,
    @SerialName("token")
    val token: String
)

@Serializable
data class NStaxUser(
    @SerialName("device_id")
    val deviceId: String
)

@Serializable
data class NAuthResponse(
    @SerialName("redirect_uri")
    val redirectUri: NRedirectUri,
    @SerialName("status")
    val status: String
)

@Serializable
data class NRedirectUri(
    @SerialName("code")
    val code: String,
    @SerialName("action")
    val action: String
)