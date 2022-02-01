package com.hover.stax.onboarding.questioningVariant

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.hover.stax.databinding.OnboardingVariantTwoBinding
import com.hover.stax.onboarding.OnBoardingActivity

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
    }

    private fun setQuestionsClick() {
        binding.onboardingVariant2Question1.setOnClickListener {
            onboardingActivity.navigateToVariantTwoWithCheckBox()
        }
        binding.onboardingVariant2Question2.setOnClickListener {
            onboardingActivity.navigateToVariantTwoNoCheckBox(AnswersNoCheckBoxFragment.QUESTION_TWO)
        }
        binding.onboardingVariant2Question3.setOnClickListener {
            onboardingActivity.navigateToVariantTwoNoCheckBox(AnswersNoCheckBoxFragment.QUESTION_THREE)
        }
    }

    private fun setSkipOnboardingClick() {
        binding.skipBtn.setOnClickListener {
            onboardingActivity.checkPermissionThenNavigateMainActivity()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}