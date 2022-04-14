package com.hover.stax.home

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.navigation.NavDirections
import com.hover.sdk.actions.HoverAction
import com.hover.sdk.api.Hover
import com.hover.sdk.permissions.PermissionHelper
import com.hover.stax.MainNavigationDirections
import com.hover.stax.R
import com.hover.stax.accounts.Account
import com.hover.stax.accounts.DUMMY
import com.hover.stax.actions.ActionSelectViewModel
import com.hover.stax.balances.BalanceAdapter
import com.hover.stax.balances.BalancesViewModel
import com.hover.stax.contacts.StaxContact
import com.hover.stax.databinding.ActivityMainBinding
import com.hover.stax.financialTips.FinancialTipsFragment
import com.hover.stax.login.LoginViewModel
import com.hover.stax.login.StaxGoogleLoginInterface
import com.hover.stax.notifications.PushNotificationTopicsInterface
import com.hover.stax.schedules.Schedule
import com.hover.stax.settings.BiometricChecker
import com.hover.stax.settings.SettingsFragment
import com.hover.stax.transactions.TransactionDetailsFragment
import com.hover.stax.transactions.TransactionHistoryViewModel
import com.hover.stax.transactions.USSDLogBottomSheetFragment
import com.hover.stax.transfers.TransferViewModel
import com.hover.stax.utils.*
import com.hover.stax.views.StaxDialog
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class MainActivity : AbstractRequestActivity(), BalancesViewModel.RunBalanceListener, BalanceAdapter.BalanceListener,
    BiometricChecker.AuthListener, PushNotificationTopicsInterface, StaxGoogleLoginInterface {

    private val balancesViewModel: BalancesViewModel by viewModel()
    private val actionSelectViewModel: ActionSelectViewModel by viewModel()
    private val transferViewModel: TransferViewModel by viewModel()
    private val historyViewModel: TransactionHistoryViewModel by viewModel()
    private val loginViewModel: LoginViewModel by viewModel()
    private val bountyRequest = 3000

    private lateinit var binding: ActivityMainBinding

    private lateinit var navHelper: NavHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        navHelper = NavHelper(this)
        setContentView(binding.root)

        navHelper.setUpNav()

        initFromIntent()
        startObservers()
        checkForRequest(intent)
        checkForFragmentDirection(intent)
        observeForAppReview()
        setGoogleLoginInterface(this)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        checkForRequest(intent!!)
    }

    override fun onResume() {
        super.onResume()
        navHelper.setUpNav()
    }

    fun submit(account: Account) = actionSelectViewModel.activeAction.value?.let { makeHoverCall(it, account) }

    fun checkPermissionsAndNavigate(navDirections: NavDirections) {
        navHelper.checkPermissionsAndNavigate(navDirections)
    }

    fun showUSSDLogBottomSheet(uuid: String) {
        USSDLogBottomSheetFragment().apply {
            val bundle = Bundle()
            bundle.putString(TransactionDetailsFragment.UUID, uuid)
            arguments = bundle
            show(supportFragmentManager, tag)
        }
    }

    fun navigateTransferAutoFill(type: String, transactionUUID: String) {
        navHelper.navigateTransfer(type)
        transferViewModel.autoFill(transactionUUID)
    }

    private fun observeForAppReview() = historyViewModel.showAppReviewLiveData().observe(this@MainActivity) {
        if (it) StaxAppReview.launchStaxReview(this@MainActivity)
    }

    private fun startObservers() {
        with(balancesViewModel) {
            setListener(this@MainActivity)

            //This is to prevent the SAM constructor from being compiled to singleton causing breakages. See
            //https://stackoverflow.com/a/54939860/2371515
            val accountsObserver = Observer<List<Account>> { t -> logResult("Observing selected channels", t?.size ?: 0) }

            accounts.observe(this@MainActivity, accountsObserver)
            toRun.observe(this@MainActivity) { logResult("Observing action to run", it.size) }
            runFlag.observe(this@MainActivity) { logResult("Observing run flag ", it) }
            actions.observe(this@MainActivity) { logResult("Observing actions", it.size) }
        }
    }

    private fun logResult(result: String, size: Int) {
        Timber.i(result.plus(" $size"))
    }

    private fun showMessage(str: String) = UIHelper.flashMessage(this, findViewById(R.id.fab), str)

    private fun checkForRequest(intent: Intent) {
        if (intent.hasExtra(Constants.REQUEST_LINK)) {
            navHelper.checkPermissionsAndNavigate(MainNavigationDirections.actionGlobalTransferFragment(HoverAction.P2P))
            createFromRequest(intent.getStringExtra(Constants.REQUEST_LINK)!!)
        }
    }

    private fun checkForFragmentDirection(intent: Intent) {
        if (intent.hasExtra(Constants.FRAGMENT_DIRECT)) {
            val toWhere = intent.extras!!.getInt(Constants.FRAGMENT_DIRECT, 0)

            if (toWhere == Constants.NAV_EMAIL_CLIENT)
                Utils.openEmail(getString(R.string.stax_emailing_subject, Hover.getDeviceId(this)), this)
            else
                navHelper.checkPermissionsAndNavigate(toWhere)
        }
    }

    private fun onRequest(data: Intent) {
        if (data.action == Constants.SCHEDULED)
            showMessage(getString(R.string.toast_request_scheduled, DateUtils.humanFriendlyDate(data.getLongExtra(Schedule.DATE_KEY, 0))))
        else
            showMessage(getString(R.string.toast_confirm_request))
    }

    private fun showBountyDetails(data: Intent?) {
        Timber.e("Request code is bounty")
        if (data != null) {
            val transactionUUID = data.getStringExtra("uuid")
            if (transactionUUID != null) NavUtil.showTransactionDetailsFragment(transactionUUID, supportFragmentManager, true)
        }
    }

    private fun showPopUpTransactionDetailsIfRequired(data: Intent?) {
        if (data != null && data.extras != null && data.extras!!.getString("uuid") != null) {
            NavUtil.showTransactionDetailsFragment(
                data.extras!!.getString("uuid")!!,
                supportFragmentManager,
                false
            )
        }
    }

    private fun initFromIntent() {
        when {
            intent.hasExtra(Schedule.SCHEDULE_ID) -> createFromSchedule(
                intent.getIntExtra(Schedule.SCHEDULE_ID, -1),
                intent.getBooleanExtra(Constants.REQUEST_TYPE, false)
            )
            intent.hasExtra(Constants.REQUEST_LINK) -> createFromRequest(intent.getStringExtra(Constants.REQUEST_LINK)!!)
            intent.hasExtra(FinancialTipsFragment.TIP_ID) -> navHelper.navigateWellness(intent.getStringExtra(FinancialTipsFragment.TIP_ID)!!)
            else -> AnalyticsUtil.logAnalyticsEvent(getString(R.string.visit_screen, intent.action), this)
        }
    }

    private fun createFromRequest(link: String) {
        transferViewModel.decrypt(link)
        observeRequest()
        AnalyticsUtil.logAnalyticsEvent(getString(R.string.clicked_request_link), this)
    }

    private fun observeRequest() {
        val alertDialog = StaxDialog(this).setDialogMessage(R.string.loading_link_dialoghead).showIt()
        transferViewModel.request.observe(this@MainActivity) { it?.let { alertDialog?.dismiss() } }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Constants.SMS && PermissionHelper(this).permissionsGranted(grantResults)) {
            AnalyticsUtil.logAnalyticsEvent(getString(R.string.perms_sms_granted), this)
            sendSms()
        } else if (requestCode == Constants.SMS) {
            AnalyticsUtil.logAnalyticsEvent(getString(R.string.perms_sms_denied), this)
            UIHelper.flashMessage(this, getString(R.string.toast_error_smsperm))
        }
    }

    override fun startRun(actionPair: Pair<Account?, HoverAction>, index: Int) = run(actionPair, index)

    override fun onTapRefresh(accountId: Int) {
        if (accountId == DUMMY)
            checkPermissionsAndNavigate(HomeFragmentDirections.actionNavigationHomeToNavigationLinkAccount())
        else {
            AnalyticsUtil.logAnalyticsEvent(getString(R.string.refresh_balance_single), this)
            balancesViewModel.setRunning(accountId)
        }
    }

    override fun onTapDetail(accountId: Int) {
        if (accountId == DUMMY)
            checkPermissionsAndNavigate(HomeFragmentDirections.actionNavigationHomeToNavigationLinkAccount())
        else
            navHelper.navigateAccountDetails(accountId)
    }

    override fun onAuthError(error: String) {
        Timber.e("Error : $error")
    }

    override fun onAuthSuccess(action: HoverAction?) {
        Timber.e("Auth success on action: ${action?.public_id}")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Timber.e("received result. %s", data?.action)
        Timber.e("uuid? %s", data?.extras?.getString("uuid"))

        when {
            requestCode == Constants.TRANSFER_REQUEST && data != null && data.action == Constants.SCHEDULED ->
                showMessage(getString(R.string.toast_confirm_schedule, DateUtils.humanFriendlyDate(data.getLongExtra(Schedule.DATE_KEY, 0))))
            requestCode == Constants.REQUEST_REQUEST -> if (resultCode == RESULT_OK && data != null) onRequest(data)
            requestCode == bountyRequest -> showBountyDetails(data)
            else -> {
                if (requestCode != Constants.TRANSFER_REQUEST) {
                    balancesViewModel.setRan(requestCode)
                    balancesViewModel.showBalances(true)
                }
                showPopUpTransactionDetailsIfRequired(data)
            }
        }
    }

    override fun googleLoginSuccessful() {
        if (loginViewModel.postGoogleAuthNav.value == SettingsFragment.SHOW_BOUNTY_LIST) navHelper.navigateToBountyList()
    }

    override fun googleLoginFailed() {
        UIHelper.flashMessage(this, R.string.login_google_err)
    }
}