package com.hover.stax.addChannels

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import com.hover.stax.R
import com.hover.stax.channels.UpdateChannelsWorker
import com.hover.stax.presentation.add_accounts.AddAccountNavHost
import com.hover.stax.transfers.TransferViewModel
import com.hover.stax.utils.AnalyticsUtil
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class AddAccountActivity : AppCompatActivity() {

    private val viewModel: ChannelsViewModel by viewModel()

    @ExperimentalAnimationApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        refreshChannels()
        AnalyticsUtil.logAnalyticsEvent(getString(R.string.visit_screen, getString(R.string.visit_link_account)), this)

        waitForAccountCreation()

        setContent {
            AddAccountNavHost()
        }
    }

    private fun waitForAccountCreation() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.accountEventFlow.collect { isCreated ->
                    if (isCreated) {
                        finish()
                    }
                }
            }
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