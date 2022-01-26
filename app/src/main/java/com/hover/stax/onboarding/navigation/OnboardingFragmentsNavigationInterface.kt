package com.hover.stax.onboarding.navigation

internal interface OnboardingFragmentsNavigationInterface {
    fun continueWithoutSignIn()
    fun initiateSignIn()
    fun checkPermissionThenNavigateMainActivity()
}