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
import com.hover.stax.domain.model.Resource
import com.hover.stax.domain.use_case.stax_user.StaxUserUseCase
import com.hover.stax.domain.model.StaxUser
import com.hover.stax.domain.use_case.auth.AuthUseCase
import com.hover.stax.utils.AnalyticsUtil
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber

class LoginViewModel(application: Application, private val staxUserUseCase: StaxUserUseCase, private val authUseCase: AuthUseCase) : AndroidViewModel(application) {

    lateinit var signInClient: GoogleSignInClient

    val googleUser = MutableLiveData<GoogleSignInAccount>()
    var staxUser = MutableLiveData<StaxUser?>()
        private set

    var progress = MutableLiveData(-1)
    var error = MutableLiveData<String>()

    init {
        getUser()
    }

    private fun getUser() = viewModelScope.launch {
        staxUserUseCase.user.collect { staxUser.value = it }
    }

    fun signIntoGoogle(data: Intent?) {
        progress.value = 25
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        try {
            val account = task.getResult(ApiException::class.java)!!
            setUser(account, account.idToken!!)
        } catch (e: ApiException) {
            Timber.e(e, "Google sign in failed")
            onError((getApplication() as Context).getString(R.string.login_google_err))
        }
    }

    private fun authorizeClient(token: String) {
        authUseCase.authorize(token).onEach { result ->
            when (result) {
                is Resource.Success -> {
                    Timber.d("Stax login successful ${result.data?.accessToken}")
                    progress.postValue(100)
                }
                is Resource.Error -> onError(result.message ?: getString(R.string.upload_user_error), false)
                is Resource.Loading -> progress.value = 66
            }
        }.launchIn(viewModelScope)
    }

    private fun uploadUserToStax(email: String, username: String, token: String) {
        if (staxUser.value == null) {
            Timber.e("Uploading user to stax")

            val userDto = UploadDto(Hover.getDeviceId(getApplication()), email, username, token)
            val requestDto = UserUploadDto(userDto)

            staxUserUseCase.uploadUser(requestDto).onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        Timber.d("User uploaded to stax successfully ${result.data?.id}")
                        progress.postValue(100)
                    }
                    is Resource.Error -> onError(result.message ?: getString(R.string.upload_user_error), false)
                    is Resource.Loading -> progress.value = 66
                }
            }.launchIn(viewModelScope)
        }
    }

    fun uploadLastUser() {
        val account = GoogleSignIn.getLastSignedInAccount(getApplication())
        if (account != null) uploadUserToStax(account.email!!, account.displayName!!, account.idToken!!)
        else Timber.e("No account found")
    }

    fun optInMarketing(optIn: Boolean) = staxUser.value?.email?.let { updateUser(UserUpdateDto(UpdateDto(marketingOptedIn = optIn, email = it))) }

    private fun updateUser(data: UserUpdateDto) = staxUserUseCase.updateUser(data.staxUser.email, data).onEach { result ->
        when (result) {
            is Resource.Success -> {
                Timber.d("User updated successfully")
                progress.postValue(100)
            }
            is Resource.Error -> onError(result.message ?: getString(R.string.upload_user_error), false)
            is Resource.Loading -> progress.value = 66
        }
    }.launchIn(viewModelScope)

    private fun setUser(signInAccount: GoogleSignInAccount, idToken: String) {
        Timber.e("setting user: %s", signInAccount.email)
        googleUser.postValue(signInAccount)

        progress.value = 33

        authorizeClient(idToken)
//        if (signInAccount.email != null && signInAccount.displayName != null)
//            uploadUserToStax(signInAccount.email!!, signInAccount.displayName!!, idToken)
    }

    fun userIsNotSet(): Boolean = staxUser.value == null

    //Sign out user if any step of the login process fails. Have user restart the flow, except for updates
    private fun onError(message: String, isUpdate: Boolean = false) {
        Timber.e(message)
        if (isUpdate) {
            progress.postValue(-1)
            error.postValue(message)
        } else {
            signInClient.signOut().addOnCompleteListener {
                AnalyticsUtil.logErrorAndReportToFirebase(LoginViewModel::class.java.simpleName, message, null)
                AnalyticsUtil.logAnalyticsEvent(message, getApplication())

                removeUser()

                progress.postValue(-1)
                error.postValue(message)
            }
        }
    }

    fun silentSignOut() = signInClient.signOut().addOnCompleteListener {
        AnalyticsUtil.logAnalyticsEvent(getString(R.string.logout), getApplication())
        removeUser()

        progress.postValue(-1)
    }

    private fun removeUser() = viewModelScope.launch {
        staxUser.value?.let { staxUserUseCase.deleteUser(it) }
    }

    private fun getString(id: Int): String {
        return (getApplication() as Context).getString(id)
    }

}