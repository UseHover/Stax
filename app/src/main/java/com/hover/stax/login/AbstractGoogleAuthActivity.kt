package com.hover.stax.login

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.hover.stax.R
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

abstract class AbstractGoogleAuthActivity : AppCompatActivity() {

    private val loginViewModel: LoginViewModel by viewModel()
    private lateinit var staxGoogleLoginInterface: StaxGoogleLoginInterface

    private lateinit var updateManager: AppUpdateManager
    private var installListener: InstallStateUpdatedListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initGoogleAuth()
        setLoginObserver()

        updateManager = AppUpdateManagerFactory.create(this)
        checkForUpdates()
    }

    //checks that the update has not stalled
    override fun onResume() {
        super.onResume()
        updateManager.appUpdateInfo.addOnSuccessListener { updateInfo ->
            //if the update is downloaded but not installed, notify user to complete the update
            if (updateInfo.installStatus() == InstallStatus.DOWNLOADED)
                showSnackbarForCompleteUpdate()

            //if an in-app update is already running, resume the update
            if(updateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                updateManager.startUpdateFlowForResult(updateInfo, AppUpdateType.IMMEDIATE, this, UPDATE_REQUEST_CODE)
            }
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

    fun signIn() = startActivityForResult(loginViewModel.signInClient.signInIntent, LOGIN_REQUEST)

    private fun checkForUpdates() {
        val updateInfoTask = updateManager.appUpdateInfo

        updateInfoTask.addOnSuccessListener { updateInfo ->
            val updateType = if ((updateInfo.clientVersionStalenessDays() ?: -1) <= DAYS_FOR_FLEXIBLE_UPDATE)
                AppUpdateType.FLEXIBLE
            else
                AppUpdateType.IMMEDIATE

            if (updateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE && updateInfo.isUpdateTypeAllowed(updateType))
                requestUpdate(updateInfo, updateType)
            else
                Timber.i("No new update available")
        }
    }

    private fun requestUpdate(updateInfo: AppUpdateInfo, updateType: Int) {
        if (updateType == AppUpdateType.FLEXIBLE) {
            installListener = InstallStateUpdatedListener {
                if (it.installStatus() == InstallStatus.DOWNLOADED)
                    showSnackbarForCompleteUpdate()
            }
            updateManager.registerListener(installListener!!)
        } 

        updateManager.startUpdateFlowForResult(updateInfo, updateType, this, UPDATE_REQUEST_CODE)
    }

    private fun showSnackbarForCompleteUpdate() {
        Snackbar.make(findViewById(R.id.home_root), getString(R.string.update_downloaded), Snackbar.LENGTH_INDEFINITE).apply {
            setAction(getString(R.string.restart)) { updateManager.completeUpdate(); installListener?.let { updateManager.unregisterListener(it) } }
            setActionTextColor(ContextCompat.getColor(this@AbstractGoogleAuthActivity, R.color.stax_state_blue))
            show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            LOGIN_REQUEST -> if (resultCode == RESULT_OK) loginViewModel.signIntoGoogle(data)
            UPDATE_REQUEST_CODE -> if (resultCode != RESULT_OK) {
                Timber.e("Update flow failed. Result code : $resultCode")
                checkForUpdates()
            }
        }
    }

    companion object {
        const val LOGIN_REQUEST = 4000
        const val DAYS_FOR_FLEXIBLE_UPDATE = 3
        const val UPDATE_REQUEST_CODE = 90
    }
}