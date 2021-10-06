package com.hover.stax.utils.network

import android.Manifest
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import androidx.annotation.MainThread
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.lifecycle.MutableLiveData
import timber.log.Timber
import kotlin.properties.Delegates


class NetworkMonitor
@RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
constructor(val context: Context) {

    private val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    @RequiresApi(21)
    fun startNetworkCallback() {
        val builder: NetworkRequest.Builder = NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)

        if (Build.VERSION.SDK_INT < 24)
            cm.registerNetworkCallback(builder.build(), connectivityManagerCallback)
        else
            cm.registerDefaultNetworkCallback(connectivityManagerCallback)
    }

    @RequiresApi(21)
    fun stopNetworkCallback() = try {
        cm.unregisterNetworkCallback(connectivityManagerCallback)
    } catch (ignored: Exception) {
        Timber.e("Network callback already unregistered.")
    }

    @RequiresApi(21)
    private val connectivityManagerCallback = object : ConnectivityManager.NetworkCallback() {

        override fun onAvailable(network: Network) {
            isNetworkConnected = true
        }

        override fun onLost(network: Network) {
            isNetworkConnected = false
        }

    }

    fun isNetworkAvailable() {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        connectivityManager?.let {
            val activeNetworkInfo = it.activeNetworkInfo
            isNetworkConnected = activeNetworkInfo != null && activeNetworkInfo.isConnected
        }
    }

    var isNetworkConnected: Boolean by Delegates.observable(true, { _, _, newValue ->
        StateLiveData.get().postValue(newValue)
    })

    class StateLiveData : MutableLiveData<Boolean>() {

        companion object {
            private lateinit var instance: StateLiveData

            @MainThread
            fun get(): StateLiveData {
                instance = if (::instance.isInitialized) instance else StateLiveData()
                return instance
            }
        }
    }
}