package com.hover.stax.onboarding

import android.content.Context
import android.os.Bundle
import com.hover.stax.R
import com.hover.stax.databinding.OnboardingLayoutBinding
import com.hover.stax.login.StaxGoogleLoginInterface
import com.hover.stax.onboarding.navigation.AbstractOnboardingNavigationActivity
import com.hover.stax.utils.AnalyticsUtil
import com.hover.stax.utils.UIHelper
import com.hover.stax.utils.Utils

class OnBoardingActivity : AbstractOnboardingNavigationActivity(), StaxGoogleLoginInterface {

    private lateinit var binding: OnboardingLayoutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        UIHelper.setFullscreenView(this)
        super.onCreate(savedInstanceState)

        logEvents()
        binding = OnboardingLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigation()
        navigateNextScreen()
        setGoogleLoginInterface(this)

    }

    private fun logEvents() {
        AnalyticsUtil.logAnalyticsEvent(getString(R.string.visit_screen, getString(R.string.visit_onboarding)), this)
    }

    private fun navigateNextScreen() {
        if (hasPassedOnboarding(this)) checkPermissionThenNavigateMainActivity()
        else chooseOnboardingVariant()
    }

    private fun chooseOnboardingVariant() {
        navigateOnbardingVariantTwo()
    }

    override fun googleLoginSuccessful() {
        checkPermissionThenNavigateMainActivity()
    }

    override fun googleLoginFailed() {
        UIHelper.flashMessage(this, R.string.login_google_err)
    }

    companion object {
        fun hasPassedOnboarding(context: Context) = Utils.getBoolean(OnBoardingActivity::class.java.simpleName, context)
    }

    override fun googleLoginSuccessful() {
        checkPermissionThenNavigateMainActivity()
    }

    override fun googleLoginFailed() {
        UIHelper.flashMessage(this, R.string.login_google_err)
    }

    companion object {
        fun hasPassedOnboarding(context: Context) = Utils.getBoolean(OnBoardingActivity::class.java.simpleName, context)
    }

}