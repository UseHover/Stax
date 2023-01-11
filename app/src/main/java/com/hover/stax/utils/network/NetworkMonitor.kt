/*
 * Copyright 2022 Stax
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hover.stax.utils.network

import android.Manifest
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import androidx.annotation.MainThread
import androidx.annotation.RequiresPermission
import androidx.lifecycle.MutableLiveData
import kotlin.properties.Delegates
import timber.log.Timber

class NetworkMonitor
@RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
constructor(val context: Context) {

    private val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    fun startNetworkCallback() {
        val builder: NetworkRequest.Builder = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)

        if (Build.VERSION.SDK_INT < 24)
            cm.registerNetworkCallback(builder.build(), connectivityManagerCallback)
        else
            cm.registerDefaultNetworkCallback(connectivityManagerCallback)
    }

    fun stopNetworkCallback() = try {
        cm.unregisterNetworkCallback(connectivityManagerCallback)
    } catch (ignored: Exception) {
        Timber.d("Network callback already unregistered.")
    }

    private val connectivityManagerCallback = object : ConnectivityManager.NetworkCallback() {

        override fun onAvailable(network: Network) {
            isNetworkConnected = true
        }

        override fun onLost(network: Network) {
            isNetworkConnected = false
        }
    }

    var isNetworkConnected: Boolean by Delegates.observable(true) { _, _, newValue ->
        StateLiveData.get().postValue(newValue)
    }

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