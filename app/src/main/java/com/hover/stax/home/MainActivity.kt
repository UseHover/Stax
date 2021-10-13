package com.hover.stax.home

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.google.android.play.core.review.ReviewManagerFactory
import com.hover.sdk.actions.HoverAction
import com.hover.stax.R
import com.hover.stax.account.Account
import com.hover.stax.account.DUMMY
import com.hover.stax.balances.BalanceAdapter
import com.hover.stax.balances.BalancesViewModel
import com.hover.stax.channels.Channel
import com.hover.stax.databinding.ActivityMainBinding
import com.hover.stax.hover.HoverSession
import com.hover.stax.navigation.AbstractNavigationActivity
import com.hover.stax.schedules.Schedule
import com.hover.stax.settings.BiometricChecker
import com.hover.stax.transactions.StaxTransaction
import com.hover.stax.transactions.TransactionHistoryViewModel
import com.hover.stax.utils.Constants
import com.hover.stax.utils.DateUtils
import com.hover.stax.utils.UIHelper
import com.hover.stax.utils.Utils
import kotlinx.coroutines.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class MainActivity : AbstractNavigationActivity(),
        BalancesViewModel.RunBalanceListener,
        BalanceAdapter.BalanceListener,
        BiometricChecker.AuthListener {

    private val balancesViewModel: BalancesViewModel by viewModel()
    private val historyViewModel: TransactionHistoryViewModel by viewModel()

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        startObservers()
        checkForRequest(intent)
        checkForFragmentDirection(intent)
        checkForDeepLinking()
        observeForAppReview()
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
        Timber.e(result.plus(" $size"))
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
                    navigateToTransferActivity(HoverAction.P2P, false, intent, this)
                route.contains(getString(R.string.deeplink_airtime)) ->
                    navigateToTransferActivity(HoverAction.AIRTIME, false, intent, this)
                route.contains(getString(R.string.deeplink_linkaccount)) ->
                    navigateToChannelsListFragment(getNavController(), true)
                route.contains(getString(R.string.deeplink_balance)) || route.contains(getString(R.string.deeplink_history)) ->
                    navigateToBalanceFragment(getNavController())
                route.contains(getString(R.string.deeplink_settings)) ->
                    navigateToSettingsFragment(getNavController())
                route.contains(getString(R.string.deeplink_reviews)) ->
                    launchStaxReview()
            }

            intent.data = null
        }
    }

    private fun observeForAppReview() = historyViewModel.showAppReviewLiveData().observe(this, { if (it) launchReviewDialog() })

    private fun launchStaxReview() {
        Utils.logAnalyticsEvent(getString(R.string.visited_rating_review_screen), this)

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
        if (intent.hasExtra(Constants.REQUEST_LINK)) navigateToTransferActivity(HoverAction.P2P, true, intent, this)
    }

    private fun checkForFragmentDirection(intent: Intent) {
        if (intent.hasExtra(Constants.FRAGMENT_DIRECT)) {
            val toWhere = intent.extras!!.getInt(Constants.FRAGMENT_DIRECT, 0)
            checkPermissionsAndNavigate(toWhere)
        }
    }

    override fun startRun(actionPair: Pair<Account?, HoverAction>, index: Int) {
        Timber.e("Starting run for ${actionPair.first?.name} - ${actionPair.second.transaction_type}")
        run(actionPair, index)
    }

    override fun onTapRefresh(accountId: Int) {
        if (accountId == DUMMY)
            checkPermissionsAndNavigate(Constants.NAV_LINK_ACCOUNT)
        else {
            Utils.logAnalyticsEvent(getString(R.string.refresh_balance_single), this)
            balancesViewModel.setRunning(accountId)
        }
    }

    override fun onTapDetail(accountId: Int) {
        if (accountId == DUMMY)
            checkPermissionsAndNavigate(Constants.NAV_LINK_ACCOUNT)
        else
            navigateToAccountDetailsFragment(accountId, getNavController())
    }

    override fun onAuthError(error: String?) {
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

            hsb.run()
        }
    }

    private fun run(actionPair: Pair<Account?, HoverAction>, index: Int) {
        if (balancesViewModel.getChannel(actionPair.second.channel_id) != null) {
            val hsb = HoverSession.Builder(actionPair.second, balancesViewModel.getChannel(actionPair.second.channel_id)!!, this@MainActivity, index)
                    .extra(Constants.ACCOUNT_NAME, actionPair.first?.name)
            actionPair.first?.let { hsb.setAccountId(it.id.toString()) }

            if (index + 1 < balancesViewModel.accounts.value!!.size) hsb.finalScreenTime(0)

            hsb.run()
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

    private fun onProbableHoverCall(data: Intent) {
        if (data.action != null && data.action == Constants.SCHEDULED) {
            showMessage(getString(R.string.toast_confirm_schedule, DateUtils.humanFriendlyDate(data.getLongExtra(Schedule.DATE_KEY, 0))))
        } else {
            Utils.logAnalyticsEvent(getString(R.string.finish_load_screen), this)
            historyViewModel.saveTransaction(data, this)
        }
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

        showPopUpTransactionDetailsIfRequired(requestCode, data)
        handleAllOtherResults(requestCode, resultCode, data)
    }

    private fun handleAllOtherResults(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            Constants.TRANSFER_REQUEST -> data?.let { onProbableHoverCall(it) }
            Constants.REQUEST_REQUEST -> if (resultCode == RESULT_OK) onRequest(data!!)
            else -> {
                balancesViewModel.setRan(requestCode)
                if (resultCode == RESULT_OK && data != null && data.action != null) onProbableHoverCall(data)

                balancesViewModel.showBalances(true)
            }
        }
    }

    private fun showPopUpTransactionDetailsIfRequired(requestCode: Int, data: Intent?) {
        data?.let {
            if (it.action.equals(Constants.TRANSFERRED) || requestCode == Constants.TRANSFERRED_INT) {
                val uuid = it.extras?.getString("uuid")
                uuid?.let {
                    showTransactionPopup(uuid);
                }
                return@let
            }
        }
    }

    private fun showTransactionPopup(uuid: String) {
        navigateToTransactionDetailsFragment(uuid, supportFragmentManager, false)
    }
}