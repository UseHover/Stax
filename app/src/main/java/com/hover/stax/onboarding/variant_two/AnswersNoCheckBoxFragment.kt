package com.hover.stax.onboarding.variant_two

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.hover.stax.R
import com.hover.stax.databinding.VariantTwoAnswerNocheckboxBinding
import com.hover.stax.onboarding.OnBoardingActivity
import com.hover.stax.utils.Constants


internal class AnswersNoCheckBoxFragment : Fragment() {

    private var _binding: VariantTwoAnswerNocheckboxBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = VariantTwoAnswerNocheckboxBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setTopBarClicks()
        setContents()
        setContinueClick()
    }

    private fun setTopBarClicks() {
        binding.backButton.setOnClickListener { requireActivity().onBackPressed() }
        binding.skipTutorial.setOnClickListener {
            (activity as OnBoardingActivity).checkPermissionThenNavigateMainActivity()
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

    private fun setContinueClick() {
        binding.continueBtn.setOnClickListener {
            (activity as OnBoardingActivity).checkPermissionThenNavigateMainActivity()
        }
    }

    companion object {
        val QUESTION_TWO: Int = 2
        val QUESTION_THREE: Int = 3
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}