package com.hover.stax.data.remote.dto.authorization

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TokenData(
    @SerialName("iss")
    val iss: String,
    @SerialName("iat")
    val iat: Int,
    @SerialName("user")
    val user: String,
    @SerialName("jti")
    val jti: String
)
