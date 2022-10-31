package com.hover.stax.data.remote

data class NAuthRequest(
    val scope: String = "write",
    val responseType: String = "code",
    val redirectUri: String,
    val deviceInfo: NDeviceInfo,
    val clientId: String,
    val token: String
)

data class NDeviceInfo(
    val deviceId: String
)

data class NAuthResponse(
    val redirectUri: NRedirectUri,
    val status: String
)

data class NRedirectUri(
    val code: String,
    val action: String
)