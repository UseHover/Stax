package com.hover.stax.onboarding.questioningVariant

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.hover.stax.R
import com.hover.stax.databinding.VariantTwoWithCheckboxBinding
import com.hover.stax.onboarding.OnBoardingActivity
import com.hover.stax.onboarding.welcome.WelcomeFragment

internal class AnswersWithCheckBoxFragment : Fragment() {

    private var _binding: VariantTwoWithCheckboxBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = VariantTwoWithCheckboxBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setTopBarClicks()
        setContinueClick()
    }

    private fun setTopBarClicks() {
        binding.backButton.setOnClickListener { findNavController().popBackStack() }
        binding.skipTutorial.setOnClickListener {
            (activity as OnBoardingActivity).checkPermissionsAndNavigate()
        }
    }

    private fun setContinueClick() = binding.continueBtn.setOnClickListener {
        val variation = if(isNoneApply()) 3 else 2
        findNavController().navigate(R.id.action_checkboxOnboardingFragment_to_welcomeFragment, bundleOf(WelcomeFragment.SALUTATIONS to variation))
    }

    private fun isNoneApply(): Boolean {
        return !binding.variant2Checkbox1.isChecked &&
                !binding.variant2Checkbox2.isChecked &&
                !binding.variant2Checkbox3.isChecked
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}