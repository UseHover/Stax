package com.hover.stax

import android.app.Application
import android.content.pm.PackageManager
import com.google.firebase.FirebaseApp
import com.hover.stax.di.appModule
import com.hover.stax.di.dataModule
import com.hover.stax.utils.Utils
import com.hover.stax.utils.fonts.FontReplacer
import com.yariksoffice.lingver.Lingver
import io.sentry.Sentry
import io.sentry.android.core.SentryAndroid
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
        FirebaseApp.initializeApp(this)

        initSentry()
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
            modules(listOf(appModule, dataModule))
        }
    }

    private fun initSentry() {
        SentryAndroid.init(this) {
            it.dsn = getString(R.string.sentry_dsn)
            it.release = BuildConfig.VERSION_NAME
            it.sessionTrackingIntervalMillis = 60000
        }

        addTags()
    }

    private fun addTags() {
        Sentry.configureScope {
            it.setTag("app_name", Utils.getPackage(this))

            try {
                val pInfo = packageManager.getPackageInfo(Utils.getPackage(this), 0)
                Sentry.setTag("app_version", pInfo.versionName)
                Sentry.setTag("app_version_code", pInfo.versionCode.toString())
            } catch (ignored: PackageManager.NameNotFoundException) {
            }
        }
    }

    private fun setLogger() {
        if (BuildConfig.DEBUG) Timber.plant(Timber.DebugTree()) else Timber.uprootAll()
    }
}