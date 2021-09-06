package com.hover.stax.bounties


import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import com.appsflyer.AppsFlyerLib
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.hover.sdk.actions.HoverAction
import com.hover.sdk.api.HoverParameters
import com.hover.stax.R
import com.hover.stax.databinding.ActivityBountyBinding
import com.hover.stax.navigation.AbstractNavigationActivity
import com.hover.stax.pushNotification.PushNotificationTopicsInterface
import com.hover.stax.utils.Utils
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber


class BountyActivity : AbstractNavigationActivity(), PushNotificationTopicsInterface {

    private val bountyViewModel: BountyViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Utils.logAnalyticsEvent(getString(R.string.visit_screen, BountyActivity::class.java.simpleName), this)

        val binding = ActivityBountyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setUpNav()

        if (!Utils.getString(EMAIL_KEY, this).isNullOrEmpty())
            navigateToBountyListFragment(getNavController())
        else
            Utils.logAnalyticsEvent(getString(R.string.visit_screen, getString(R.string.visit_bounty_email)), this)
    }

    override fun onStart() {
        super.onStart()
        AppsFlyerLib.getInstance().start(this)
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