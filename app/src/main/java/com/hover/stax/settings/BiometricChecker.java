package com.hover.stax.settings;

import android.app.KeyguardManager;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;

import com.hover.sdk.actions.HoverAction;
import com.hover.stax.R;
import com.hover.stax.utils.Utils;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG;
import static androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK;
import static androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL;


public class BiometricChecker extends BiometricPrompt.AuthenticationCallback {
    private AppCompatActivity a;
    private AuthListener listener;
    private HoverAction action;

    public BiometricChecker(AuthListener authListener, AppCompatActivity activity) {
        listener = authListener;
        a = activity;
    }

    public void startAuthentication(HoverAction act) {
        action = act;
        if (!((KeyguardManager) a.getSystemService(Context.KEYGUARD_SERVICE)).isKeyguardSecure()) {
            listener.onAuthSuccess(action);
            return;
        }

        Executor newExecutor = Executors.newSingleThreadExecutor();
        final BiometricPrompt myBiometricPrompt = new BiometricPrompt(a, newExecutor, this);

        final BiometricPrompt.PromptInfo promptInfo =
                new BiometricPrompt.PromptInfo.Builder()
                        .setTitle(a.getString(R.string.auth_title))
                        .setAllowedAuthenticators(BIOMETRIC_STRONG | BIOMETRIC_WEAK | DEVICE_CREDENTIAL)
                        .build();

        myBiometricPrompt.authenticate(promptInfo);
    }

    @Override
    public void onAuthenticationError(int errorCode, @NonNull CharSequence error) {
        super.onAuthenticationError(errorCode, error);
        if (errorCode == BiometricPrompt.ERROR_NO_BIOMETRICS) {
            listener.onAuthError(error.toString());
        } else Utils.logAnalyticsEvent(a.getString(R.string.biometrics_not_setup), a.getBaseContext());
    }

    @Override
    public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
        super.onAuthenticationSucceeded(result);
        Utils.logAnalyticsEvent(a.getString(R.string.biometrics_succeeded), a.getBaseContext());
        listener.onAuthSuccess(action);
    }

    @Override
    public void onAuthenticationFailed() {
        super.onAuthenticationFailed();
        Utils.logAnalyticsEvent(a.getString(R.string.biometrics_failed), a.getBaseContext());
    }

    public interface AuthListener {
        void onAuthError(String error);

        void onAuthSuccess(HoverAction action);
    }
}
