package com.hover.stax.onboarding

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.hover.sdk.permissions.PermissionHelper
import com.hover.stax.R
import com.hover.stax.databinding.OnboardingLayoutBinding
import com.hover.stax.home.MainActivity
import com.hover.stax.permissions.PermissionUtils
import com.hover.stax.utils.Constants
import com.hover.stax.utils.UIHelper
import com.hover.stax.utils.Utils

class OnBoardingActivity : AppCompatActivity(), ViewPager.OnPageChangeListener {

    private var viewPager: StaxAutoScrollViewPager? = null

    private lateinit var binding: OnboardingLayoutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        UIHelper.setFullscreenView(this)
        super.onCreate(savedInstanceState)

        logEvents()
        binding = OnboardingLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setUpSlides()
        initContinueButton()
    }

    private fun logEvents() {
        Utils.logAnalyticsEvent(getString(R.string.visit_screen, getString(R.string.visit_onboarding)), this)
        Utils.timeEvent(getString(R.string.perms_basic_requested))
    }

    private fun setUpSlides() {
        val viewPagerAdapter = SlidesPagerAdapter(supportFragmentManager, FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT)

        viewPager = binding.vpPager
        viewPager!!.apply {
            startAutoScroll(FIRST_SCROLL_DELAY)
            setInterval(OTHER_SCROLL_DELAY)
            setCycle(true)
            setAutoScrollDurationFactor(AUTO_SCROLL_EASE_DURATION_FACTOR)
            setSwipeScrollDurationFactor(SWIPE_DURATION_FACTOR)
            setStopScrollWhenTouch(true)
            addOnPageChangeListener(this@OnBoardingActivity)
            adapter = viewPagerAdapter
        }
    }

    private fun initContinueButton() = binding.onboardingContinueBtn.setOnClickListener {
        viewPager?.stopAutoScroll()
        Utils.logAnalyticsEvent(getString(R.string.clicked_getstarted), this)
        setPassedThrough()
        checkPermissionsAndNavigate()
    }

    private fun checkPermissionsAndNavigate() {
        val permissionHelper = PermissionHelper(this)

        //if remote configs haven't been pulled yet, default to the baseline version
        if (Utils.variant.isEmpty()) Utils.variant = Constants.VARIANT_1

        if (Utils.variant == Constants.VARIANT_1 || permissionHelper.hasBasicPerms()) {
            startActivity(Intent(this, MainActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
            finish()
        } else
            PermissionUtils.showInformativeBasicPermissionDialog({
                PermissionUtils.requestPerms(Constants.NAV_HOME, this@OnBoardingActivity)
            }, {
                Utils.logAnalyticsEvent(getString(R.string.perms_basic_cancelled), this@OnBoardingActivity)
            }, this)
    }

    private fun setPassedThrough() = Utils.saveBoolean(OnBoardingActivity::class.java.simpleName, true, this)

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

    override fun onPageSelected(position: Int) = Utils.logAnalyticsEvent(getString(R.string.viewing_onboarding_slide, position.toString()), this);

    override fun onPageScrollStateChanged(state: Int) {}

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        PermissionUtils.logPermissionsGranted(grantResults, this)
        checkPermissionsAndNavigate()
    }

    companion object {
        const val FIRST_SCROLL_DELAY = 4000
        const val OTHER_SCROLL_DELAY = 5000L
        const val SWIPE_DURATION_FACTOR = 2.0
        const val AUTO_SCROLL_EASE_DURATION_FACTOR = 5.0

        fun hasPassedThrough(context: Context) = Utils.getBoolean(OnBoardingActivity::class.java.simpleName, context)
    }
}