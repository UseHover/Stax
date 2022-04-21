package com.hover.stax.login

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.hover.stax.R
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

const val DAYS_FOR_FLEXIBLE_UPDATE = 3
const val UPDATE_REQUEST_CODE = 90

abstract class AbstractGoogleAuthActivity : AppCompatActivity() {

    private val loginViewModel: LoginViewModel by viewModel()
    private lateinit var staxGoogleLoginInterface: StaxGoogleLoginInterface

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initGoogleAuth()
        setLoginObserver()

        checkForUpdates()
    }

    private fun checkForUpdates() {
        val updateManager = AppUpdateManagerFactory.create(this)
        val updateInfoTask = updateManager.appUpdateInfo

        updateInfoTask.addOnSuccessListener { updateInfo ->
            val updateType = if ((updateInfo.clientVersionStalenessDays() ?: -1) <= DAYS_FOR_FLEXIBLE_UPDATE)
                AppUpdateType.FLEXIBLE
            else
                AppUpdateType.IMMEDIATE

            if (updateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE && updateInfo.isUpdateTypeAllowed(updateType))
                updateManager.startUpdateFlowForResult(updateInfo, updateType, this, UPDATE_REQUEST_CODE)
            else
                Timber.i("No new update available")
        }
    }

    fun setGoogleLoginInterface(staxGoogleLoginInterface: StaxGoogleLoginInterface) {
        this.staxGoogleLoginInterface = staxGoogleLoginInterface
    }

    private fun initGoogleAuth() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.google_server_client_id))
            .requestEmail()
            .build()
        loginViewModel.signInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun setLoginObserver() = with(loginViewModel) {
        error.observe(this@AbstractGoogleAuthActivity) {
            it?.let { staxGoogleLoginInterface.googleLoginFailed() }
        }

        user.observe(this@AbstractGoogleAuthActivity) {
            it?.let { staxGoogleLoginInterface.googleLoginSuccessful() }
        }
    }

    fun signIn(optInMarketing: Boolean? = false) =
        startActivityForResult(
            loginViewModel.signInClient.signInIntent,
            if (optInMarketing!!) LOGIN_REQUEST_OPT_IN_MARKETING else LOGIN_REQUEST
        )

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {
            if (requestCode == LOGIN_REQUEST) {
                val checkBox = findViewById<MaterialCheckBox>(R.id.marketingOptIn)
                loginViewModel.signIntoGoogle(data, checkBox?.isChecked ?: false)
            } else if (requestCode == LOGIN_REQUEST_OPT_IN_MARKETING) {
                loginViewModel.signIntoGoogle(data, true)
            }
        }
    }

    companion object {
        const val LOGIN_REQUEST = 4000
        const val LOGIN_REQUEST_OPT_IN_MARKETING = 4001
    }
}