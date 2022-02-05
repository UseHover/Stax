package com.hover.stax.onboarding.signInVariant

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.viewpager.widget.ViewPager
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.hover.stax.R
import com.hover.stax.databinding.FragmentSigninVariantBinding
import com.hover.stax.onboarding.OnBoardingActivity
import com.hover.stax.onboarding.welcome.WelcomeFragment
import com.hover.stax.utils.AnalyticsUtil
import timber.log.Timber


class SignInVariantFragment : Fragment(), ViewPager.OnPageChangeListener {

    private var _binding: FragmentSigninVariantBinding? = null
    private val binding get() = _binding!!

    private lateinit var progressBar1: LinearProgressIndicator
    private lateinit var progressBar2: LinearProgressIndicator
    private lateinit var progressBar3: LinearProgressIndicator
    private lateinit var progressBar4: LinearProgressIndicator

    private lateinit var animator1: ValueAnimator
    private lateinit var animator2: ValueAnimator
    private lateinit var animator3: ValueAnimator
    private lateinit var animator4: ValueAnimator

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSigninVariantBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        AnalyticsUtil.logAnalyticsEvent(getString(R.string.visit_screen, getString(R.string.visit_sign_in)), requireActivity())

        initProgressBarView()
        initAnimators()

        setUpSlides()

        setupPrivacyPolicy()
        setupTermsOfService()

        setupSignInWithGoogle()
        setupContinueNoSignIn()

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, backPressedCallback)
    }

    private fun setupSignInWithGoogle() = binding.continueWithGoogle.setOnClickListener {
        AnalyticsUtil.logAnalyticsEvent(getString(R.string.clicked_google_sign_in), requireActivity())
        (requireActivity() as OnBoardingActivity).signIn(optInMarketing = true)
    }

    private fun setupContinueNoSignIn() = binding.continueNoSignIn.setOnClickListener {
        AnalyticsUtil.logAnalyticsEvent(getString(R.string.clicked_skip_sign_in), requireActivity())
        findNavController().navigate(R.id.action_slidingOnboardingFragment_to_welcomeFragment, bundleOf(WelcomeFragment.SALUTATIONS to 1))
    }

    private fun initProgressBarView() {
        progressBar1 = binding.pb1
        progressBar2 = binding.pb2
        progressBar3 = binding.pb3
        progressBar4 = binding.pb4

        val brightBlue = ContextCompat.getColor(requireActivity(), R.color.brightBlue)
        progressBar1.trackColor = brightBlue
        progressBar2.trackColor = brightBlue
        progressBar3.trackColor = brightBlue
        progressBar4.trackColor = brightBlue

        val deepBlue = ContextCompat.getColor(requireActivity(), R.color.stax_state_blue)
        progressBar1.setIndicatorColor(deepBlue)
        progressBar2.setIndicatorColor(deepBlue)
        progressBar3.setIndicatorColor(deepBlue)
        progressBar4.setIndicatorColor(deepBlue)
    }

    private fun initAnimators() {
        animator1 = ValueAnimator.ofInt(0, progressBar1.max)
        animator2 = ValueAnimator.ofInt(0, progressBar2.max)
        animator3 = ValueAnimator.ofInt(0, progressBar3.max)
        animator4 = ValueAnimator.ofInt(0, progressBar4.max)
    }

    private fun setUpSlides() {
        val viewPagerAdapter = SlidesPagerAdapter(requireContext())
        val viewPager = binding.vpPager
        viewPager.apply {
            startAutoScroll(FIRST_SCROLL_DELAY)
            setInterval(SCROLL_INTERVAL)
            setCycle(true)
            setAutoScrollDurationFactor(AUTO_SCROLL_EASE_DURATION_FACTOR)
            setSwipeScrollDurationFactor(SWIPE_DURATION_FACTOR)
            setStopScrollWhenTouch(true)
            addOnPageChangeListener(this@SignInVariantFragment)
            adapter = viewPagerAdapter
        }
    }

    private fun setupPrivacyPolicy() {
        binding.onboardingV1Tos.text = Html.fromHtml(requireContext().getString(R.string.privacyPolicyFullLabel))
        binding.onboardingV1Tos.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun setupTermsOfService() {
        binding.onboardingV1PrivacyPolicy.text = Html.fromHtml(requireContext().getString(R.string.termsOfServiceFullLabel))
        binding.onboardingV1PrivacyPolicy.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun updateProgressAnimation(animator: ValueAnimator, progressBar: LinearProgressIndicator) = animator.apply {
        duration = 400
        addUpdateListener { animation ->
            progressBar.progress = animation.animatedValue as Int
            if (progressBar.progress > 90) {
                fillUpProgress(progressBar)
            }
        }
        addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                super.onAnimationEnd(animation)
            }
        })
        start()
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
        showProgress(position)
    }

    override fun onPageSelected(position: Int) {}

    override fun onPageScrollStateChanged(state: Int) {}

    private fun showProgress(currentPos: Int) {
        when (currentPos) {
            0 -> {
                updateProgressAnimation(animator1, progressBar1)
                resetFilledProgress(animator2, progressBar2)
                resetFilledProgress(animator3, progressBar3)
                resetFilledProgress(animator4, progressBar4)
            }

            1 -> {
                fillUpProgress(progressBar1)
                updateProgressAnimation(animator2, progressBar2)
                resetFilledProgress(animator3, progressBar3)
                resetFilledProgress(animator4, progressBar4)
            }

            2 -> {
                fillUpProgress(progressBar1)
                fillUpProgress(progressBar2)
                updateProgressAnimation(animator3, progressBar3)
                resetFilledProgress(animator4, progressBar4)
            }

            3 -> {
                fillUpProgress(progressBar1)
                fillUpProgress(progressBar2)
                fillUpProgress(progressBar3)
                updateProgressAnimation(animator4, progressBar4)
            }
        }
    }

    private fun fillUpProgress(progressBar: LinearProgressIndicator) {
        try {
            val deepBlue = ContextCompat.getColor(requireActivity(), R.color.stax_state_blue)
            progressBar.progress = 100
            progressBar.trackColor = deepBlue
        } catch (e: IllegalStateException) {
            Timber.i("animation needed to complete")
        }
    }

    private fun resetFilledProgress(animator: ValueAnimator, progressBar: LinearProgressIndicator) {
        animator.cancel()
        val brightBlue = ContextCompat.getColor(requireActivity(), R.color.brightBlue)
        progressBar.progress = 0
        progressBar.trackColor = brightBlue
    }

    private val backPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            Timber.i("Back navigation disabled") //do nothing to prevent navigation back to the home fragment (default variant)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val FIRST_SCROLL_DELAY = 4000
        const val SCROLL_INTERVAL = 4000L
        const val SWIPE_DURATION_FACTOR = 2.0
        const val AUTO_SCROLL_EASE_DURATION_FACTOR = 5.0
    }
}