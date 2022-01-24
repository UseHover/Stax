package com.hover.stax.home

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.play.core.review.ReviewManagerFactory
import com.hover.sdk.actions.HoverAction
import com.hover.sdk.api.HoverParameters
import com.hover.sdk.permissions.PermissionHelper
import com.hover.stax.R
import com.hover.stax.accounts.Account
import com.hover.stax.accounts.DUMMY
import com.hover.stax.actions.ActionSelectViewModel
import com.hover.stax.balances.BalanceAdapter
import com.hover.stax.balances.BalancesViewModel
import com.hover.stax.channels.Channel
import com.hover.stax.channels.ChannelsViewModel
import com.hover.stax.contacts.PhoneHelper
import com.hover.stax.contacts.StaxContact
import com.hover.stax.databinding.ActivityMainBinding
import com.hover.stax.hover.HoverSession
import com.hover.stax.navigation.AbstractNavigationActivity
import com.hover.stax.paybill.PaybillViewModel
import com.hover.stax.pushNotification.PushNotificationTopicsInterface
import com.hover.stax.requests.NewRequestViewModel
import com.hover.stax.requests.RequestSenderInterface
import com.hover.stax.requests.SmsSentObserver
import com.hover.stax.schedules.Schedule
import com.hover.stax.schedules.ScheduleDetailViewModel
import com.hover.stax.settings.BiometricChecker
import com.hover.stax.settings.SettingsViewModel
import com.hover.stax.settings.SettingsViewModel.Companion.LOGIN_REQUEST
import com.hover.stax.transactions.StaxTransaction
import com.hover.stax.transactions.TransactionHistoryViewModel
import com.hover.stax.transfers.NonStandardVariable
import com.hover.stax.transfers.TransactionType
import com.hover.stax.transfers.TransferViewModel
import com.hover.stax.utils.*
import com.hover.stax.views.StaxDialog
import kotlinx.coroutines.*
import org.json.JSONException
import org.json.JSONObject
import org.koin.androidx.viewmodel.ext.android.getViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class MainActivity : AbstractNavigationActivity(), BalancesViewModel.RunBalanceListener, BalanceAdapter.BalanceListener,
        BiometricChecker.AuthListener, PushNotificationTopicsInterface, RequestSenderInterface, SmsSentObserver.SmsSentListener {

    private val balancesViewModel: BalancesViewModel by viewModel()
    private val historyViewModel: TransactionHistoryViewModel by viewModel()
    private val actionSelectViewModel: ActionSelectViewModel by viewModel()
    private val channelsViewModel: ChannelsViewModel by viewModel()
    private val transferViewModel: TransferViewModel by viewModel()
    private val requestViewModel: NewRequestViewModel by viewModel()
    private val settingsViewModel: SettingsViewModel by viewModel()
    private val paybillViewModel: PaybillViewModel by viewModel()

    private lateinit var scheduleViewModel: ScheduleDetailViewModel

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setUpNav()

        initFromIntent()
        startObservers()
        checkForRequest(intent)
        checkForFragmentDirection(intent)
        checkForDeepLinking()
        observeForAppReview()
        initAuth()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        checkForRequest(intent!!)
    }

    private fun startObservers() {
        with(balancesViewModel) {
            setListener(this@MainActivity)

            //This is to prevent the SAM constructor from being compiled to singleton causing breakages. See
            //https://stackoverflow.com/a/54939860/2371515
            val accountsObserver = object : Observer<List<Account>> {
                override fun onChanged(t: List<Account>?) {
                    logResult("Observing selected channels", t?.size ?: 0)
                }
            }

            accounts.observe(this@MainActivity, accountsObserver)
            toRun.observe(this@MainActivity, { logResult("Observing action to run", it.size) })
            runFlag.observe(this@MainActivity, { logResult("Observing run flag ", it) })
            actions.observe(this@MainActivity, { logResult("Observing actions", it.size) })
        }
    }

    private fun logResult(result: String, size: Int) {
        Timber.i(result.plus(" $size"))
    }

    override fun onResume() {
        super.onResume()
        setUpNav()
    }

    private fun checkForDeepLinking() {
        if (intent.action != null && intent.action == Intent.ACTION_VIEW && intent.data != null) {
            val route = intent.data.toString()

            when {
                route.contains(getString(R.string.deeplink_sendmoney)) ->
                    navigateToTransferFragment(getNavController(), HoverAction.P2P)
                route.contains(getString(R.string.deeplink_airtime)) ->
                    navigateToTransferFragment(getNavController(), HoverAction.AIRTIME)
                route.contains(getString(R.string.deeplink_linkaccount)) ->
                    navigateToChannelsListFragment(getNavController(), true)
                route.contains(getString(R.string.deeplink_balance)) || route.contains(getString(R.string.deeplink_history)) ->
                    navigateToBalanceFragment(getNavController())
                route.contains(getString(R.string.deeplink_settings)) ->
                    navigateToSettingsFragment(getNavController())
                route.contains(getString(R.string.deeplink_reviews)) ->
                    launchStaxReview()
                route.contains(getString(R.string.deeplink_financial_tips)) ->
                    intent.data?.getQueryParameter("id")?.let { navigateToWellnessFragment(getNavController(), it) }
            }

            intent.data = null
        }
    }

    private fun observeForAppReview() = historyViewModel.showAppReviewLiveData().observe(this, { if (it) launchReviewDialog() })

    private fun launchStaxReview() {
        AnalyticsUtil.logAnalyticsEvent(getString(R.string.visited_rating_review_screen), this)

        if (Utils.getBoolean(Constants.APP_RATED_NATIVELY, this))
            openStaxPlaystorePage()
        else
            launchReviewDialog()
    }

    private fun launchReviewDialog() {
        val reviewManager = ReviewManagerFactory.create(this)
        reviewManager.requestReviewFlow().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                reviewManager.launchReviewFlow(this@MainActivity, task.result).addOnCompleteListener {
                    Utils.saveBoolean(Constants.APP_RATED_NATIVELY, true, this@MainActivity)
                }
            }
        }
    }

    private fun openStaxPlaystorePage() {
        val link = Uri.parse(getString(R.string.stax_market_playstore_link))
        val intent = Intent(Intent.ACTION_VIEW, link).apply {
            addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_NEW_DOCUMENT or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
        }

        try {
            startActivity(intent)
        } catch (nf: ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.stax_url_playstore_review_link))))
        }
    }

    private fun checkForRequest(intent: Intent) {
        if (intent.hasExtra(Constants.REQUEST_LINK)) {
            navigateToTransferFragment(getNavController(), HoverAction.P2P)

            createFromRequest(intent.getStringExtra(Constants.REQUEST_LINK)!!)
        }
    }

    private fun checkForFragmentDirection(intent: Intent) {
        if (intent.hasExtra(Constants.FRAGMENT_DIRECT)) {
            val toWhere = intent.extras!!.getInt(Constants.FRAGMENT_DIRECT, 0)
            checkPermissionsAndNavigate(toWhere)
        }
    }

    override fun startRun(actionPair: Pair<Account?, HoverAction>, index: Int) = run(actionPair, index)

    override fun onTapRefresh(accountId: Int) {
        if (accountId == DUMMY)
            checkPermissionsAndNavigate(Constants.NAV_LINK_ACCOUNT)
        else {
            AnalyticsUtil.logAnalyticsEvent(getString(R.string.refresh_balance_single), this)
            balancesViewModel.setRunning(accountId)
        }
    }

    override fun onTapDetail(accountId: Int) {
        if (accountId == DUMMY)
            checkPermissionsAndNavigate(Constants.NAV_LINK_ACCOUNT)
        else
            getNavController().navigate(R.id.action_navigation_home_to_accountDetailsFragment, bundleOf(Constants.ACCOUNT_ID to accountId))
    }

    override fun onAuthError(error: String) {
        Timber.e("Error : $error")
    }

    override fun onAuthSuccess(action: HoverAction?) {
//        run(action!!, 0)
    }

    fun reBuildHoverSession(transaction: StaxTransaction) {
        lifecycleScope.launch(Dispatchers.IO) {
            val actionAndChannelPair = historyViewModel.getActionAndChannel(transaction.action_id, transaction.channel_id)
            val accountNumber = historyViewModel.getAccountNumber(transaction.counterparty_id)

            val hsb = HoverSession.Builder(actionAndChannelPair.first, actionAndChannelPair.second, this@MainActivity, Constants.TRANSFERRED_INT)
                    .extra(HoverAction.AMOUNT_KEY, Utils.formatAmount(transaction.amount.toString()))
                    .extra(HoverAction.ACCOUNT_KEY, accountNumber)
                    .extra(HoverAction.PHONE_KEY, accountNumber)

            runAction(hsb)
        }
    }

    private fun run(actionPair: Pair<Account?, HoverAction>, index: Int) {
        if (balancesViewModel.getChannel(actionPair.second.channel_id) != null) {
            val hsb = HoverSession.Builder(actionPair.second, balancesViewModel.getChannel(actionPair.second.channel_id)!!, this@MainActivity, index)
                    .extra(Constants.ACCOUNT_NAME, actionPair.first?.name)
            actionPair.first?.let { hsb.setAccountId(it.id.toString()) }

            if (index + 1 < balancesViewModel.accounts.value!!.size) hsb.finalScreenTime(0)

            runAction(hsb)
        } else {
//            the only way to get the reference to the observer is to move this out onto it's own block.
            val selectedChannelsObserver = object : Observer<List<Channel>> {
                override fun onChanged(t: List<Channel>?) {
                    if (t != null && balancesViewModel.getChannel(t, actionPair.second.channel_id) != null) {
                        run(actionPair, 0)
                        balancesViewModel.selectedChannels.removeObserver(this)
                    }
                }
            }

            balancesViewModel.selectedChannels.observe(this, selectedChannelsObserver)
        }
    }

    private fun runAction(hsb: HoverSession.Builder) = try {
        hsb.run()
    } catch (e: Exception) {
        runOnUiThread { UIHelper.flashMessage(this, getString(R.string.error_running_action)) }

        val data = JSONObject()
        try {
            data.put("actionId", hsb.action.id)
        } catch (ignored: JSONException) {
        }

        AnalyticsUtil.logAnalyticsEvent("Failed Actions", data, this)
        Timber.e(e)
    }

    private fun onRequest(data: Intent) {
        if (data.action == Constants.SCHEDULED)
            showMessage(getString(R.string.toast_request_scheduled, DateUtils.humanFriendlyDate(data.getLongExtra(Schedule.DATE_KEY, 0))))
        else
            showMessage(getString(R.string.toast_confirm_request))
    }

    private fun showMessage(str: String) = UIHelper.flashMessage(this, findViewById(R.id.fab), str)

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Timber.e("received result. %s", data?.action)
        Timber.e("uuid? %s", data?.extras?.getString("uuid"))

        when {
            requestCode == Constants.TRANSFER_REQUEST && data != null && data.action == Constants.SCHEDULED ->
                showMessage(getString(R.string.toast_confirm_schedule, DateUtils.humanFriendlyDate(data.getLongExtra(Schedule.DATE_KEY, 0))))
            requestCode == Constants.REQUEST_REQUEST -> if (resultCode == RESULT_OK && data != null) onRequest(data)
            requestCode == BOUNTY_REQUEST -> showBountyDetails(data)
            requestCode == LOGIN_REQUEST -> settingsViewModel.signIntoFirebaseAsync(data, findViewById<MaterialCheckBox>(R.id.marketingOptIn)?.isChecked
                    ?: false, this)
            else -> {
                if (requestCode != Constants.TRANSFER_REQUEST) {
                    balancesViewModel.setRan(requestCode)
                    balancesViewModel.showBalances(true)
                }
                showPopUpTransactionDetailsIfRequired(data)
            }
        }
    }

    private fun showBountyDetails(data: Intent?) {
        Timber.e("Request code is bounty")
        if (data != null) {
            val transactionUUID = data.getStringExtra("uuid")
            if (transactionUUID != null) navigateToTransactionDetailsFragment(transactionUUID, supportFragmentManager, true)
        }
    }

    private fun showPopUpTransactionDetailsIfRequired(data: Intent?) {
        if (data != null && data.extras != null && data.extras!!.getString("uuid") != null) {
            navigateToTransactionDetailsFragment(
                    data.extras!!.getString("uuid")!!,
                    supportFragmentManager,
                    false
            )
        }
    }

    fun submit(account: Account, nonStandardVariables: List<NonStandardVariable>? = null) = actionSelectViewModel.activeAction.value?.let { makeHoverCall(it, account, nonStandardVariables) }

    private fun makeHoverCall(action: HoverAction, account: Account, nonStandardVariables: List<NonStandardVariable>?) {
        AnalyticsUtil.logAnalyticsEvent(getString(R.string.finish_transfer, TransactionType.type), this)
        updatePushNotifGroupStatus()

        transferViewModel.checkSchedule()

        makeCall(action, selectedAccount = account, nonStandardVariables = nonStandardVariables)
    }

    private fun getRequestCode(transactionType: String): Int {
        return if (transactionType == HoverAction.FETCH_ACCOUNTS) Constants.FETCH_ACCOUNT_REQUEST
        else Constants.TRANSFER_REQUEST
    }

    private fun makeCall(action: HoverAction, channel: Channel? = null, selectedAccount: Account? = null, nonStandardVariables: List<NonStandardVariable>? = null) {
        val hsb = HoverSession.Builder(action, channel
                ?: channelsViewModel.activeChannel.value!!, this, getRequestCode(action.transaction_type))

        if (action.transaction_type != HoverAction.FETCH_ACCOUNTS) {
            hsb.extra(HoverAction.AMOUNT_KEY, transferViewModel.amount.value)
                    .extra(HoverAction.NOTE_KEY, transferViewModel.note.value)
                    .extra(Constants.ACCOUNT_NAME, selectedAccount?.name)

            if(!nonStandardVariables.isNullOrEmpty()) {
                nonStandardVariables.forEach {
                    hsb.extra(it.key, it.value)
                }
            }

            selectedAccount?.run { hsb.setAccountId(id.toString()) }
            transferViewModel.contact.value?.let { addRecipientInfo(hsb) }
        }

        runAction(hsb)
    }

    fun submitPaymentRequest(action: HoverAction, channel: Channel, account: Account) {
        val hsb = HoverSession.Builder(action, channel, this, Constants.PAYBILL_REQUEST)
                .extra(HoverAction.AMOUNT_KEY, paybillViewModel.amount.value)
                .extra("businessNo", paybillViewModel.businessNumber.value)
                .extra(Constants.ACCOUNT_NAME, account.name)
                .extra(HoverAction.ACCOUNT_KEY, paybillViewModel.accountNumber.value)
        hsb.setAccountId(account.id.toString())

        runAction(hsb)

        val data = JSONObject()
        try {
            data.put("businessNo", paybillViewModel.businessNumber.value)
        } catch(e: Exception) {
            Timber.e(e)
        }

        AnalyticsUtil.logAnalyticsEvent(getString(R.string.finish_transfer, TransactionType.type), data, this)
    }

    private fun addRecipientInfo(hsb: HoverSession.Builder) {
        hsb.extra(HoverAction.ACCOUNT_KEY, transferViewModel.contact.value!!.accountNumber)
                .extra(
                        HoverAction.PHONE_KEY, PhoneHelper.getNumberFormatForInput(
                        transferViewModel.contact.value?.accountNumber,
                        actionSelectViewModel.activeAction.value, channelsViewModel.activeChannel.value
                )
                )
    }

    private fun updatePushNotifGroupStatus() {
        joinTransactionGroup(this)
        leaveNoUsageGroup(this)
    }

    private fun updatePushNotifGroupStatus(a: HoverAction) {
        joinAllBountiesGroup(this)
        joinBountyCountryGroup(a.country_alpha2.uppercase(), this)
    }

    private fun returnResult(type: Int, result: Int, data: Intent?) {
        val i = data?.let { Intent(it) } ?: Intent()
        transferViewModel.contact.value?.let { i.putExtra(StaxContact.ID_KEY, it.lookupKey) }
        i.action = if (type == Constants.SCHEDULE_REQUEST) Constants.SCHEDULED else Constants.TRANSFERRED
        setResult(result, i)
    }

    private fun initFromIntent() {
        when {
            intent.hasExtra(Schedule.SCHEDULE_ID) -> createFromSchedule(intent.getIntExtra(Schedule.SCHEDULE_ID, -1), intent.getBooleanExtra(Constants.REQUEST_TYPE, false))
            intent.hasExtra(Constants.REQUEST_LINK) -> createFromRequest(intent.getStringExtra(Constants.REQUEST_LINK)!!)
            else -> AnalyticsUtil.logAnalyticsEvent(getString(R.string.visit_screen, intent.action), this)
        }
    }

    private fun createFromSchedule(scheduleId: Int, isRequestType: Boolean) {
        scheduleViewModel = getViewModel()
        with(scheduleViewModel) {
            if (isRequestType) {
                schedule.observe(this@MainActivity) { it?.let { requestViewModel.setSchedule(it) } }
            } else {
                action.observe(this@MainActivity) { it?.let { actionSelectViewModel.setActiveAction(it) } }
                schedule.observe(this@MainActivity) { it?.let { transferViewModel.view(it) } }
            }

            setSchedule(scheduleId)
        }

        AnalyticsUtil.logAnalyticsEvent(getString(R.string.clicked_schedule_notification), this)
    }

    private fun createFromRequest(link: String) {
        transferViewModel.decrypt(link)
        observeRequest()
        AnalyticsUtil.logAnalyticsEvent(getString(R.string.clicked_request_link), this)
    }

    private fun observeRequest() {
        val alertDialog = StaxDialog(this).setDialogMessage(R.string.loading_link_dialoghead).showIt()
        transferViewModel.request.observe(this@MainActivity, { it?.let { alertDialog?.dismiss() } })
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

    fun sendSms() {
        requestViewModel.saveRequest()
        SmsSentObserver(this, listOf(requestViewModel.requestee.value), Handler(), this).start()
        sendSms(requestViewModel.formulatedRequest.value, listOf(requestViewModel.requestee.value), this)
    }

    fun sendWhatsapp() {
        requestViewModel.saveRequest()
        sendWhatsapp(requestViewModel.formulatedRequest.value, listOf(requestViewModel.requestee.value), requestViewModel.activeChannel.value, this)
    }

    fun copyShareLink(view: View) {
        requestViewModel.saveRequest()
        copyShareLink(requestViewModel.formulatedRequest.value, view.findViewById(R.id.copylink_share_selection), this)
    }

    override fun onSmsSendEvent(sent: Boolean) {
        if (sent) onFinished(Constants.SMS)
    }

    private fun onFinished(type: Int) = setResult(RESULT_OK, createSuccessIntent(type))

    private fun createSuccessIntent(type: Int): Intent =
            Intent().apply { action = if (type == Constants.SCHEDULE_REQUEST) Constants.SCHEDULED else Constants.TRANSFERRED }

    fun cancel() = setResult(RESULT_CANCELED)

    fun makeCall(action: HoverAction, channel: Channel) {
        val hsb = HoverSession.Builder(action, channel, this, Constants.REQUEST_REQUEST)
        runAction(hsb)
    }

    fun makeCall(a: HoverAction) {
        AnalyticsUtil.logAnalyticsEvent(getString(R.string.clicked_run_bounty_session), this)
        updatePushNotifGroupStatus(a)
        call(a.public_id)
    }

    fun retryCall(actionId: String) {
        AnalyticsUtil.logAnalyticsEvent(getString(R.string.clicked_retry_bounty_session), this)
        call(actionId)
    }

    private fun call(actionId: String) {
        val i = HoverParameters.Builder(this).request(actionId).setEnvironment(HoverParameters.MANUAL_ENV).buildIntent()
        startActivityForResult(i, BOUNTY_REQUEST)
    }

    fun initAuth() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.google_server_client_id))
                .requestEmail()
                .build()
        settingsViewModel.signInClient = GoogleSignIn.getClient(this, gso)
    }

    fun signIn() = startActivityForResult(settingsViewModel.signInClient.signInIntent, LOGIN_REQUEST)

    companion object {
        private const val BOUNTY_REQUEST = 3000
    }

}