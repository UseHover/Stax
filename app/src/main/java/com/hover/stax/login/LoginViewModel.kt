/*
 * Copyright 2022 Stax
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hover.stax.login

import android.content.Context
import android.content.Intent
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import com.hover.sdk.api.Hover
import com.hover.stax.R
import com.hover.stax.data.auth.AuthRepository
import com.hover.stax.data.util.toStaxUser
import com.hover.stax.database.models.StaxUser
import com.hover.stax.datastore.DefaultTokenProvider
import com.hover.stax.datastore.TokenProvider
import com.hover.stax.domain.use_case.stax_user.StaxUserUseCase
import com.hover.stax.model.auth.UpdateDto
import com.hover.stax.model.auth.UploadDto
import com.hover.stax.model.auth.UserUpdateDto
import com.hover.stax.model.auth.UserUploadDto
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    @ApplicationContext val context: Context,
    private val staxUserUseCase: StaxUserUseCase,
    private val authRepository: AuthRepository,
    private val tokenProvider: TokenProvider
) : ViewModel() {

    lateinit var signInClient: GoogleSignInClient

    val googleUser = MutableLiveData<GoogleSignInAccount>()
    var staxUser = MutableLiveData<StaxUser?>()
        private set

    var error = MutableLiveData<String>()

    private val _loginState = MutableStateFlow(LoginScreenUiState(LoginUiState.Loading))
    val loginState = _loginState.asStateFlow()

    init {
        getUser()
    }

    private fun getUser() = viewModelScope.launch {
        staxUserUseCase.user.collectLatest { staxUser.value = it } // To understand the UseCase
    }

    fun signIntoGoogle(data: Intent?) {
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        try {
            val account = task.getResult(ApiException::class.java)!!
            setUser(account, account.idToken!!)
        } catch (e: ApiException) {
            Timber.e(e, "Google sign in failed")
            onError(context.getString(R.string.login_google_err))
        }
    }

    private fun loginUser(token: String, signInAccount: GoogleSignInAccount) =
        viewModelScope.launch {
            try {
                val authorization = authRepository.authorizeClient(token)
                val response = authRepository.fetchTokenInfo(authorization.redirectUri.code)
                with(tokenProvider) {
                    update(
                        key = DefaultTokenProvider.ACCESS_TOKEN,
                        token = response.accessToken
                    )
                    update(
                        key = DefaultTokenProvider.REFRESH_TOKEN,
                        token = response.refreshToken.toString()
                    )
                }.also {
                    val user = authRepository.uploadUserToStax(
                        UserUploadDto(
                            UploadDto(
                                deviceId = Hover.getDeviceId(context),
                                email = signInAccount.email,
                                username = signInAccount.displayName,
                                token = response.accessToken
                            )
                        )
                    )
                    staxUserUseCase.saveUser(user.toStaxUser())
                    _loginState.value = LoginScreenUiState(LoginUiState.Success)
                }
            } catch (e: Exception) {
                Timber.e("Login failed $e")
                _loginState.value = LoginScreenUiState(LoginUiState.Error)
            }
        }

    fun optInMarketing(optIn: Boolean) = staxUser.value?.email?.let { email ->
        updateUser(
            email = email,
            data = UserUpdateDto(
                UpdateDto(
                    marketingOptedIn = optIn,
                    email = email
                )
            )
        )
    }

    private fun updateUser(email: String, data: UserUpdateDto) =
        viewModelScope.launch {
            try {
                authRepository.updateUser(email, data)
                _loginState.value = LoginScreenUiState(LoginUiState.Success)
            } catch (e: Exception) {
                _loginState.value = LoginScreenUiState(LoginUiState.Error)
            }
        }

    private fun setUser(signInAccount: GoogleSignInAccount, idToken: String) {
        Timber.d("setting user: %s", signInAccount.email)
        googleUser.postValue(signInAccount)
        loginUser(token = idToken, signInAccount = signInAccount)
    }

    @Suppress("unused")
    fun userIsNotSet(): Boolean = staxUser.value == null

    // Sign out user if any step of the login process fails. Have user restart the flow, except for updates
    private fun onError(message: String, isUpdate: Boolean = false) {
        Timber.e(message)
        if (isUpdate) {
            error.postValue(message)
        } else {
            signInClient.signOut().addOnCompleteListener {
                com.hover.stax.utils.AnalyticsUtil.logErrorAndReportToFirebase(
                    LoginViewModel::class.java.simpleName,
                    message,
                    null
                )
                com.hover.stax.utils.AnalyticsUtil.logAnalyticsEvent(message, context)

                removeUser()

                error.postValue(message)
            }
        }
    }

    fun silentSignOut() = signInClient.signOut().addOnCompleteListener {
        com.hover.stax.utils.AnalyticsUtil.logAnalyticsEvent(
            getString(R.string.logout),
            context
        )
        removeUser()
    }

    private fun removeUser() = viewModelScope.launch {
        staxUser.value?.let { staxUserUseCase.deleteUser(it) }
    }

    private fun getString(id: Int): String {
        return context.getString(id)
    }
}