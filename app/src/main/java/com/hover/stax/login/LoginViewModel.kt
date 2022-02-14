package com.hover.stax.login

import android.app.Application
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.hover.stax.R
import com.hover.stax.database.DatabaseRepo
import com.hover.stax.utils.AnalyticsUtil
import com.hover.stax.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import timber.log.Timber
import java.io.IOException


private const val EMAIL = "email"
private const val REFEREE_CODE = "referee"
private const val USERNAME = "username"
private const val BOUNTY_EMAIL_KEY = "email_for_bounties"

class LoginViewModel(val repo: DatabaseRepo, val application: Application) : ViewModel() {

    private var auth: FirebaseAuth = Firebase.auth
    lateinit var signInClient: GoogleSignInClient

    private val user = MutableLiveData<FirebaseUser>()
    private var optedIn = MutableLiveData(false)

    var email = MediatorLiveData<String?>()
    var refereeCode = MutableLiveData<String?>()
    var progress = MutableLiveData(-1)
    var error = MutableLiveData<String>()
    var username = MediatorLiveData<String?>()

    val postGoogleAuthNav = MutableLiveData<Int>()

    init {
        getEmail()
        getUsername()
        getRefereeCode()
    }

    fun signIntoFirebaseAsync(data: Intent?, inOrOut: Boolean, activity: AppCompatActivity) {
        optedIn.value = inOrOut
        progress.value = 25
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        try {
            val account = task.getResult(ApiException::class.java)!!
            signIntoFirebase(account.idToken!!, activity)
        } catch (e: ApiException) {
            Timber.e(e, "Google sign in failed")
            onError(application.getString(R.string.login_google_err))
        }
    }

    private fun signIntoFirebase(idToken: String, activity: AppCompatActivity) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential).addOnCompleteListener(activity) {
            progress.value = 33
            if (it.isSuccessful) {
                auth.currentUser?.let { user -> setUser(user) }
            } else {
                onError(application.getString(R.string.login_google_err))
            }
        }
    }

    private fun uploadUserToStax(email: String?, token: String?) {
        if (getUsername().isNullOrEmpty() && !email.isNullOrEmpty()) {
            Timber.e("Uploading user to stax")
            progress.value = 66
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    val result = LoginNetworking(application).uploadUserToStax(email, optedIn.value!!, token)
                    Timber.e("Uploading user to stax came back: ${result.code}")

                    if (result.code in 200..299) onSuccess(
                        JSONObject(result.body!!.string()),
                        application.getString(
                            R.string.uploaded_to_hover,
                            application.getString(R.string.upload_user)
                        )
                    )
                    else onError(application.getString(R.string.upload_user_error))
                } catch (e: IOException) {
                    onError(application.getString(R.string.upload_user_error))
                }
            }
        }
    }

    fun uploadLastUser() {
        val account = GoogleSignIn.getLastSignedInAccount(application)
        if (account != null) uploadUserToStax(email.value, account.idToken)
        else Timber.e("No account found")
    }

    fun saveReferee(refereeCode: String, name: String, phone: String) {
        viewModelScope.launch(Dispatchers.IO) {
            if (!email.value.isNullOrEmpty()) {
                val account = GoogleSignIn.getLastSignedInAccount(application)
                Timber.i("save in referee method, now trying")
                try {
                    val result = LoginNetworking(application).uploadReferee(
                        email.value!!,
                        refereeCode,
                        name,
                        phone,
                        account?.idToken
                    )
                    if (result.code in 200..299) onSuccess(JSONObject(result.body!!.string()), name)
                    else onError(application.getString(R.string.upload_referee_error))
                } catch (e: IOException) {
                    onError(application.getString(R.string.upload_referee_error))
                }
            }
        }
    }

    private fun onSuccess(json: JSONObject, name: String) {
        Timber.e(json.toString())

        refereeCode.value?.let {
            val data = JSONObject()
            try {
                data.put("referee ID", it)
                data.put("username", name)
                AnalyticsUtil.logAnalyticsEvent(
                    application.getString(R.string.upload_referee_success_event),
                    data,
                    application
                )
            } catch (e: Exception) {
                Timber.e(e.localizedMessage)
            }
        }

        progress.postValue(100)
        saveResponseData(json)
    }

    private fun setUser(firebaseUser: FirebaseUser) {
        Timber.e("setting user: %s", firebaseUser.email)
        user.postValue(firebaseUser)
        setEmail(firebaseUser.email)

        firebaseUser.getIdToken(false).addOnSuccessListener {
            Timber.e("getting token which is : %s", it.token)
            uploadUserToStax(firebaseUser.email, it.token)
        }
    }

    private fun saveResponseData(json: JSONObject?) {
        val data = json?.optJSONObject("data")?.optJSONObject("attributes")
        setUsername(data?.optString("username"))
        setRefereeCode(data)
    }

    fun usernameIsNotSet(): Boolean {
        return !getEmail().isNullOrEmpty() && getEmail().isNullOrEmpty()
    }

    private fun setUsername(name: String?) {
        Timber.e("setting username %s", name)
        if (!name.isNullOrEmpty()) {
            username.postValue(name)
            Utils.saveString(USERNAME, name, application)
        }
    }

    private fun setRefereeCode(json: JSONObject?) {
        if (!json?.optString("referee_id").isNullOrEmpty() && !json!!.isNull("referee_id")) {
            refereeCode.postValue(json.optString("referee_id"))
            Utils.saveString(REFEREE_CODE, json.optString("referee_id"), application)
        }
    }

    private fun setEmail(address: String?) {
        Utils.saveString(EMAIL, address, application)
        email.postValue(address)
    }

    private fun getEmail(): String? {
        if (Utils.getString(EMAIL, application) == null && Utils.getString(
                BOUNTY_EMAIL_KEY,
                application
            ) != null
        ) {
            email.value = Utils.getString(BOUNTY_EMAIL_KEY, application)
            Utils.saveString(EMAIL, email.value, application)
        } else email.value = Utils.getString(EMAIL, application)
        return email.value
    }


    private fun getUsername(): String? {
        username.value = Utils.getString(USERNAME, application)
        return username.value
    }

    private fun getRefereeCode(): String? {
        refereeCode.value = Utils.getString(REFEREE_CODE, application)
        return refereeCode.value
    }

    private fun onError(message: String) {
        AnalyticsUtil.logErrorAndReportToFirebase(LoginViewModel::class.java.simpleName, message, null)
        AnalyticsUtil.logAnalyticsEvent(message, application)

        progress.postValue(-1)
        error.postValue(message)
    }

}