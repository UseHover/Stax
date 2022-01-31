package com.hover.stax.onboarding.questioningVariant

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.hover.stax.databinding.VariantTwoAnswerWithcheckboxBinding
import com.hover.stax.onboarding.OnBoardingActivity

internal class AnswersWithCheckBoxFragment : Fragment() {

    private var _binding: VariantTwoAnswerWithcheckboxBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = VariantTwoAnswerWithcheckboxBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setTopBarClicks()
        setContinueClick()
    }

    private fun setTopBarClicks() {
        binding.backButton.setOnClickListener { requireActivity().onBackPressed() }
        binding.skipTutorial.setOnClickListener {
            (activity as OnBoardingActivity).checkPermissionThenNavigateMainActivity()
        }
    }

    private fun setContinueClick() {
        binding.continueBtn.setOnClickListener {
            //To be replaced to navigate to welcome model: Kombo is working on this separately
            (activity as OnBoardingActivity).checkPermissionThenNavigateMainActivity()
        }
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