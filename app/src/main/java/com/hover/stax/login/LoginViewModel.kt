package com.hover.stax.login

import android.app.Application
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
import com.hover.stax.database.DatabaseRepo
import com.hover.stax.user.StaxUser
import com.hover.stax.user.UserRepo
import com.hover.stax.utils.AnalyticsUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.json.JSONObject
import timber.log.Timber
import java.io.IOException

class LoginViewModel(val repo: DatabaseRepo, val application: Application, private val loginNetworking: LoginNetworking, val userRepo: UserRepo) : ViewModel() {

    lateinit var signInClient: GoogleSignInClient

    val user = MutableLiveData<GoogleSignInAccount>()
    val staxUser = MutableLiveData<StaxUser?>()

    var progress = MutableLiveData(-1)
    var error = MutableLiveData<String>()

    val postGoogleAuthNav = MutableLiveData<Int>()

    init {
        getUser()
    }

    private fun getUser() = viewModelScope.launch {
        userRepo.user.collect { staxUser.postValue(it) }
    }

    fun signIntoGoogle(data: Intent?) {
        progress.value = 25
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        try {
            val account = task.getResult(ApiException::class.java)!!
            setUser(account, account.idToken!!)
        } catch (e: ApiException) {
            Timber.e(e, "Google sign in failed")
            onError(application.getString(R.string.login_google_err))
        }
    }

    private fun uploadUserToStax(email: String?, username: String?, token: String?) {
        if (staxUser.value == null) {
            Timber.e("Uploading user to stax")
            progress.value = 66
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    val userJson = JSONObject()
                        .apply {
                            put("email", email)
                            put("username", username)
                            put("device_id", Hover.getDeviceId(application))
                            put("token", token)
                        }

                    val result = loginNetworking.uploadUserToStax(userJson)
                    Timber.e("Uploading user to stax came back: ${result.code}")

                    if (result.code in 200..299) {
                        Timber.e(result.body.toString())
                        onSuccess(
                            JSONObject(result.body!!.string())
                        )
                    } else onError(application.getString(R.string.upload_user_error))
                } catch (e: IOException) {
                    onError(application.getString(R.string.upload_user_error))
                }
            }
        }
    }

    fun uploadLastUser() {
        val account = GoogleSignIn.getLastSignedInAccount(application)
        if (account != null) uploadUserToStax(account.email, account.displayName!!, account.idToken)
        else Timber.e("No account found")
    }

    fun joinMappers() {
        updateUser(JSONObject().apply {
            put("is_mapper", true)
        })
    }

    fun optInMarketing(optIn: Boolean) {
        updateUser(JSONObject().apply {
            put("marketing_opted_in", optIn)
        })
    }

    private fun updateUser(data: JSONObject) = viewModelScope.launch(Dispatchers.IO) {
        progress.postValue(50)
        staxUser.value?.email?.let {
            Timber.e("Email to update: $it")
            try {
                val result = loginNetworking.updateUser(it, data.put("email", it))
                Timber.e("Updating stax user: ${result.code}")

                if (result.code in 200..299) {
                    val response = result.body!!.string()
                    Timber.e(response)
                    onSuccess(JSONObject(response))
                    progress.postValue(100)
                } else onError(application.getString(R.string.upload_user_error), true)
            } catch (e: IOException) {
                onError(application.getString(R.string.upload_user_error), true)
            }
        }
    }

    private fun onSuccess(json: JSONObject) {
        Timber.e(json.toString())
        saveResponseData(json)
    }

    private fun setUser(signInAccount: GoogleSignInAccount, idToken: String) {
        Timber.e("setting user: %s", signInAccount.email)
        user.postValue(signInAccount)

        progress.value = 33
        uploadUserToStax(signInAccount.email, signInAccount.displayName, idToken)
    }

    private fun saveResponseData(json: JSONObject?) = viewModelScope.launch(Dispatchers.IO) {
        json?.let {
            val attributes = it.getJSONObject("data").getJSONObject("attributes")
            with(attributes) {
                val user = StaxUser(
                    getInt("id"),
                    getString("username"),
                    getString("email"),
                    getBoolean("is_mapper"),
                    getInt("transaction_count"),
                    getInt("bounty_total")
                )

                userRepo.saveUser(user)
            }
        }
    }

    fun userIsNotSet(): Boolean = staxUser.value == null

    //Sign out user if any step of the login process fails. Have user restart the flow, except for updates
    private fun onError(message: String, isUpdate: Boolean = false) {
        Timber.e(message)
        if(isUpdate){
            progress.postValue(-1)
            error.postValue(message)
        } else {
            signInClient.signOut().addOnCompleteListener {
                AnalyticsUtil.logErrorAndReportToFirebase(LoginViewModel::class.java.simpleName, message, null)
                AnalyticsUtil.logAnalyticsEvent(message, application)

                removeUser()

                progress.postValue(-1)
                error.postValue(message)
            }
        }
    }

    fun silentSignOut() = signInClient.signOut().addOnCompleteListener {
        AnalyticsUtil.logAnalyticsEvent(application.getString(R.string.logout), application)
        removeUser()

        progress.postValue(-1)
    }

    private fun removeUser() = viewModelScope.launch(Dispatchers.IO) {
        staxUser.value?.let { userRepo.deleteUser(it) }
    }

}