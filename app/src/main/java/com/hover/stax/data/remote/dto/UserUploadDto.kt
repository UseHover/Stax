package com.hover.stax.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Builds the user object used to create a user on Stax
 */
data class UserUploadDto(

	@SerializedName("stax_user")
	val staxUser: UploadDto
)

data class UploadDto(

	@SerializedName("device_id")
	val deviceId: String,

	@SerializedName("email")
	val email: String,

	@SerializedName("username")
	val username: String,

	@SerializedName("token")
	val token: String
)
