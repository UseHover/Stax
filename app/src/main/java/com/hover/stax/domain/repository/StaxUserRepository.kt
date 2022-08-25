package com.hover.stax.domain.repository

import com.hover.stax.data.remote.dto.UserUpdateDto
import com.hover.stax.data.remote.dto.UserUploadDto
import com.hover.stax.user.StaxUser
import kotlinx.coroutines.flow.Flow

interface StaxUserRepository {

    suspend fun uploadUser(userUploadDto: UserUploadDto): StaxUser

    suspend fun updateUser(email: String, userUpdateDto: UserUpdateDto): StaxUser

    val staxUser: Flow<StaxUser>

    suspend fun saveUser(user: StaxUser)

    suspend fun deleteUser(user: StaxUser)

}