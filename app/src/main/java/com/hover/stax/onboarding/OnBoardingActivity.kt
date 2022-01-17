package com.hover.stax.onboarding

import android.os.Bundle
import com.hover.stax.R
import com.hover.stax.databinding.OnboardingLayoutBinding
import com.hover.stax.onboarding.navigation.AbstractOnboardingNavigationActivity
import com.hover.stax.utils.AnalyticsUtil
import com.hover.stax.utils.UIHelper

class OnBoardingActivity : AbstractOnboardingNavigationActivity() {

    private lateinit var binding: OnboardingLayoutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        UIHelper.setFullscreenView(this)
        super.onCreate(savedInstanceState)

        logEvents()
        binding = OnboardingLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        navigateNextScreen()

    }

    private fun logEvents() {
        AnalyticsUtil.logAnalyticsEvent(getString(R.string.visit_screen, getString(R.string.visit_onboarding)), this)
    }

    private fun navigateNextScreen() {
        if(hasPassedOnboarding(this)) navigateMainActivity()
        else chooseOnboardingFragment()
    }

    private fun chooseOnboardingFragment() {
        navigateOnboardingVariantOne()
    }

}