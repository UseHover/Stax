package com.hover.stax.utils.network

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class NetworkReceiver: BroadcastReceiver(), KoinComponent {

    private val networkMonitor: NetworkMonitor by inject()

    override fun onReceive(context: Context?, intent: Intent?) {
        networkMonitor.isNetworkAvailable()
    }
}