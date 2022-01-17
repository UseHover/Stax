package com.hover.stax.onboarding.navigation

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.hover.sdk.permissions.PermissionHelper
import com.hover.stax.R
import com.hover.stax.home.MainActivity
import com.hover.stax.onboarding.OnBoardingActivity
import com.hover.stax.permissions.PermissionUtils
import com.hover.stax.utils.AnalyticsUtil
import com.hover.stax.utils.Constants
import com.hover.stax.utils.Utils

abstract class AbstractOnboardingNavigationActivity : AppCompatActivity(), OnboardingFragmentsNavigationInterface {

	private lateinit var navController: NavController
	override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
		super.onCreate(savedInstanceState, persistentState)
		setupNavigation()
	}

	private fun setupNavigation() {
		navController = Navigation.findNavController(this, R.id.nav_host_fragment_onboarding)
	}

	fun navigateMainActivity() = startActivity(Intent(this, MainActivity::class.java))
	fun navigateOnboardingVariantOne() = navController.navigate(R.id.navigation_onboarding_default)

	 private fun setPassedOnboarding() = Utils.saveBoolean(OnBoardingActivity::class.java.simpleName, true, this)

	 private fun checkPermissionsAndNavigate() {
		 val permissionHelper = PermissionHelper(this)
		 if (permissionHelper.hasBasicPerms()) navigateToMainActivity()
		 else showBasicPermission()
	 }
	 private fun navigateToMainActivity() {
		 val intent = Intent(this, MainActivity::class.java).apply {
			 putExtra(Constants.FRAGMENT_DIRECT, Constants.NAV_LINK_ACCOUNT)
			 flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
		 }
		 startActivity(intent)
		 finish()
	 }
	 private fun showBasicPermission(){
		 PermissionUtils.showInformativeBasicPermissionDialog(0,
			 { PermissionUtils.requestPerms(Constants.NAV_HOME, this@AbstractOnboardingNavigationActivity) }, {
				 AnalyticsUtil.logAnalyticsEvent(getString(R.string.perms_basic_cancelled), this@AbstractOnboardingNavigationActivity)
			 }, this)
	 }

	 override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
		 super.onRequestPermissionsResult(requestCode, permissions, grantResults)
		 PermissionUtils.logPermissionsGranted(grantResults, this)
		 checkPermissionsAndNavigate()
	 }

	 override fun continueWithoutSignIn() {
		 setPassedOnboarding()

	 }

	 override fun initiateSignIn() {
		 setPassedOnboarding()
	 }

	 override fun checkPermissionThenNavigateMainActivity() {
		 setPassedOnboarding()
		 checkPermissionsAndNavigate()
	 }

	fun hasPassedOnboarding(context: Context) = Utils.getBoolean(OnBoardingActivity::class.java.simpleName, context)

 }