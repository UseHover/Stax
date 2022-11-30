package com.hover.stax.domain.use_case.stax_user

import com.hover.stax.domain.model.StaxUser
import com.hover.stax.domain.repository.StaxUserRepository

class StaxUserUseCase(private val staxUserRepository: StaxUserRepository) {

    val user = staxUserRepository.staxUser

    suspend fun saveUser(user: StaxUser) = staxUserRepository.saveUser(user)

    suspend fun deleteUser(user: StaxUser) = staxUserRepository.deleteUser(user)
}