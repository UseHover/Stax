package com.hover.stax.security;

import android.app.KeyguardManager;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;

import com.amplitude.api.Amplitude;
import com.hover.stax.R;
import com.hover.stax.actions.Action;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG;
import static androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK;
import static androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL;


public class BiometricChecker extends BiometricPrompt.AuthenticationCallback {
	private AppCompatActivity a;
	private AuthListener listener;
	private Action action;

	public BiometricChecker(AuthListener authListener, AppCompatActivity activity) {
		listener = authListener;
		a = activity;
	}

	public void startAuthentication(Action act) {
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
						.setSubtitle(a.getString(R.string.auth_subTitle))
						.setDescription(a.getString(R.string.auth_desc))
						.setAllowedAuthenticators(BIOMETRIC_STRONG | BIOMETRIC_WEAK | DEVICE_CREDENTIAL)
						.build();

		myBiometricPrompt.authenticate(promptInfo);
	}

	@Override
	public void onAuthenticationError(int errorCode, @NonNull CharSequence error) {
		super.onAuthenticationError(errorCode, error);
		if (errorCode == BiometricPrompt.ERROR_NO_BIOMETRICS) {
			listener.onAuthError(error.toString());
		} else Amplitude.getInstance().logEvent(a.getString(R.string.biometrics_not_setup));
	}

	@Override
	public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
		super.onAuthenticationSucceeded(result);
		Amplitude.getInstance().logEvent(a.getString(R.string.biometrics_succeeded));
		listener.onAuthSuccess(action);
	}

	@Override
	public void onAuthenticationFailed() {
		super.onAuthenticationFailed();
		Amplitude.getInstance().logEvent(a.getString(R.string.biometrics_failed));
	}

	public interface AuthListener {
		void onAuthError(String error);

		void onAuthSuccess(Action action);
	}
}
