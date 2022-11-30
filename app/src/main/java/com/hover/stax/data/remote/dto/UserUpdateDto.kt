package com.hover.stax.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserUpdateDto(
    @SerialName("stax_user")
    val staxUser: UpdateDto
)

@Serializable
data class UpdateDto(
    @SerialName("is_mapper")
    val isMapper: Boolean? = null,
    @SerialName("marketing_opted_in")
    val marketingOptedIn: Boolean? = null,
    @SerialName("email")
    val email: String
)