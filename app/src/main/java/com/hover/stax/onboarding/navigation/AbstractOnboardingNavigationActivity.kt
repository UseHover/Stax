package com.hover.stax.onboarding.navigation

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import com.hover.sdk.permissions.PermissionHelper
import com.hover.stax.R
import com.hover.stax.home.AbstractNavigationActivity
import com.hover.stax.home.MainActivity
import com.hover.stax.login.AbstractGoogleAuthActivity
import com.hover.stax.onboarding.OnBoardingActivity
import com.hover.stax.permissions.PermissionUtils
import com.hover.stax.utils.AnalyticsUtil
import com.hover.stax.utils.Constants
import com.hover.stax.utils.Utils

abstract class AbstractOnboardingNavigationActivity : AbstractGoogleAuthActivity(), OnboardingFragmentsNavigationInterface {

	private lateinit var onboardingNavController: NavController

	fun setupNavigation() {
		val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_onboarding) as NavHostFragment
		onboardingNavController = navHostFragment.navController
	}

	fun navigateMainActivity() = startActivity(Intent(this, MainActivity::class.java))
	fun navigateOnboardingVariantOne() = onboardingNavController.navigate(R.id.navigation_onboarding_v1)

	private fun navigateToMainActivity(activity: Activity) {
		val intent = Intent(activity, MainActivity::class.java)
		intent.putExtra(Constants.FRAGMENT_DIRECT, Constants.NAV_LINK_ACCOUNT)
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
		activity.startActivity(intent)
		activity.finish()
	}
	 private fun setPassedOnboarding() = Utils.saveBoolean(OnBoardingActivity::class.java.simpleName, true, this)

	 override fun continueWithoutSignIn() {
		 setPassedOnboarding()
	 }

	 override fun initiateSignIn() {
		 setPassedOnboarding()
	 }

	 override fun checkPermissionThenNavigateMainActivity() {
		 setPassedOnboarding()

	 }

 }