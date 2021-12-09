package com.hover.stax.settings

import android.app.KeyguardManager
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.*
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.hover.sdk.actions.HoverAction
import com.hover.stax.R
import com.hover.stax.utils.AnalyticsUtil

class BiometricChecker(private val authListener: AuthListener, val activity: AppCompatActivity) : BiometricPrompt.AuthenticationCallback() {

    private var action: HoverAction? = null

    fun startAuthentication(a: HoverAction?) {
        action = a

        if (!(activity.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager).isKeyguardSecure) {
            authListener.onAuthSuccess(action)
            return
        }

        val biometricsManager = BiometricManager.from(activity)
        when (biometricsManager.canAuthenticate(BIOMETRIC_STRONG or BIOMETRIC_WEAK or DEVICE_CREDENTIAL)) {
            BiometricManager.BIOMETRIC_SUCCESS -> launchBiometricsPrompt()
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE,
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE,
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> authListener.onAuthSuccess(action)
            else -> authListener.onAuthSuccess(action)
        }
    }

    private fun launchBiometricsPrompt() {
        val executor = ContextCompat.getMainExecutor(activity)
        val biometricPrompt = BiometricPrompt(activity, executor, this)
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle(activity.getString(R.string.auth_title))
                .setAllowedAuthenticators(BIOMETRIC_STRONG or BIOMETRIC_WEAK or DEVICE_CREDENTIAL)
                .build()
        biometricPrompt.authenticate(promptInfo)
    }

    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
        super.onAuthenticationSucceeded(result)
        AnalyticsUtil.logAnalyticsEvent(activity.getString(R.string.biometrics_succeeded), activity.baseContext)
        authListener.onAuthSuccess(action)
    }

    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
        super.onAuthenticationError(errorCode, errString)
        if (errorCode == BiometricPrompt.ERROR_NO_BIOMETRICS)
            authListener.onAuthError(errString.toString())
        else
            AnalyticsUtil.logAnalyticsEvent(activity.getString(R.string.biometrics_not_setup), activity.baseContext)
    }

    override fun onAuthenticationFailed() {
        super.onAuthenticationFailed()
        AnalyticsUtil.logAnalyticsEvent(activity.getString(R.string.biometrics_failed), activity.baseContext)
    }

    interface AuthListener {
        fun onAuthError(error: String)
        fun onAuthSuccess(action: HoverAction?)
    }
}