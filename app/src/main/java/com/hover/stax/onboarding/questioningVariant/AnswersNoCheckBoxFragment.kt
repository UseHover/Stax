package com.hover.stax.onboarding.questioningVariant

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.hover.stax.R
import com.hover.stax.databinding.VariantTwoWithoutCheckboxBinding
import com.hover.stax.onboarding.OnBoardingActivity
import com.hover.stax.onboarding.WelcomeFragment
import com.hover.stax.utils.Constants


internal class AnswersNoCheckBoxFragment : Fragment() {

    private var _binding: VariantTwoWithoutCheckboxBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = VariantTwoWithoutCheckboxBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setTopBarClicks()
        setContents()
        setContinueClick()
    }

    private fun setTopBarClicks() {
        binding.backButton.setOnClickListener { findNavController().popBackStack() }
        binding.skipTutorial.setOnClickListener {
            (activity as OnBoardingActivity).checkPermissionsAndNavigate()
        }
    }

    private fun setContents() {
        val questionType: Int = arguments?.getInt(Constants.QUESTION_TYPE, QUESTION_TWO)
            ?: QUESTION_TWO
        binding.variantV2NocheckboxTitle.setText(getTitleRes(questionType))
        binding.variantV2NocheckboxDesc.setText(getDescContent(questionType))
    }

    private fun getTitleRes(questionType: Int): Int {
        return if (questionType == QUESTION_TWO) R.string.does_stax_charge_fees
        else R.string.what_does_stax_do
    }

    private fun getDescContent(questionType: Int): Int {
        return if (questionType == QUESTION_TWO) R.string.variant_v2_nocheckbox_desc1
        else R.string.variant_v2_nocheckbox_desc2
    }

    private fun setContinueClick() = binding.continueBtn.setOnClickListener {
        findNavController().navigate(R.id.action_noCheckboxOnboardingFragment_to_welcomeFragment, bundleOf(WelcomeFragment.SALUTATIONS to 3))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val QUESTION_TWO: Int = 2
        const val QUESTION_THREE: Int = 3
    }
}