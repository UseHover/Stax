package com.hover.stax.data.remote.dto.authorization

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TokenRequest(
    @SerialName("client_id")
    val clientId: String,
    @SerialName("client_secret")
    val clientSecret: String,
    val code: String,
    @SerialName("grant_type")
    val grantType: String,
    @SerialName("redirect_uri")
    val redirectUri: String
)

@Serializable
data class TokenRefresh(
    @SerialName("client_id")
    val clientId: String,
    @SerialName("client_secret")
    val clientSecret: String,
    @SerialName("refresh_token")
    val refreshToken: String,
    @SerialName("grant_type")
    val grantType: String,
    @SerialName("redirect_uri")
    val redirectUri: String
)

@Serializable
data class RevokeTokenRequest(
    @SerialName("client_id")
    val clientId: String,
    @SerialName("client_secret")
    val clientSecret: String,
    val token: String
)