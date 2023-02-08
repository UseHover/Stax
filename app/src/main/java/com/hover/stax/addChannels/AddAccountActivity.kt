package com.hover.stax.addChannels

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import com.hover.stax.R
import com.hover.stax.channels.UpdateChannelsWorker
import com.hover.stax.presentation.add_accounts.AddAccountNavHost
import com.hover.stax.utils.AnalyticsUtil

class AddAccountActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        refreshChannels()
        AnalyticsUtil.logAnalyticsEvent(getString(R.string.visit_screen, getString(R.string.visit_link_account)), this)

        setContent {
            AddAccountNavHost()
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