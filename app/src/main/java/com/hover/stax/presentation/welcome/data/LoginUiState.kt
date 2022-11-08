package com.hover.stax.presentation.welcome.data

import androidx.compose.runtime.Immutable
import com.hover.stax.domain.model.StaxUser

@Immutable
sealed interface LoginUiState {
    object Loading : LoginUiState
    data class Success(val user: StaxUser) : LoginUiState
    object Error : LoginUiState
}