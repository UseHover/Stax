package com.hover.stax.data.repository

import com.hover.stax.data.remote.StaxApi
import com.hover.stax.data.remote.dto.UserRequestDto
import com.hover.stax.data.remote.dto.toStaxUser
import com.hover.stax.domain.repository.StaxUserRepository
import com.hover.stax.user.StaxUser

class StaxUserRepositoryImpl(private val staxApi: StaxApi) : StaxUserRepository {

    override suspend fun uploadUser(user: UserRequestDto): StaxUser {
        val userDto = staxApi.uploadUserToStax(user)
        return userDto.toStaxUser()
    }

    override suspend fun updateUser(email: String, user: UserRequestDto): StaxUser {
        val userDto = staxApi.updateUser(email, user)
        return userDto.toStaxUser()
    }
}