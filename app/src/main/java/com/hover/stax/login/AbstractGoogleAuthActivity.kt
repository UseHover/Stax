package com.hover.stax.login

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.checkbox.MaterialCheckBox
import com.hover.stax.R
import com.hover.stax.bounties.BountyEmailFragmentDirections
import com.hover.stax.settings.SettingsFragment
import com.hover.stax.utils.UIHelper
import org.koin.androidx.viewmodel.ext.android.viewModel

abstract class AbstractGoogleAuthActivity : AppCompatActivity(), StaxGoogleLoginInterface {

    private val loginViewModel: LoginViewModel by viewModel()
    private lateinit var staxGoogleLoginInterface: StaxGoogleLoginInterface

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initGoogleAuth()

        setLoginObserver()
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

    override fun googleLoginSuccessful() {
        if (loginViewModel.postGoogleAuthNav.value == SettingsFragment.SHOW_BOUNTY_LIST)
            BountyEmailFragmentDirections.actionBountyEmailFragmentToBountyListFragment()
    }

    override fun googleLoginFailed() {
        UIHelper.flashMessage(this, R.string.login_google_err)
    }

    companion object {
        const val LOGIN_REQUEST = 4000
        const val LOGIN_REQUEST_OPT_IN_MARKETING = 4001
    }
}