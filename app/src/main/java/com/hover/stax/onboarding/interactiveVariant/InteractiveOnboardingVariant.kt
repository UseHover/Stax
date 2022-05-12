package com.hover.stax.onboarding.interactiveVariant

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.hover.stax.R
import com.hover.stax.databinding.FragmentInteractiveOnboardingBinding
import com.hover.stax.onboarding.OnBoardingActivity
import com.hover.stax.utils.AnalyticsUtil
import com.hover.stax.utils.NavUtil
import com.uxcam.UXCam
import org.json.JSONObject
import timber.log.Timber

internal class InteractiveOnboardingVariant : Fragment() {

    private var _binding: FragmentInteractiveOnboardingBinding? = null
    private val binding get() = _binding!!

    private lateinit var onboardingActivity: OnBoardingActivity

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentInteractiveOnboardingBinding.inflate(inflater, container, false)
        onboardingActivity = (activity as OnBoardingActivity)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        AnalyticsUtil.logAnalyticsEvent(getString(R.string.visit_screen, getString(R.string.visit_interactive)), requireActivity())

        setQuestionsClick()
        setSkipOnboardingClick()

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, backPressedCallback)
    }

    private fun setQuestionsClick() {
        binding.onboardingVariant2Question1.setOnClickListener {
            logQuestionClicked(1)
            NavUtil.navigate(findNavController(), InteractiveOnboardingVariantDirections.actionInteractiveOnboardingFragmentToInteractiveTutorialFragment())
        }

        binding.onboardingVariant2Question2.setOnClickListener {
            logQuestionClicked(2)
            NavUtil.navigate(
                findNavController(),
                InteractiveOnboardingVariantDirections.actionInteractiveOnboardingFragmentToNonInteractiveTutorialFragment(NonInteractiveTutorialFragment.QUESTION_TWO)
            )
        }
        binding.onboardingVariant2Question3.setOnClickListener {
            logQuestionClicked(3)
            NavUtil.navigate(
                findNavController(),
                InteractiveOnboardingVariantDirections.actionInteractiveOnboardingFragmentToNonInteractiveTutorialFragment(NonInteractiveTutorialFragment.QUESTION_THREE)
            )
        }
    }

    private fun logQuestionClicked(q: Int) {
        val question = when (q) {
            1 -> getString(R.string.onboarding_variant_2_question1)
            2 -> getString(R.string.does_stax_charge_fees)
            3 -> getString(R.string.what_does_stax_do)
            else -> ""
        }

        val data = JSONObject()
        try {
            data.put("question", question)
        } catch (e: Exception) {
            Timber.e(e)
        }

        AnalyticsUtil.logAnalyticsEvent(getString(R.string.clicked_onboarding_question), data, requireActivity())
    }

    private fun setSkipOnboardingClick() = binding.skipBtn.setOnClickListener {
        AnalyticsUtil.logAnalyticsEvent(getString(R.string.clicked_skip_tutorial), requireActivity())
        onboardingActivity.checkPermissionsAndNavigate()
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
}