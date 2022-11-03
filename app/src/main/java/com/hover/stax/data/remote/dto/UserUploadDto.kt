package com.hover.stax.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Builds the user object used to create a user on Stax
 */
@Serializable
data class UserUploadDto(
    @SerialName("stax_user")
    val staxUser: UploadDto
)

@Serializable
data class UploadDto(
    @SerialName("device_id")
    val deviceId: String,
    @SerialName("email")
    val email: String,
    @SerialName("username")
    val username: String,
    @SerialName("token")
    val token: String
)
