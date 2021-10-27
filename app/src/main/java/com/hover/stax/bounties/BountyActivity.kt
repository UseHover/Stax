package com.hover.stax.bounties


import android.content.Intent
import android.os.Bundle
import com.appsflyer.AppsFlyerLib
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.hover.sdk.actions.HoverAction
import com.hover.sdk.api.HoverParameters
import com.hover.stax.R
import com.hover.stax.databinding.ActivityBountyBinding
import com.hover.stax.navigation.AbstractNavigationActivity
import com.hover.stax.pushNotification.PushNotificationTopicsInterface
import com.hover.stax.settings.SettingsViewModel
import com.hover.stax.utils.Utils
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber


class BountyActivity : AbstractNavigationActivity(), PushNotificationTopicsInterface {

    private val bountyViewModel: BountyViewModel by viewModel()
    private val loginViewModel: SettingsViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Utils.logAnalyticsEvent(getString(R.string.visit_screen, BountyActivity::class.java.simpleName), this)

        val binding = ActivityBountyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setUpNav()
        initAuth()
    }

    private fun initAuth() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.google_server_client_id))
                .requestEmail()
                .build()
        loginViewModel.signInClient = GoogleSignIn.getClient(this, gso)
        loginViewModel.email.observe(this) { Timber.e("Got email from google %s", it) }
        loginViewModel.username.observe(this) { navigateOnUsernameLoad(it) }
    }

    fun signIn() {
        val signInIntent = loginViewModel.signInClient.signInIntent
        startActivityForResult(signInIntent, LOGIN_REQUEST)
    }

    override fun onStart() {
        super.onStart()
        AppsFlyerLib.getInstance().start(this)

        if (loginViewModel.auth.currentUser == null)
            Utils.logAnalyticsEvent(getString(R.string.visit_screen, getString(R.string.visit_bounty_email)), this)
    }

    private fun navigateOnUsernameLoad(username: String?) {
        Timber.e("Username: %s", username)
        Timber.e("is it empty?: %b", username.isNullOrEmpty())
        if (!username.isNullOrEmpty())
            navigateToBountyListFragment(getNavController())
    }

    fun makeCall(a: HoverAction) {
        Utils.logAnalyticsEvent(getString(R.string.clicked_run_bounty_session), this)
        updatePushNotifGroupStatus(a)
        call(a.public_id)
    }

    private fun updatePushNotifGroupStatus(a: HoverAction) {
        joinAllBountiesGroup(this)
        joinBountyCountryGroup(a.country_alpha2.uppercase(), this)
    }

    fun retryCall(actionId: String) {
        Utils.logAnalyticsEvent(getString(R.string.clicked_retry_bounty_session), this)
        call(actionId)
    }

    private fun call(actionId: String) {
        val i = HoverParameters.Builder(this).request(actionId).setEnvironment(HoverParameters.MANUAL_ENV).buildIntent()
        startActivityForResult(i, BOUNTY_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Timber.e("called on activity result")
        if (requestCode == BOUNTY_REQUEST)
            showBountyDetails(data)
        else if (requestCode == LOGIN_REQUEST)
            loginViewModel.signIntoFirebaseAsync(data, (findViewById<MaterialCheckBox>(R.id.marketingOptIn)).isChecked, this)
    }

    private fun showBountyDetails(data: Intent?) {
        if (data != null) {
            val transactionUUID = data.getStringExtra("uuid")
            if (transactionUUID != null) navigateToTransactionDetailsFragment(transactionUUID, supportFragmentManager, true)
        }
    }

    override fun onBackPressed() {
        val controller = getNavController()
        if (controller.currentDestination != null && (controller.currentDestination!!.id == R.id.bountyListFragment
                        || controller.currentDestination!!.id == R.id.bountyEmailFragment)) {
            navigateThruHome(R.id.navigation_settings)
        } else {
            controller.popBackStack()
        }
    }

    companion object {
        const val BOUNTY_REQUEST = 3000
        const val LOGIN_REQUEST = 4000
    }
}