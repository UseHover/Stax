package com.hover.stax

import android.app.Application
import android.content.ComponentCallbacks
import com.appsflyer.AppsFlyerConversionListener
import com.appsflyer.AppsFlyerLib
import com.appsflyer.AppsFlyerProperties
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.hover.sdk.api.Hover
import com.hover.stax.di.*
import com.hover.stax.utils.network.NetworkMonitor
import com.uxcam.UXCam
import com.yariksoffice.lingver.Lingver
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import timber.log.Timber
import java.util.*
import kotlin.properties.Delegates

class ApplicationInstance : Application() {

    override fun onCreate() {
        super.onCreate()

        setLocale()
        initDI()

        setLogger()
        initFirebase()

        initAppsFlyer()
        initUxCam()
    }

    private fun initFirebase() {
        FirebaseApp.initializeApp(this)
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG)
    }

    private fun setLocale() {
        Lingver.init(this, Locale.getDefault())
    }

    private fun initDI() {
        startKoin {
            androidContext(this@ApplicationInstance)
            modules(appModule + dataModule + networkModule + useCases + repositories)
        }
    }

    private fun initUxCam() {
        if (!BuildConfig.DEBUG) UXCam.startWithKey(getString(R.string.uxcam_key))
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

            override fun onConversionDataFail(errorMessage: String?) = Timber.d("Error getting conversion data: $errorMessage")

            override fun onAppOpenAttribution(data: MutableMap<String, String>?) {
                data?.keys?.forEach {
                    Timber.d("Attribute $it = ${data[it]}")
                }
            }

            override fun onAttributionFailure(errorMessage: String?) = Timber.d("Error onAttributionFailure : $errorMessage")
        }

        AppsFlyerLib.getInstance().apply {
            init(getString(R.string.appsflyer_key), conversionListener, this@ApplicationInstance)

            if (AppsFlyerProperties.getInstance().getString(AppsFlyerProperties.APP_USER_ID) == null)
                setCustomerUserId(Hover.getDeviceId(this@ApplicationInstance))

            start(this@ApplicationInstance)
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