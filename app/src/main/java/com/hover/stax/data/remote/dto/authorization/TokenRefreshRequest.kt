package com.hover.stax.data.remote.dto.authorization

import com.google.gson.annotations.SerializedName

data class TokenRefreshRequest(

    @field:SerializedName("refresh_token")
    val refreshToken: String,

    @field:SerializedName("grant_type")
    val grantType: String,

    @field:SerializedName("client_secret")
    val clientSecret: String,

    @field:SerializedName("redirect_uri")
    val redirectUri: String,

    @field:SerializedName("client_id")
    val clientId: String
)