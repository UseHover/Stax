package com.hover.stax

import android.app.Application
import com.appsflyer.AppsFlyerConversionListener
import com.appsflyer.AppsFlyerLib
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.hover.stax.di.analyticsModule
import com.hover.stax.di.appModule
import com.hover.stax.di.dataModule
import com.hover.stax.utils.fonts.FontReplacer
import com.yariksoffice.lingver.Lingver
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import timber.log.Timber
import java.util.*


class ApplicationInstance : Application() {

    override fun onCreate() {
        super.onCreate()

        setFont()
        initDI()

        setLogger()
        initFirebase()

        initAppsFlyer()
    }

    private fun initFirebase(){
        FirebaseApp.initializeApp(this)
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG)
    }

    private fun setFont() {
        FontReplacer.Build(this).apply {
            setBoldFont("Effra_Heavy.ttf")
            setMediumFont("Effra_Medium.ttf")
            setDefaultFont("Effra_Regular.ttf")
            setThinFont("Effra_Regular.ttf")
        }.also { it.applyFont() }

        Lingver.init(this, Locale.getDefault())
    }

    private fun initDI() {
        startKoin {
            androidContext(this@ApplicationInstance)
            modules(listOf(appModule, dataModule, analyticsModule))
        }
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

        AppsFlyerLib.getInstance().init(getString(R.string.appsflyer_key), conversionListener, this)
    }

}