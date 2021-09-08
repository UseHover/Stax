package com.hover.stax.bounties


import android.content.Intent
import android.os.Bundle
import com.appsflyer.AppsFlyerLib
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.hover.sdk.actions.HoverAction
import com.hover.sdk.api.HoverParameters
import com.hover.stax.R
import com.hover.stax.balances.BalancesFragment
import com.hover.stax.databinding.ActivityBountyBinding
import com.hover.stax.navigation.AbstractNavigationActivity
import com.hover.stax.pushNotification.PushNotificationTopicsInterface
import com.hover.stax.utils.Utils
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber


class BountyActivity : AbstractNavigationActivity(), PushNotificationTopicsInterface {

    private val bountyViewModel: BountyViewModel by viewModel()

    private lateinit var auth: FirebaseAuth
    private lateinit var signInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Utils.logAnalyticsEvent(getString(R.string.visit_screen, BountyActivity::class.java.simpleName), this)

        val binding = ActivityBountyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setUpNav()

        initAuth()

        if (auth.currentUser == null)
            Utils.logAnalyticsEvent(getString(R.string.visit_screen, getString(R.string.visit_bounty_email)), this)
    }

    private fun initAuth() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.google_server_client_id))
                .requestEmail()
                .build()
        signInClient = GoogleSignIn.getClient(this, gso)
        auth = Firebase.auth
    }

    fun signIn() {
        val signInIntent = signInClient.signInIntent
        startActivityForResult(signInIntent, LOGIN_REQUEST)
    }

    override fun onStart() {
        super.onStart()
        AppsFlyerLib.getInstance().start(this)

        val currentUser = auth.currentUser
        currentUser?.let {
            navigateToBountyListFragment(getNavController())
        }
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
        Timber.d("called on activity result")
        if (requestCode == BOUNTY_REQUEST) {
            if (data != null) {
                val transactionUUID = data.getStringExtra("uuid")
                if (transactionUUID != null) navigateToTransactionDetailsFragment(transactionUUID, supportFragmentManager, true)
            }
        } else if (requestCode == LOGIN_REQUEST) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                fetchAccount(account.idToken!!)
            } catch (e: ApiException) {
                Timber.e(e, "Google sign in failed")
                bountyViewModel.setLoginfailed(true)
            }
        }
    }

    private fun fetchAccount(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this) {
                    if (it.isSuccessful) {
                        Timber.i("Sign in with credential: success")
                        auth.currentUser?.let { user -> bountyViewModel.setUser(user) }
                    } else {
                        bountyViewModel.setLoginfailed(true)
                        Timber.e(it.exception, "Sign in with credential failed")
                    }
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
        const val EMAIL_KEY = "email_for_bounties"
        const val BOUNTY_REQUEST = 3000
        const val LOGIN_REQUEST = 4000
    }
}