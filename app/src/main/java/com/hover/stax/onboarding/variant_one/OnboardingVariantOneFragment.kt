package com.hover.stax.onboarding.variant_one

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.hover.stax.R
import com.hover.stax.databinding.OnboardingVariantOneBinding
import com.hover.stax.onboarding.OnBoardingActivity
import timber.log.Timber


class OnboardingVariantOneFragment : Fragment(), ViewPager.OnPageChangeListener {

    private var _binding: OnboardingVariantOneBinding? = null
    private val binding get() = _binding!!

    private lateinit var progressBar1: LinearProgressIndicator
    private lateinit var progressBar2: LinearProgressIndicator
    private lateinit var progressBar3: LinearProgressIndicator
    private lateinit var progressBar4: LinearProgressIndicator

    private lateinit var animator1: ValueAnimator
    private lateinit var animator2: ValueAnimator
    private lateinit var animator3: ValueAnimator
    private lateinit var animator4: ValueAnimator

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        _binding = OnboardingVariantOneBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initProgressBarView()
        initAnimators()
        setUpSlides()
        setupPrivacyPolicy()
        setupTermsOfService()
        setupSignInWithGoogle()
    }

    private fun setupSignInWithGoogle() {
        binding.continueWithGoogle.setOnClickListener {
            (requireActivity() as OnBoardingActivity).signIn(optInMarketing = true)
        }
    }

    private fun initProgressBarView() {
        progressBar1 = binding.onoboardingV1Progressbar1
        progressBar2 = binding.onoboardingV1Progressbar2
        progressBar3 = binding.onoboardingV1Progressbar3
        progressBar4 = binding.onoboardingV1Progressbar4

        val brightBlue = requireContext().resources.getColor(R.color.brightBlue)
        progressBar1.trackColor = brightBlue
        progressBar2.trackColor = brightBlue
        progressBar3.trackColor = brightBlue
        progressBar4.trackColor = brightBlue

        val deepBlue = requireContext().resources.getColor(R.color.stax_state_blue)
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
            addOnPageChangeListener(this@OnboardingVariantOneFragment)
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

    private fun updateProgressAnimation(animator: ValueAnimator, progressBar: LinearProgressIndicator) {
        animator.duration = 4000
        animator.addUpdateListener { animation ->
            progressBar.progress = animation.animatedValue as Int
            if (progressBar.progress > 90) {
                fillUpProgress(progressBar)
            }
        }

        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                super.onAnimationEnd(animation)
            }
        })
        animator.start()
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
        showProgress(position)
        Timber.i("On Page scrolled: $position, offset: $positionOffset, offsetPixels: $positionOffsetPixels")
    }

    override fun onPageSelected(position: Int) {
        Timber.i("On Page selected: $position")
    }

    override fun onPageScrollStateChanged(state: Int) {
        Timber.i("On Page state changed: $state")
    }

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
        try{
            val deepBlue = requireContext().resources.getColor(R.color.stax_state_blue)
            progressBar.progress = 100
            progressBar.trackColor = deepBlue
        }
            catch (e: IllegalStateException) {Timber.i("animation needed to complete")
        }
    }

    private fun resetFilledProgress(animator: ValueAnimator, progressBar: LinearProgressIndicator) {
        animator.cancel()
        val brightBlue = requireContext().resources.getColor(R.color.brightBlue)
        progressBar.progress = 0
        progressBar.trackColor = brightBlue

    }

    companion object {
        const val FIRST_SCROLL_DELAY = 4000
        const val SCROLL_INTERVAL = 4000L
        const val SWIPE_DURATION_FACTOR = 2.0
        const val AUTO_SCROLL_EASE_DURATION_FACTOR = 5.0
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}