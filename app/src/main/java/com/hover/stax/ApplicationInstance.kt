package com.hover.stax

import android.app.Application
import com.google.firebase.FirebaseApp
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
        FirebaseApp.initializeApp(this)
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

    private fun initDI(){
        startKoin {
            androidContext(this@ApplicationInstance)
            modules(listOf(appModule, dataModule))
        }
    }

    private fun setLogger() {
        if (BuildConfig.DEBUG) Timber.plant(Timber.DebugTree()) else Timber.uprootAll()
    }
}