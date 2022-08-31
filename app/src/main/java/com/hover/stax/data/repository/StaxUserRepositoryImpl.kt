package com.hover.stax.data.repository

import com.hover.stax.data.remote.StaxApi
import com.hover.stax.data.remote.dto.UserUpdateDto
import com.hover.stax.data.remote.dto.UserUploadDto
import com.hover.stax.data.remote.dto.toStaxUser
import com.hover.stax.domain.repository.StaxUserRepository
import com.hover.stax.domain.model.StaxUser
import com.hover.stax.data.local.user.UserRepo
import kotlinx.coroutines.flow.Flow

class StaxUserRepositoryImpl(private val staxApi: StaxApi, private val userRepo: UserRepo) : StaxUserRepository {

    override suspend fun uploadUser(userUploadDto: UserUploadDto): StaxUser {
        val userDto = staxApi.uploadUserToStax(userUploadDto)

        return userDto.toStaxUser().also { userRepo.saveUser(it) }
    }

    override suspend fun updateUser(email: String, userUpdateDto: UserUpdateDto): StaxUser {
        val userDto = staxApi.updateUser(email, userUpdateDto)

        return userDto.toStaxUser().also { userRepo.saveUser(it) }
    }

    override val staxUser: Flow<StaxUser>
        get() = userRepo.user

    override suspend fun saveUser(user: StaxUser) = userRepo.saveUser(user)

    override suspend fun deleteUser(user: StaxUser) = userRepo.deleteUser(user)
}