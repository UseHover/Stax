package com.hover.stax.data.remote.dto.authorization

import com.google.gson.annotations.SerializedName

data class AuthRequest(

	@field:SerializedName("scope")
	val scope: String = "write",

	@field:SerializedName("response_type")
	val responseType: String = "code",

	@field:SerializedName("redirect_uri")
	val redirectUri: String,

	@field:SerializedName("stax_user")
	val deviceInfo: DeviceInfo,

	@field:SerializedName("client_id")
	val clientId: String,

	@field:SerializedName("token")
	val token: String
)

data class DeviceInfo(

	@field:SerializedName("device_id")
	val deviceId: String
)
