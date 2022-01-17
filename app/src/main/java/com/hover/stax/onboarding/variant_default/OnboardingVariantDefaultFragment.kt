package com.hover.stax.onboarding.variant_default

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.hover.stax.R
import com.hover.stax.databinding.OnboardingLayoutDefaultBinding
import com.hover.stax.onboarding.navigation.OnboardingFragmentsNavigationInterface
import com.hover.stax.utils.AnalyticsUtil

class OnboardingVariantDefaultFragment: Fragment() {

	private var _binding: OnboardingLayoutDefaultBinding? = null
	private val binding get() = _binding!!

	private lateinit var fragmentsNavigationInterface: OnboardingFragmentsNavigationInterface
	override fun onCreateView(inflater: LayoutInflater,
	                          container: ViewGroup?,
	                          savedInstanceState: Bundle?): View {
		_binding = OnboardingLayoutDefaultBinding.inflate(inflater, container, false)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		initNavigation()
		initContinueButton()
	}


	private fun initNavigation() {
		fragmentsNavigationInterface = activity as OnboardingFragmentsNavigationInterface
	}
	private fun initContinueButton() = binding.onboardingContinueBtn.setOnClickListener {
		AnalyticsUtil.logAnalyticsEvent(getString(R.string.clicked_getstarted), requireContext())
		fragmentsNavigationInterface.checkPermissionThenNavigateMainActivity()
	}
	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}
}