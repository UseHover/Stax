package com.hover.stax.domain.use_case.auth

import com.hover.stax.data.remote.DataResult
import com.hover.stax.domain.model.Resource
import com.hover.stax.domain.model.StaxUser
import com.hover.stax.domain.repository.AuthRepository
import com.hover.stax.domain.repository.StaxUserRepository
import com.hover.stax.utils.TokenUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import timber.log.Timber

class AuthUseCase(
    private val authRepository: AuthRepository,
    private val staxUserRepository: StaxUserRepository
    ) {

    fun authorize(idToken: String): Flow<Resource<StaxUser>> = flow {
        try {
            emit(Resource.Loading())

//            val authResponse = authRepository.authorizeClient(idToken)
//            val tokenInfo = authRepository.fetchTokenInfo(authResponse.redirectUri.code)
//
//            val user = TokenUtils.decodeToken(tokenInfo.accessToken)
//            staxUserRepository.saveUser(user!!)
//
//            emit(Resource.Success(user))
        } catch (e: Exception) {
            Timber.e(e)
            emit(Resource.Error(e.message ?: "An error occurred during login", null))
        }
    }
}