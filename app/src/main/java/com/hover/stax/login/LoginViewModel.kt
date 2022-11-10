package com.hover.stax.login

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import com.hover.sdk.api.Hover
import com.hover.stax.R
import com.hover.stax.data.remote.dto.UpdateDto
import com.hover.stax.data.remote.dto.UploadDto
import com.hover.stax.data.remote.dto.UserUpdateDto
import com.hover.stax.data.remote.dto.UserUploadDto
import com.hover.stax.data.remote.dto.toStaxUser
import com.hover.stax.domain.model.StaxUser
import com.hover.stax.domain.repository.AuthRepository
import com.hover.stax.domain.use_case.stax_user.StaxUserUseCase
import com.hover.stax.preferences.TokenProvider
import com.hover.stax.utils.AnalyticsUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class LoginViewModel(
        application: Application,
        private val staxUserUseCase: StaxUserUseCase,
        private val authRepository: AuthRepository,
        private val tokenProvider: TokenProvider
) : AndroidViewModel(application) {

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
        staxUserUseCase.user.collect { staxUser.value = it }
    }

    fun signIntoGoogle(data: Intent?) {
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        try {
            val account = task.getResult(ApiException::class.java)!!
            setUser(account, account.idToken!!)
        } catch (e: ApiException) {
            Timber.e(e, "Google sign in failed")
            onError((getApplication() as Context).getString(R.string.login_google_err))
        }
    }

    private fun loginUser(token: String, signInAccount: GoogleSignInAccount) = viewModelScope.launch {
        try {
            val auth = authRepository.authorizeClient(token)
            val tokenInfo = authRepository.fetchTokenInfo(auth.redirectUri.code)
            val user = authRepository.uploadUserToStax(UserUploadDto(
                    UploadDto(
                            deviceId = Hover.getDeviceId(getApplication()),
                            email = signInAccount.email!!,
                            username = signInAccount.displayName!!,
                            token = tokenInfo.accessToken
                    )
            ))
            staxUserUseCase.saveUser(user.toStaxUser())
            _loginState.value = LoginScreenUiState(LoginUiState.Success)
        } catch (e: Exception) {
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

    private fun updateUser(email: String, data: UserUpdateDto) = viewModelScope.launch {
//        when (val response = authRepository.updateUser(email, data)) {
//            is DataResult.Loading -> progress.value = 66
//            is DataResult.Success -> {
//                Timber.d("User updated successfully")
//                progress.postValue(100)
//            }
//            is DataResult.Error -> onError(
//                    message = response.exception?.localizedMessage
//                            ?: getString(R.string.upload_user_error),
//                    isUpdate = false
//            )
//        }
    }

    private fun setUser(signInAccount: GoogleSignInAccount, idToken: String) {
        Timber.d("setting user: %s", signInAccount.email)
        googleUser.postValue(signInAccount)
        loginUser(token = idToken, signInAccount = signInAccount)
    }

    fun userIsNotSet(): Boolean = staxUser.value == null

    //Sign out user if any step of the login process fails. Have user restart the flow, except for updates
    private fun onError(message: String, isUpdate: Boolean = false) {
        Timber.e(message)
        if (isUpdate) {
            error.postValue(message)
        } else {
            signInClient.signOut().addOnCompleteListener {
                AnalyticsUtil.logErrorAndReportToFirebase(
                        LoginViewModel::class.java.simpleName,
                        message,
                        null
                )
                AnalyticsUtil.logAnalyticsEvent(message, getApplication())

                removeUser()

                error.postValue(message)
            }
        }
    }

    fun silentSignOut() = signInClient.signOut().addOnCompleteListener {
        AnalyticsUtil.logAnalyticsEvent(getString(R.string.logout), getApplication())
        removeUser()
    }

    private fun removeUser() = viewModelScope.launch {
        staxUser.value?.let { staxUserUseCase.deleteUser(it) }
    }

    private fun getString(id: Int): String {
        return (getApplication() as Context).getString(id)
    }

}