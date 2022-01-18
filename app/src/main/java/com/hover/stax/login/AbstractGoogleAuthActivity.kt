package com.hover.stax.login

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.checkbox.MaterialCheckBox
import com.hover.stax.R
import com.hover.stax.settings.SettingsViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

abstract class AbstractGoogleAuthActivity: AppCompatActivity() {
    private val settingsViewModel: SettingsViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initGoogleAuth()
    }


    private fun initGoogleAuth() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.google_server_client_id))
                .requestEmail()
                .build()
        settingsViewModel.signInClient = GoogleSignIn.getClient(this, gso)
    }

    fun signIn() = startActivityForResult(settingsViewModel.signInClient.signInIntent, LOGIN_REQUEST)

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

            if(requestCode == LOGIN_REQUEST) {
                settingsViewModel.signIntoFirebaseAsync(data, findViewById<MaterialCheckBox>(R.id.marketingOptIn)?.isChecked ?: false, this)
        }
    }

    companion object {
        val LOGIN_REQUEST = 4000
    }


}