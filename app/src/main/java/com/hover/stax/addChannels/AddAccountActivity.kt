package com.hover.stax.addChannels

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import com.hover.sdk.actions.HoverAction
import com.hover.stax.R
import com.hover.stax.channels.UpdateChannelsWorker
import com.hover.stax.domain.model.USSDAccount
import com.hover.stax.hover.HoverSession
import com.hover.stax.hover.TransactionContract
import com.hover.stax.presentation.add_accounts.AddAccountNavHost
import com.hover.stax.utils.AnalyticsUtil
import com.hover.stax.utils.NavUtil
import com.hover.stax.utils.UIHelper
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class AddAccountActivity : AppCompatActivity() {

    private val viewModel: AddAccountViewModel by viewModel()
    private val usdcViewModel: UsdcViewModel by viewModel()

    @ExperimentalAnimationApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        refreshChannels()
        AnalyticsUtil.logAnalyticsEvent(getString(R.string.visit_screen, getString(R.string.visit_link_account)), this)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.checkBalanceEvent.collect {
                        callHover(checkBalance, generateSessionBuilder(viewModel.account.first(), it))
                }}
                launch {
                    viewModel.doneEvent.collect { isDone ->
                        if (isDone) { finish() }
                }}
                launch {
                    usdcViewModel.doneEvent.collect { isDone ->
                        if (isDone) { finish() }
                }}
            }
        }

        setContent {
            AddAccountNavHost()
        }
    }

    private fun generateSessionBuilder(account: USSDAccount, action: HoverAction): HoverSession.Builder {
        return HoverSession.Builder(action, account, this)
    }

    private val checkBalance = registerForActivityResult(TransactionContract()) { data: Intent? ->
        Timber.e("got check balance result")
        if (data != null && data.extras != null && data.extras!!.getString("uuid") != null) {
//            NavUtil.showTransactionDetailsFragment(navController, data.extras!!.getString("uuid")!!)
            Timber.e("finishing")
            finish()
        }
    }

    private fun callHover(launcher: ActivityResultLauncher<HoverSession.Builder>, b: HoverSession.Builder) {
        Timber.e("calling hover")
        try {
            launcher.launch(b)
        } catch (e: Exception) {
            runOnUiThread { UIHelper.flashAndReportMessage(this, getString(
                R.string.error_running_action)) }
            AnalyticsUtil.logErrorAndReportToFirebase(b.action.public_id, getString(R.string.error_running_action_log), e)
        }
    }

    private fun refreshChannels() {
        val wm = WorkManager.getInstance(this)
        wm.beginUniqueWork(
            UpdateChannelsWorker.CHANNELS_WORK_ID, ExistingWorkPolicy.KEEP,
            UpdateChannelsWorker.makeWork()
        ).enqueue()
    }
}