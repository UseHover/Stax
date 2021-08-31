package com.hover.stax.onboarding

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.hover.sdk.permissions.PermissionHelper
import com.hover.stax.R
import com.hover.stax.databinding.OnboardingLayoutBinding
import com.hover.stax.home.MainActivity
import com.hover.stax.permissions.PermissionUtils
import com.hover.stax.utils.Constants
import com.hover.stax.utils.UIHelper
import com.hover.stax.utils.Utils

class OnBoardingActivity : AppCompatActivity() {

    private lateinit var binding: OnboardingLayoutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        UIHelper.setFullscreenView(this)
        super.onCreate(savedInstanceState)

        logEvents()
        binding = OnboardingLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initContinueButton()
    }

    private fun logEvents() {
        Utils.logAnalyticsEvent(getString(R.string.visit_screen, getString(R.string.visit_onboarding)), this)
    }

    private fun initContinueButton() = binding.onboardingContinueBtn.setOnClickListener {
        Utils.logAnalyticsEvent(getString(R.string.clicked_getstarted), this)
        setPassedThrough()
        checkPermissionsAndNavigate()
    }

    private fun checkPermissionsAndNavigate() {
        val permissionHelper = PermissionHelper(this)

        if (permissionHelper.hasBasicPerms()) {
            val intent = Intent(this, MainActivity::class.java).apply {
                putExtra(Constants.FRAGMENT_DIRECT, Constants.NAV_LINK_ACCOUNT)
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            startActivity(intent)
            finish()
        } else {
            PermissionUtils.showInformativeBasicPermissionDialog(0, {
                PermissionUtils.requestPerms(Constants.NAV_HOME, this@OnBoardingActivity)
            }, {
                Utils.logAnalyticsEvent(getString(R.string.perms_basic_cancelled), this@OnBoardingActivity)
            }, this)
        }
    }

    private fun setPassedThrough() = Utils.saveBoolean(OnBoardingActivity::class.java.simpleName, true, this)

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        PermissionUtils.logPermissionsGranted(grantResults, this)
        checkPermissionsAndNavigate()
    }

    companion object {
        fun hasPassedThrough(context: Context) = Utils.getBoolean(OnBoardingActivity::class.java.simpleName, context)
    }
}