package com.hover.stax.data.remote

import com.hover.stax.data.remote.dto.StaxUserDto
import com.hover.stax.data.remote.dto.UserUpdateDto
import com.hover.stax.data.remote.dto.UserUploadDto
import com.hover.stax.data.remote.dto.authorization.*
import retrofit2.http.*

interface StaxApi {

    /**
     * Authorization methods
     */
    @POST("stax_api/authorize")
    suspend fun authorize(@Body authRequest: AuthRequest): AuthResponse

    @POST("stax_api/token")
    suspend fun fetchToken(@Body tokenRequest: TokenRequest): TokenResponse

    @POST("stax_api/token")
    suspend fun refreshToken(@Body refreshRequest: TokenRefreshRequest): TokenResponse

    /**
     * User methods
     */
    @POST("/stax_api/stax_users")
    suspend fun uploadUserToStax(@Body userDTO: UserUploadDto): StaxUserDto

    @PUT("/stax_api/stax_users/{email}")
    suspend fun updateUser(@Path("email") email: String, @Body userDTO: UserUpdateDto): StaxUserDto

    /**
     * Rewards methods
     */
    @GET("/api/rewards/reward_points")
    suspend fun getRewardPoints(@Query("email") email: String)
}