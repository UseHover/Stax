package com.hover.stax.login

sealed interface LoginUiState {
    object Success : LoginUiState
    object Error : LoginUiState
    object Loading : LoginUiState
}

data class LoginScreenUiState(
        val loginState: LoginUiState
)