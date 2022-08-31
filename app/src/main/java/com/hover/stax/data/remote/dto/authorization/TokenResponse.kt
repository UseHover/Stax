package com.hover.stax.data.remote.dto.authorization

import com.google.gson.annotations.SerializedName
import com.hover.stax.domain.model.TokenInfo

data class TokenResponse(

    @field:SerializedName("access_token")
    val accessToken: String,

    @field:SerializedName("refresh_token")
    val refreshToken: String,

    @field:SerializedName("scope")
    val scope: String,

    @field:SerializedName("created_at")
    val createdAt: Int,

    @field:SerializedName("token_type")
    val tokenType: String,

    @field:SerializedName("expires_in")
    val expiresIn: Int
)

fun TokenResponse.toTokenInfo(): TokenInfo {
    return TokenInfo(
        accessToken = accessToken,
        refreshToken = refreshToken,
        scope = scope,
        createdAt = createdAt,
        tokenType = tokenType,
        expiresIn = expiresIn
    )
}
