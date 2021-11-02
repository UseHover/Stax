package com.hover.stax.settings

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.hover.stax.R
import com.hover.stax.accounts.Account
import com.hover.stax.bounties.BountyEmailFragment
import com.hover.stax.database.DatabaseRepo
import com.hover.stax.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent
import timber.log.Timber
import org.json.JSONObject


class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = KoinJavaComponent.get(DatabaseRepo::class.java)

    var auth: FirebaseAuth = Firebase.auth
    lateinit var signInClient: GoogleSignInClient

    var accounts: LiveData<List<Account>> = MutableLiveData()
    var account = MutableLiveData<Account>()

    val user = MutableLiveData<FirebaseUser>()
    var optedIn = MutableLiveData(false)
    var email = MediatorLiveData<String?>()
    var username = MediatorLiveData<String?>()
    var refereeCode = MutableLiveData<String?>()

    var progress = MutableLiveData(-1)
    var error = MutableLiveData<String?>()

    init {
        username.addSource(email, this::uploadUserToStax)
        loadAccounts()
        getEmail()
        getUsername()
        getRefereeCode()
    }

    fun createGoogleClient(activity: AppCompatActivity) {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.google_server_client_id))
            .requestEmail()
            .build()
        signInClient = GoogleSignIn.getClient(activity, gso)
    }

    private fun setUser(firebaseUser: FirebaseUser) {
        Timber.e("setting user: %s", firebaseUser.email)
        user.postValue(firebaseUser)
        setEmail(firebaseUser.email)
    }

    private fun saveResponseData(json: JSONObject?) {
        Timber.e("response: %s", json.toString())
        setUsername(json?.optString("username"))
        setRefereeCode(json)
    }

    private fun setUsername(name: String?) {
        if (!name.isNullOrEmpty()) {
            username.postValue(name)
            Utils.saveString(USERNAME, name, getApplication())
        }
    }

    private fun setRefereeCode(json: JSONObject?) {
        if (!json?.optString("referee_id").isNullOrEmpty() && !json!!.isNull("referee_id")) {
            refereeCode.postValue(json.optString("referee_id"))
            Utils.saveString(REFEREE_CODE, json.optString("referee_id"), getApplication())
        }
    }

    private fun setEmail(address: String?) {
        Utils.saveString(EMAIL, address, getApplication())
        email.postValue(address)
    }

    private fun getEmail(): String? {
        if (Utils.getString(EMAIL, getApplication()) == null && Utils.getString(BOUNTY_EMAIL_KEY, getApplication()) != null) {
            email.value = Utils.getString(BOUNTY_EMAIL_KEY, getApplication())
            Utils.saveString(EMAIL, email.value, getApplication())
        } else email.value = Utils.getString(EMAIL, getApplication())
        return email.value
    }

    private fun getUsername(): String? {
        username.value = Utils.getString(USERNAME, getApplication())
        return username.value
    }

    private fun getRefereeCode(): String? {
        refereeCode.value = Utils.getString(REFEREE_CODE, getApplication())
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
            onError(getString(R.string.login_google_err))
        }
    }

    private fun signIntoFirebase(idToken: String, activity: AppCompatActivity) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(activity) {
                progress.value = 33
                if (it.isSuccessful) {
                    Timber.i("Sign in with credential: success")
                    auth.currentUser?.let { user -> setUser(user) }
                } else {
                    onError(getString(R.string.login_google_err))
                    Timber.e(it.exception, "Sign in with credential failed")
                }
            }
    }

    fun fetchUsername() {
        val account = GoogleSignIn.getLastSignedInAccount(getApplication())
        if (account != null)
            uploadUserToStax(email.value)
        else
            Timber.e("No account found")
    }

    private fun uploadUserToStax(email: String?) {
        if (getUsername().isNullOrEmpty() && !email.isNullOrEmpty()) {
            progress.value = 66
            viewModelScope.launch(Dispatchers.IO) {
                val result = LoginNetworking(getApplication()).uploadUserToStax(email, optedIn.value!!)
                if (result.code in 200..299)
                    onSuccess(JSONObject(result.body!!.string()), (getApplication() as Context).getString(R.string.uploaded_to_hover, getString(R.string.upload_user)))
                else
                    onError(getString(R.string.upload_user_error))
            }
        }
    }

    fun saveReferee(refereeCode: String) {
        viewModelScope.launch(Dispatchers.IO) {
            if (!email.value.isNullOrEmpty()) {
                val result = LoginNetworking(getApplication()).uploadReferee(email.value!!, refereeCode)
                if (result.code in 200..299)
                    onSuccess(JSONObject(result.body!!.string()), getString(R.string.upload_referee))
                else
                    onError(getString(R.string.upload_referee_error))
            }
        }
    }

    private fun onSuccess(json: JSONObject, successLog: String) {
        Timber.e(json.toString())
        Utils.logAnalyticsEvent((getApplication() as Context).getString(R.string.uploaded_to_hover, successLog), getApplication())
        progress.postValue(100)
        saveResponseData(json)
    }

    private fun onError(message: String?) {
        Utils.logErrorAndReportToFirebase(TAG, message!!, null)
        Utils.logAnalyticsEvent(message, getApplication())
        progress.postValue(-1)
        error.postValue(message)
    }

    private fun loadAccounts() {
        accounts = repo.allAccountsLive
    }

    fun setDefaultAccount(account: Account) {
        Utils.logAnalyticsEvent(getString(R.string.changed_default_account), getApplication())
        if (!accounts.value.isNullOrEmpty()) {
            for (a in accounts.value!!) {
                a.isDefault = a.id == account.id
                repo.update(a)
            }
        }
    }

    private fun getString(res: Int): String {
        return (getApplication() as Context).getString(res)
    }

    companion object {
        const val LOGIN_REQUEST = 4000

//      DEPRECIATED, Migrating
        const val BOUNTY_EMAIL_KEY = "email_for_bounties"

        const val TAG = "SettingsViewModel"
        const val EMAIL = "email"
        const val REFEREE_CODE = "referee"
        const val USERNAME = "username"
    }
}