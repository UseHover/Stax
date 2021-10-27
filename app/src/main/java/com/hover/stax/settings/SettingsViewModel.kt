package com.hover.stax.settings

import android.app.Application
import android.content.Context
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
import com.hover.stax.bounties.BountyEmailFragment
import com.hover.stax.database.DatabaseRepo
import com.hover.stax.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent
import timber.log.Timber
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.gson.JsonObject
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
        username.addSource(user, this::uploadUserToStax)
        email.addSource(username, this::setUserEmail)
        loadAccounts()
        getEmail()
        getUsername()
        getRefereeCode()
    }

    fun setUser(firebaseUser: FirebaseUser) {
        user.value = firebaseUser
    }

    private fun setUserEmail(username: String?): String? {
        if (username != null && Utils.getString(EMAIL, getApplication()).isNullOrEmpty() && user.value?.email != null) {
            Utils.saveString(EMAIL, user.value!!.email, getApplication())
            email.value = user.value!!.email
        } else email.value = Utils.getString(EMAIL, getApplication())
        return email.value
    }

    private fun setUsername(name: String?) {
        if (name == null) return
        Utils.saveString(USERNAME, name, getApplication())
        username.postValue(name)
    }

    fun setRefereeCode(code: String) {
        Utils.saveString(REFEREE_CODE, code, getApplication())
        refereeCode.value = code
    }

    fun getEmail(): String? {
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
            onError((getApplication() as Context).getString(R.string.login_google_err))
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
                    onError((getApplication() as Context).getString(R.string.login_google_err))
                    Timber.e(it.exception, "Sign in with credential failed")
                }
            }
    }

    fun fetchUsername() {
        Timber.e("Getting username?")
        val account = GoogleSignIn.getLastSignedInAccount(getApplication())
        if (account != null)
            uploadUserToStax(user.value)
        else
            Timber.e("No account found")
    }

    fun uploadUserToStax(user: FirebaseUser?) {
        if (getUsername().isNullOrEmpty()) {
            email.value?.let {
                progress.value = 66
                viewModelScope.launch(Dispatchers.IO) {
                    val result = LoginNetworking(getApplication()).uploadUserToStax(email.value!!, optedIn.value!!)
                    if (result.code in 200..299)
                        onSuccess(JSONObject(result.body!!.string()))
                    else
                        onError(result.body!!.string())
                }
            }
        }
    }

    private fun onSuccess(json: JSONObject?) {
        progress.postValue(100)
        Timber.e(json.toString())
        setUsername(json?.getString("username"))
        Utils.logAnalyticsEvent((getApplication() as Context).getString(R.string.create_login_success), getApplication())
    }

    private fun onError(message: String?) {
        Utils.logErrorAndReportToFirebase(BountyEmailFragment.TAG, message!!, null)
        Utils.logAnalyticsEvent(message, getApplication())
        progress.postValue(-1)
        error.postValue(message)
    }

    private fun loadAccounts() {
        accounts = repo.allAccountsLive
    }

    fun setDefaultAccount(account: Account) {
        if (!accounts.value.isNullOrEmpty()) {
            for (a in accounts.value!!) {
                a.isDefault = a.id == account.id
                repo.update(a)
            }
        }
    }

    companion object {
//      DEPRECIATED, Migrating
        const val BOUNTY_EMAIL_KEY = "email_for_bounties"

        const val EMAIL = "email"
        const val REFEREE_CODE = "referee"
        const val USERNAME = "username"
    }
}