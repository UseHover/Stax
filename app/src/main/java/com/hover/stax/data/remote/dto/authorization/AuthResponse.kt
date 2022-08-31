package com.hover.stax.data.remote.dto.authorization

import com.google.gson.annotations.SerializedName

data class AuthResponse(

	@field:SerializedName("redirect_uri")
	val redirectUri: RedirectUri,

	@field:SerializedName("status")
	val status: String
)

data class RedirectUri(

	@field:SerializedName("code")
	val code: String,

	@field:SerializedName("action")
	val action: String
)
