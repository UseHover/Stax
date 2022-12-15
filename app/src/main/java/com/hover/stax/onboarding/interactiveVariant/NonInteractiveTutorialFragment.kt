/*
 * Copyright 2022 Stax
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hover.stax.onboarding.interactiveVariant

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.hover.stax.R
import com.hover.stax.databinding.FragmentNonInteractiveTutorialBinding
import com.hover.stax.onboarding.OnBoardingActivity
import com.hover.stax.utils.AnalyticsUtil

internal class NonInteractiveTutorialFragment : Fragment() {

    private var _binding: FragmentNonInteractiveTutorialBinding? = null
    private val binding get() = _binding!!

    private val args: NonInteractiveTutorialFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNonInteractiveTutorialBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        AnalyticsUtil.logAnalyticsEvent(getString(R.string.visit_screen, getString(R.string.visit_non_interactive_tutorial)), requireActivity())

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
        val questionType: Int = args.questionType
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
        (requireActivity() as OnBoardingActivity).checkPermissionsAndNavigate()
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