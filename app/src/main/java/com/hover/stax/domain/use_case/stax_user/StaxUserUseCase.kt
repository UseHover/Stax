package com.hover.stax.domain.use_case.stax_user

import com.hover.stax.data.remote.dto.UserUpdateDto
import com.hover.stax.data.remote.dto.UserUploadDto
import com.hover.stax.domain.model.Resource
import com.hover.stax.domain.repository.StaxUserRepository
import com.hover.stax.domain.model.StaxUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import timber.log.Timber

class StaxUserUseCase(private val staxUserRepository: StaxUserRepository) {

    val user = staxUserRepository.staxUser

    @Deprecated("Use the method in auth use case instead")
    fun uploadUser(userUploadDto: UserUploadDto): Flow<Resource<StaxUser>> = flow {
        try {
            emit(Resource.Loading())

            val staxUser = staxUserRepository.uploadUser(userUploadDto)

            emit(Resource.Success(staxUser))
        } catch (e: Exception) {
            Timber.e(e)
            emit(Resource.Error("Error uploading user. Please try again later."))
        }
    }

    fun updateUser(email: String, userUpdateDto: UserUpdateDto): Flow<Resource<StaxUser>> = flow {
        try {
            emit(Resource.Loading())

            val staxUser = staxUserRepository.updateUser(email, userUpdateDto)

            emit(Resource.Success(staxUser))
        } catch (e: Exception) {
            emit(Resource.Error("Error updating user details. Please try again later."))
        }
    }

    suspend fun deleteUser(user: StaxUser) = staxUserRepository.deleteUser(user)
}