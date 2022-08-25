package com.hover.stax.data.remote

import com.hover.stax.data.remote.dto.StaxUserDto
import com.hover.stax.data.remote.dto.UserRequestDto
import retrofit2.http.*

interface StaxApi {

    @POST("/stax_api/stax_users")
    suspend fun uploadUserToStax(userDTO: UserRequestDto): StaxUserDto

    @PUT("/stax_api/stax_users/{email}")
    suspend fun updateUser(@Path("email") email: String, userDTO: UserRequestDto): StaxUserDto

    @GET("/api/rewards/reward_points")
    suspend fun getRewardPoints(@Query("email") email: String)
}