package com.hover.stax.home

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.hover.stax.R
import com.hover.stax.databinding.FragmentMainBinding
import com.hover.stax.utils.Constants
import com.hover.stax.utils.Utils

class HomeFragment : Fragment() {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        Utils.logAnalyticsEvent(getString(R.string.visit_screen, getString(R.string.visit_home)), requireContext())
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.airtime.setOnClickListener { navigateTo(Constants.NAV_AIRTIME, requireActivity()) }
        binding.transfer.setOnClickListener { navigateTo(Constants.NAV_TRANSFER, requireActivity()) }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }

    companion object {
        fun navigateTo(destination: Int, activity: Activity) {
            val a = activity as? MainActivity
            a?.checkPermissionsAndNavigate(destination)
        }
    }
}