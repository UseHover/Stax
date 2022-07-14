package com.hover.stax.presentation.simcard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.hover.stax.R
import com.hover.stax.databinding.FragmentSimBinding
import com.hover.stax.utils.AnalyticsUtil

class SimFragment : Fragment() {

	private var _binding: FragmentSimBinding? = null
	private val binding get() = _binding!!

	override fun onCreateView(inflater: LayoutInflater,
	                          container: ViewGroup?,
	                          savedInstanceState: Bundle?): View {
		AnalyticsUtil.logAnalyticsEvent(getString(R.string.visit_screen, getString(R.string.visit_sim)), requireContext())
		_binding = FragmentSimBinding.inflate(inflater, container, false)
		return binding.root
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}
}