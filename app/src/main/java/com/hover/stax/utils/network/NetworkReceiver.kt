package com.hover.stax.utils.network

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class NetworkReceiver: BroadcastReceiver(){

    private lateinit var networkMonitor: NetworkMonitor

    override fun onReceive(context: Context?, intent: Intent?) {
        context?.let {
            networkMonitor = NetworkMonitor(it)
            networkMonitor.isNetworkAvailable()
        }

    }
}