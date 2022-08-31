package com.hover.stax.data.remote.dto

import com.google.gson.annotations.SerializedName

data class UserUpdateDto(

    @SerializedName("stax_user")
    val staxUser: UpdateDto
)

data class UpdateDto(

    @SerializedName("is_mapper")
    val isMapper: Boolean? = null,

    @SerializedName("marketing_opted_in")
    val marketingOptedIn: Boolean? = null,

    @SerializedName("email")
    val email: String
)