package com.hover.stax.onboarding.questioningVariant

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.hover.stax.R
import com.hover.stax.databinding.OnboardingVariantTwoBinding
import com.hover.stax.onboarding.OnBoardingActivity
import com.hover.stax.utils.Constants
import timber.log.Timber

internal class QuestioningVariantFragment : Fragment() {

    private var _binding: OnboardingVariantTwoBinding? = null
    private val binding get() = _binding!!

    private lateinit var onboardingActivity: OnBoardingActivity

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = OnboardingVariantTwoBinding.inflate(inflater, container, false)
        onboardingActivity = (activity as OnBoardingActivity)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setQuestionsClick()
        setSkipOnboardingClick()

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, backPressedCallback)
    }

    private fun setQuestionsClick() {
        binding.onboardingVariant2Question1.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_onboarding_v2_to_navigation_onboarding_v2_withCheckbox)
        }
        binding.onboardingVariant2Question2.setOnClickListener {
            findNavController().navigate(
                R.id.action_navigation_onboarding_v2_to_navigation_onboarding_v2_noCheckbox,
                bundleOf(Constants.QUESTION_TYPE to AnswersNoCheckBoxFragment.QUESTION_TWO)
            )
        }
        binding.onboardingVariant2Question3.setOnClickListener {
            findNavController().navigate(
                R.id.action_navigation_onboarding_v2_to_navigation_onboarding_v2_noCheckbox,
                bundleOf(Constants.QUESTION_TYPE to AnswersNoCheckBoxFragment.QUESTION_THREE)
            )
        }
    }

    private fun setSkipOnboardingClick() = binding.skipBtn.setOnClickListener {
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