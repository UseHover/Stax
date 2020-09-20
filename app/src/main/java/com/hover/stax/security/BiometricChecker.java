package com.hover.stax.security;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricConstants;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.amplitude.api.Amplitude;
import com.hover.stax.ApplicationInstance;
import com.hover.stax.R;


public class BiometricChecker extends BiometricPrompt.AuthenticationCallback {
	private AppCompatActivity a;
	private AuthListener listener;

	public BiometricChecker(AuthListener authListener, AppCompatActivity activity) {
		listener = authListener;
		a = activity;
	}

	public void startAuthentication() {
		BiometricManager biometricManager = BiometricManager.from(a);
		if (biometricManager.canAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS) {
			Executor newExecutor = Executors.newSingleThreadExecutor();
			final BiometricPrompt myBiometricPrompt = new BiometricPrompt(a, newExecutor, this);

			final BiometricPrompt.PromptInfo promptInfo =
					new BiometricPrompt.PromptInfo.Builder()
							.setTitle(ApplicationInstance.getContext().getResources().getString(R.string.auth_title))
							.setSubtitle(ApplicationInstance.getContext().getResources().getString(R.string.auth_subTitle))
							.setDescription(ApplicationInstance.getContext().getResources().getString(R.string.auth_desc))
							.setDeviceCredentialAllowed(true)
							.build();

			myBiometricPrompt.authenticate(promptInfo);
		} else onAuthenticationError(BiometricConstants.ERROR_NO_BIOMETRICS, "");
	}

	@Override
	public void onAuthenticationError(int errorCode, @NonNull CharSequence error) {
		super.onAuthenticationError(errorCode, error);
		Log.e("Bio", "Received auth error. code: " + errorCode);
		if (errorCode == BiometricConstants.ERROR_NO_BIOMETRICS) {
			Amplitude.getInstance().logEvent(a.getString(R.string.biometrics_not_matched));
			listener.onAuthError(error.toString());
		} else Amplitude.getInstance().logEvent(a.getString(R.string.biometrics_not_setup));
	}

	@Override
	public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
		super.onAuthenticationSucceeded(result);
		Amplitude.getInstance().logEvent(a.getString(R.string.biometrics_succeeded));
		listener.onAuthSuccess();
	}

	@Override
	public void onAuthenticationFailed() {
		super.onAuthenticationFailed();
		Amplitude.getInstance().logEvent(a.getString(R.string.biometrics_failed));
	}

	public interface AuthListener {
		void onAuthError(String error);
		void onAuthSuccess();
	}
}
