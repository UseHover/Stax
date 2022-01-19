package com.hover.stax.onboarding

import android.app.Activity
import android.os.Bundle
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

class SplashActivity: Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val splashScreen = installSplashScreen()
    }
}