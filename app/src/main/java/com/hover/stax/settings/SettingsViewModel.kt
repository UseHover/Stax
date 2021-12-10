package com.hover.stax.settings

import android.app.Application
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.hover.stax.R
import com.hover.stax.accounts.Account
import com.hover.stax.channels.Channel
import com.hover.stax.database.DatabaseRepo
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

class SettingsViewModel(val repo: DatabaseRepo, val application: Application) : ViewModel() {

    private var auth: FirebaseAuth = Firebase.auth
    lateinit var signInClient: GoogleSignInClient

    private val user = MutableLiveData<FirebaseUser>()
    private var optedIn = MutableLiveData(false)

    var accounts: LiveData<List<Account>> = MutableLiveData()
    var account = MutableLiveData<Account>()
    val channel = MutableLiveData<Channel>()
    var email = MediatorLiveData<String?>()
    var refereeCode = MutableLiveData<String?>()
    var progress = MutableLiveData(-1)
    var error = MutableLiveData<String>()


    var username = MediatorLiveData<String?>()
    init {
        loadAccounts()
        getEmail()
        getUsername()
        getRefereeCode()
    }

    private fun setUser(firebaseUser: FirebaseUser) {
        Timber.e("setting user: %s", firebaseUser.email)
        user.postValue(firebaseUser)
        setEmail(firebaseUser.email)

        firebaseUser.getIdToken(false).addOnSuccessListener {
            uploadUserToStax(firebaseUser.email, it.token)
        }
    }

    private fun saveResponseData(json: JSONObject?) {
        val data = json?.optJSONObject("data")?.optJSONObject("attributes")
        setUsername(data?.optString("username"))
        setRefereeCode(data)
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
        if (Utils.getString(EMAIL, application) == null && Utils.getString(BOUNTY_EMAIL_KEY, application) != null) {
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
        auth.signInWithCredential(credential)
                .addOnCompleteListener(activity) {
                    progress.value = 33
                    if (it.isSuccessful) {
                        auth.currentUser?.let { user -> setUser(user) }
                    } else {
                        onError(application.getString(R.string.login_google_err))
                    }
                }
    }

    fun fetchUsername() {
        val account = GoogleSignIn.getLastSignedInAccount(application)
        if (account != null)
            uploadUserToStax(email.value, account.idToken)
        else
            Timber.e("No account found")
    }

    private fun uploadUserToStax(email: String?, token: String?) {
        if (getUsername().isNullOrEmpty() && !email.isNullOrEmpty()) {
            progress.value = 66
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    val result = LoginNetworking(application).uploadUserToStax(email, optedIn.value!!, token)
                    if (result.code in 200..299)
                        onSuccess(JSONObject(result.body!!.string()), application.getString(R.string.uploaded_to_hover, application.getString(R.string.upload_user)))
                } catch (e: IOException) {
                    onError(application.getString(R.string.upload_user_error))
                }
            }
        }
    }

    fun saveReferee(refereeCode: String, name: String, phone: String) {
        viewModelScope.launch(Dispatchers.IO) {
            if (!email.value.isNullOrEmpty()) {
                val account = GoogleSignIn.getLastSignedInAccount(application)
                Timber.i("save in referee method, now trying")
                try {
                    val result = LoginNetworking(application).uploadReferee(email.value!!, refereeCode, name, phone, account?.idToken)
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

//        Utils.logAnalyticsEvent(application.getString(R.string.uploaded_to_hover, successLog), application)
        refereeCode.value?.let {
            val data = JSONObject()
            try {
                data.put("referee ID", it)
                data.put("username", name)
                Utils.logAnalyticsEvent(application.getString(R.string.upload_referee_success_event), data, application)
            } catch (e: Exception) {
                Timber.e(e.localizedMessage)
            }
        }

        progress.postValue(100)
        saveResponseData(json)
    }

    private fun onError(message: String?) {
        Utils.logErrorAndReportToFirebase(SettingsViewModel::class.java.simpleName, message!!, null)
        Utils.logAnalyticsEvent(message, application)

        progress.postValue(-1)
        error.postValue(message)
    }

    private fun loadAccounts() {
        accounts = repo.allAccountsLive
    }

    fun setDefaultAccount(account: Account) {
        if (!accounts.value.isNullOrEmpty()) {
            //remove current default account
            val current: Account? = accounts.value!!.firstOrNull { it.isDefault }
            current?.isDefault = false
            repo.update(current)

            val a = accounts.value!!.first { it.id == account.id }
            a.isDefault = true
            repo.update(a)
        }
    }

    companion object {
        const val LOGIN_REQUEST = 4000
    }
}