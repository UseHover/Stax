package com.hover.stax.domain.repository

import com.hover.stax.data.remote.dto.UserRequestDto
import com.hover.stax.user.StaxUser

interface StaxUserRepository {

    suspend fun uploadUser(user: UserRequestDto): StaxUser

    suspend fun updateUser(email: String, user: UserRequestDto): StaxUser

}