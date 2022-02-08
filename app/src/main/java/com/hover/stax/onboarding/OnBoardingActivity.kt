package com.hover.stax.onboarding

import android.content.Intent
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.hover.sdk.permissions.PermissionHelper
import com.hover.stax.R
import com.hover.stax.databinding.OnboardingLayoutBinding
import com.hover.stax.home.MainActivity
import com.hover.stax.login.AbstractGoogleAuthActivity
import com.hover.stax.login.StaxGoogleLoginInterface
import com.hover.stax.permissions.PermissionUtils
import com.hover.stax.utils.AnalyticsUtil
import com.hover.stax.utils.Constants
import com.hover.stax.utils.UIHelper
import com.hover.stax.utils.Utils
import timber.log.Timber

class OnBoardingActivity : AbstractGoogleAuthActivity(), StaxGoogleLoginInterface {

    private lateinit var binding: OnboardingLayoutBinding
    private lateinit var navController: NavController

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

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_onboarding) as NavHostFragment
        navController = navHostFragment.navController
    }

    private fun logEvents() = AnalyticsUtil.logAnalyticsEvent(getString(R.string.visit_screen, getString(R.string.visit_onboarding)), this)

    private fun navigateNextScreen() {
        if (hasPassedOnboarding()) checkPermissionsAndNavigate()
        else chooseOnboardingVariant()
    }

    private fun chooseOnboardingVariant() = when (Utils.getString(Constants.VARIANT, this) ?: "baseline") {
        "multichoice" -> navController.navigate(R.id.questionOnboardingFragment)
        "slides" ->  navController.navigate(R.id.questionOnboardingFragment)
        else -> {
            Timber.i("Loading default fragment")
            navController.navigate(R.id.questionOnboardingFragment) //do nothing, loading default fragment
        }
    }

    override fun googleLoginSuccessful() {
        navController.navigate(R.id.action_slidingOnboardingFragment_to_welcomeFragment)
    }

    override fun googleLoginFailed() {
        UIHelper.flashMessage(this, R.string.login_google_err)
    }

    fun checkPermissionsAndNavigate() {
        val permissionHelper = PermissionHelper(this)
        if (permissionHelper.hasBasicPerms()) navigateToMainActivity()
        else showBasicPermission()
    }

    private fun showBasicPermission() {
        PermissionUtils.showInformativeBasicPermissionDialog(0,
            { PermissionUtils.requestPerms(Constants.NAV_HOME, this) }, {
                AnalyticsUtil.logAnalyticsEvent(getString(R.string.perms_basic_cancelled), this)
            }, this
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        PermissionUtils.logPermissionsGranted(grantResults, this)
        checkPermissionsAndNavigate()
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra(Constants.FRAGMENT_DIRECT, Constants.NAV_LINK_ACCOUNT)
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        startActivity(intent)

        setPassedOnboarding()
        finish()
    }

    private fun setPassedOnboarding() = Utils.saveBoolean(OnBoardingActivity::class.java.simpleName, true, this)

    private fun hasPassedOnboarding(): Boolean = Utils.getBoolean(OnBoardingActivity::class.java.simpleName, this)
}