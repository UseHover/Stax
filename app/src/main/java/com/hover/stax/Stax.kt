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
package com.hover.stax

import android.app.Application
import android.content.ComponentCallbacks
import com.appsflyer.AppsFlyerConversionListener
import com.appsflyer.AppsFlyerLib
import com.appsflyer.AppsFlyerProperties
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.hover.sdk.api.Hover
import com.hover.stax.remoteconfig.config.RemoteFeatureToggles
import com.hover.stax.sync.initializers.Sync
import com.hover.stax.core.network.NetworkMonitor
import com.jakewharton.processphoenix.ProcessPhoenix
import com.yariksoffice.lingver.Lingver
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import java.util.Locale
import javax.inject.Inject
import kotlin.properties.Delegates

@HiltAndroidApp
class Stax : Application() {

    @Inject
    lateinit var remoteFeatureToggles: RemoteFeatureToggles

    override fun onCreate() {
        super.onCreate()

        if (ProcessPhoenix.isPhoenixProcess(this)) {
            return // skip initialization for Phoenix process
        }

        setLocale()

        setLogger()
        initFirebase()

        initAppsFlyer()

        // Initialize Sync; the system responsible for keeping data in the app up to date.
        Sync.initialize(context = this)

        // Sync remote feature flags
        remoteFeatureToggles.sync()
    }

    private fun initFirebase() {
        FirebaseApp.initializeApp(this)
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG)
    }

    private fun setLocale() {
        Lingver.init(this, Locale.getDefault())
    }

    private fun setLogger() {
        if (BuildConfig.DEBUG) Timber.plant(Timber.DebugTree()) else Timber.uprootAll()
    }

    private fun initAppsFlyer() {
        val conversionListener = object : AppsFlyerConversionListener {
            override fun onConversionDataSuccess(data: MutableMap<String, Any>?) {
                data?.keys?.forEach {
                    Timber.d("Attribute $it = ${data[it]}")
                }
            }

            override fun onConversionDataFail(errorMessage: String?) =
                Timber.d("Error getting conversion data: $errorMessage")

            override fun onAppOpenAttribution(data: MutableMap<String, String>?) {
                data?.keys?.forEach {
                    Timber.d("Attribute $it = ${data[it]}")
                }
            }

            override fun onAttributionFailure(errorMessage: String?) =
                Timber.d("Error onAttributionFailure : $errorMessage")
        }

        AppsFlyerLib.getInstance().apply {
            init(getString(R.string.appsflyer_key), conversionListener, this@Stax)

            if (AppsFlyerProperties.getInstance()
                    .getString(AppsFlyerProperties.APP_USER_ID) == null
            )
                setCustomerUserId(Hover.getDeviceId(this@Stax))

            start(this@Stax)
        }
    }

    companion object {
        val txnDetailsRetryCounter: MutableMap<String, Int> by Delegates.observable(HashMap()) { _, _, _ -> }
    }

    override fun registerComponentCallbacks(callback: ComponentCallbacks?) {
        super.registerComponentCallbacks(callback)
        NetworkMonitor(this).startNetworkCallback()
    }

    override fun unregisterComponentCallbacks(callback: ComponentCallbacks?) {
        super.unregisterComponentCallbacks(callback)
        NetworkMonitor(this).stopNetworkCallback()
    }
}