package com.hover.stax.login

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.checkbox.MaterialCheckBox
import com.hover.stax.R
import com.hover.stax.settings.SettingsViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

abstract class AbstractGoogleAuthActivity : AppCompatActivity() {

    private val settingsViewModel: SettingsViewModel by viewModel()
    private lateinit var staxGoogleLoginInterface: StaxGoogleLoginInterface

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initGoogleAuth()
    }

    fun setGoogleLoginInterface(staxGoogleLoginInterface: StaxGoogleLoginInterface) {
        this.staxGoogleLoginInterface = staxGoogleLoginInterface
    }
    
    private fun initGoogleAuth() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.google_server_client_id))
                .requestEmail()
                .build()
        settingsViewModel.signInClient = GoogleSignIn.getClient(this, gso)
    }

    fun signIn(optInMarketing: Boolean? = false) = startActivityForResult(settingsViewModel.signInClient.signInIntent,
            if (optInMarketing!!) LOGIN_REQUEST_OPT_IN_MARKETING else LOGIN_REQUEST)

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {
            if (requestCode == LOGIN_REQUEST) {
                val checkBox = findViewById<MaterialCheckBox>(R.id.marketingOptIn)
                settingsViewModel.signIntoFirebaseAsync(data, checkBox?.isChecked ?: false, this)
            } else if (requestCode == LOGIN_REQUEST_OPT_IN_MARKETING) {
                settingsViewModel.signIntoFirebaseAsync(data, true, this)
            }
            staxGoogleLoginInterface.googleLoginSuccessful()
        } else {
            staxGoogleLoginInterface.googleLoginFailed()
        }
    }

    companion object {
        const val LOGIN_REQUEST = 4000
        const val LOGIN_REQUEST_OPT_IN_MARKETING = 4001
    }
}