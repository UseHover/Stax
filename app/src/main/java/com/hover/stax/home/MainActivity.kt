package com.hover.stax.home

import android.content.Intent
import android.os.Bundle
import androidx.navigation.NavDirections
import com.hover.sdk.actions.HoverAction
import com.hover.sdk.api.Hover
import com.hover.sdk.permissions.PermissionHelper
import com.hover.stax.FRAGMENT_DIRECT
import com.hover.stax.MainNavigationDirections
import com.hover.stax.R
import com.hover.stax.bonus.BonusViewModel
import com.hover.stax.databinding.ActivityMainBinding
import com.hover.stax.presentation.financial_tips.FinancialTipsFragment
import com.hover.stax.login.AbstractGoogleAuthActivity
import com.hover.stax.notifications.PushNotificationTopicsInterface
import com.hover.stax.presentation.home.HomeViewModel
import com.hover.stax.requests.NewRequestViewModel
import com.hover.stax.requests.REQUEST_LINK
import com.hover.stax.requests.RequestSenderInterface
import com.hover.stax.requests.SMS
import com.hover.stax.settings.BiometricChecker
import com.hover.stax.transactions.TransactionHistoryViewModel
import com.hover.stax.transfers.TransferViewModel
import com.hover.stax.utils.AnalyticsUtil
import com.hover.stax.utils.UIHelper
import com.hover.stax.utils.Utils
import com.hover.stax.views.StaxDialog
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class MainActivity : AbstractGoogleAuthActivity(), BiometricChecker.AuthListener, PushNotificationTopicsInterface, RequestSenderInterface {

    private val transferViewModel: TransferViewModel by viewModel()
    private val requestViewModel: NewRequestViewModel by viewModel()
    private val historyViewModel: TransactionHistoryViewModel by viewModel()
    private val bonusViewModel: BonusViewModel by viewModel()

    private val homeViewModel: HomeViewModel by viewModel()

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        navHelper = NavHelper(this)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        navHelper.setUpNav()

        initFromIntent()
        checkForRequest(intent)
        checkForFragmentDirection(intent)
        observeForAppReview()
        setGoogleLoginInterface(this)

        bonusViewModel.fetchBonuses()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        checkForRequest(intent!!)
        checkForFragmentDirection(intent)
    }

    override fun onResume() {
        super.onResume()
        navHelper.setUpNav()
    }

    fun checkPermissionsAndNavigate(navDirections: NavDirections) {
        navHelper.checkPermissionsAndNavigate(navDirections)
    }

    private fun observeForAppReview() = historyViewModel.showAppReviewLiveData().observe(this@MainActivity) {
        if (it) StaxAppReview.launchStaxReview(this@MainActivity)
    }

    private fun checkForRequest(intent: Intent) {
        if (intent.hasExtra(REQUEST_LINK)) {
            createFromRequest(intent.getStringExtra(REQUEST_LINK)!!)
        }
    }

    private fun checkForFragmentDirection(intent: Intent) {
        if (intent.hasExtra(FRAGMENT_DIRECT)) {
            val toWhere = intent.extras!!.getInt(FRAGMENT_DIRECT, 0)

            if (toWhere == NAV_EMAIL_CLIENT)
                Utils.openEmail(getString(R.string.stax_emailing_subject, Hover.getDeviceId(this)), this)
            else
                navHelper.checkPermissionsAndNavigate(toWhere)
        }
    }

    private fun initFromIntent() {
        when {
            intent.hasExtra(REQUEST_LINK) -> createFromRequest(intent.getStringExtra(REQUEST_LINK)!!)
            intent.hasExtra(FinancialTipsFragment.TIP_ID) -> navHelper.navigateWellness(intent.getStringExtra(FinancialTipsFragment.TIP_ID)!!)
            else -> AnalyticsUtil.logAnalyticsEvent(getString(R.string.visit_screen, intent.action), this)
        }
    }

    private fun createFromRequest(link: String) {
        navHelper.checkPermissionsAndNavigate(MainNavigationDirections.actionGlobalTransferFragment(HoverAction.P2P))
        addLoadingDialog()
        transferViewModel.load(link)
        AnalyticsUtil.logAnalyticsEvent(getString(R.string.clicked_request_link), this)
    }

    private fun addLoadingDialog() {
        val alertDialog = StaxDialog(this).setDialogMessage(R.string.loading_link_dialoghead).showIt()
        transferViewModel.isLoading.observe(this@MainActivity) { if (!it) alertDialog?.dismiss() }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == SMS && PermissionHelper(this).permissionsGranted(grantResults)) {
            AnalyticsUtil.logAnalyticsEvent(getString(R.string.perms_sms_granted), this)
            sendSms(requestViewModel, this)
        } else if (requestCode == SMS) {
            AnalyticsUtil.logAnalyticsEvent(getString(R.string.perms_sms_denied), this)
            UIHelper.flashMessage(this, getString(R.string.toast_error_smsperm))
        }
    }

    override fun onAuthError(error: String) {
        Timber.e("Error : $error")
    }

    override fun onAuthSuccess(action: HoverAction?) {
        Timber.v("Auth success on action: ${action?.public_id}")
    }
}