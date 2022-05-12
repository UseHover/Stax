package com.hover.stax.onboarding.defaultVariant

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.hover.stax.R
import com.hover.stax.databinding.FragmentDefaultVariantBinding
import com.hover.stax.onboarding.OnBoardingActivity
import com.hover.stax.utils.AnalyticsUtil
import com.uxcam.UXCam

class DefaultVariantFragment : Fragment() {

    private var _binding: FragmentDefaultVariantBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDefaultVariantBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        AnalyticsUtil.logAnalyticsEvent(getString(R.string.visit_screen, getString(R.string.visit_onboarding)), requireActivity())
        initContinueButton()
    }

    private fun initContinueButton() = binding.onboardingContinueBtn.setOnClickListener {
        AnalyticsUtil.logAnalyticsEvent(getString(R.string.clicked_getstarted), requireContext())
        (requireActivity() as OnBoardingActivity).checkPermissionsAndNavigate()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}