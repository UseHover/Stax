package com.hover.stax.domain.use_case.stax_user

import com.hover.stax.data.remote.dto.UserRequestDto
import com.hover.stax.domain.model.Resource
import com.hover.stax.domain.repository.StaxUserRepository
import com.hover.stax.user.StaxUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class StaxUserUseCase(private val staxUserRepository: StaxUserRepository) {

    fun uploadUser(userRequestDto: UserRequestDto): Flow<Resource<StaxUser>> = flow {
        try {
            emit(Resource.Loading())

            val staxUser = staxUserRepository.uploadUser(userRequestDto)
            emit(Resource.Success(staxUser))
        } catch (e: Exception) {
            emit(Resource.Error("Error uploading user. Please try again later."))
        }
    }

    fun updateUser(email: String, userRequestDto: UserRequestDto): Flow<Resource<StaxUser>> = flow {
        try {
            emit(Resource.Loading())

            val staxUser = staxUserRepository.updateUser(email, userRequestDto)
            emit(Resource.Success(staxUser))
        } catch (e: Exception) {
            emit(Resource.Error("Error updating user details. Please try again later."))
        }
    }
}