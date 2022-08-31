package com.hover.stax.domain.use_case.auth

import com.hover.stax.domain.model.Resource
import com.hover.stax.domain.model.TokenInfo
import com.hover.stax.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import timber.log.Timber

class AuthUseCase(private val authRepository: AuthRepository) {

    fun authorize(idToken: String): Flow<Resource<TokenInfo>> = flow {
        try {
            emit(Resource.Loading())

            val authResponse = authRepository.authorizeClient(idToken)
            val tokenInfo = authRepository.fetchTokenInfo(authResponse.redirectUri.code)

            emit(Resource.Success(tokenInfo))
        } catch (e: Exception) {
            Timber.e(e)
            emit(Resource.Error(e.message ?: "An error occurred during login", null))
        }
    }
}