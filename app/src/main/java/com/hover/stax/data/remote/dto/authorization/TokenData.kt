package com.hover.stax.data.remote.dto.authorization

import com.google.gson.annotations.SerializedName

data class TokenData(

	@field:SerializedName("iss")
	val iss: String,

	@field:SerializedName("iat")
	val iat: Int,

	@field:SerializedName("user")
	val user: String,

	@field:SerializedName("jti")
	val jti: String
)
