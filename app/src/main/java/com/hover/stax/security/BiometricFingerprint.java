package com.hover.stax.security;

import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricConstants;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.fragment.app.FragmentActivity;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.hover.stax.ApplicationInstance;
import com.hover.stax.R;


public class BiometricFingerprint {
	//Accept AppCompact Activity
	public void startFingerPrint(AppCompatActivity activity, BiometricPrompt.AuthenticationCallback authenticationCallback) {
		BiometricManager biometricManager = BiometricManager.from(ApplicationInstance.getContext());
		if(biometricManager.canAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS) {
			Executor newExecutor = Executors.newSingleThreadExecutor();
			final BiometricPrompt myBiometricPrompt = new BiometricPrompt(activity, newExecutor, authenticationCallback);

			final BiometricPrompt.PromptInfo promptInfo =
					new BiometricPrompt.PromptInfo.Builder()
							.setTitle(ApplicationInstance.getContext().getResources().getString(R.string.auth_title))
							.setSubtitle(ApplicationInstance.getContext().getResources().getString(R.string.auth_subTitle))
							.setDescription(ApplicationInstance.getContext().getResources().getString(R.string.auth_desc))
							.setNegativeButtonText(ApplicationInstance.getContext().getResources().getString(R.string.auth_cancel))
							.build();

			myBiometricPrompt.authenticate(promptInfo);
		} else authenticationCallback.onAuthenticationError(BiometricConstants.ERROR_NO_BIOMETRICS, "");
	}

	//Accept FragmentActivity
	public void startFingerPrint(FragmentActivity activity, BiometricPrompt.AuthenticationCallback authenticationCallback) {
		BiometricManager biometricManager = BiometricManager.from(ApplicationInstance.getContext());
		if(biometricManager.canAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS) {
			Executor newExecutor = Executors.newSingleThreadExecutor();
			final BiometricPrompt myBiometricPrompt = new BiometricPrompt(activity, newExecutor, authenticationCallback);

			final BiometricPrompt.PromptInfo promptInfo =
					new BiometricPrompt.PromptInfo.Builder()
							.setTitle(ApplicationInstance.getContext().getResources().getString(R.string.auth_title))
							.setSubtitle(ApplicationInstance.getContext().getResources().getString(R.string.auth_subTitle))
							.setDescription(ApplicationInstance.getContext().getResources().getString(R.string.auth_desc))
							.setNegativeButtonText(ApplicationInstance.getContext().getResources().getString(R.string.auth_cancel))
							.build();

			myBiometricPrompt.authenticate(promptInfo);
		} else authenticationCallback.onAuthenticationError(BiometricConstants.ERROR_NO_BIOMETRICS, "");
	}
}
