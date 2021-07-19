package com.hover.stax.home

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.hover.stax.R
import com.hover.stax.databinding.FragmentMainBinding
import com.hover.stax.inapp_banner.BannerViewModel
import com.hover.stax.utils.Constants
import com.hover.stax.utils.Utils
import org.koin.androidx.viewmodel.ext.android.viewModel

class HomeFragment : Fragment() {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    private val bannerViewModel: BannerViewModel by viewModel()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        Utils.logAnalyticsEvent(getString(R.string.visit_screen, getString(R.string.visit_home)), requireContext())
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupBanner()
        binding.airtime.setOnClickListener { navigateTo(Constants.NAV_AIRTIME, requireActivity()) }
        binding.transfer.setOnClickListener { navigateTo(Constants.NAV_TRANSFER, requireActivity()) }
    }

    private fun setupBanner() {
        with(bannerViewModel) {
            qualifiedBanner().observe(viewLifecycleOwner, { banner ->
                if (banner != null) {
                    Utils.logAnalyticsEvent(getString(R.string.displaying_in_app_banner, banner.id), requireContext())
                    binding.homeBanner.visibility = View.VISIBLE
                    binding.homeBanner.display(banner)

                    binding.homeBanner.setOnClickListener {
                        Utils.logAnalyticsEvent(getString(R.string.clicked_on_banner), requireContext())
                        Utils.openUrl(banner.url, requireActivity())
                        closeCampaign(banner.id)
                    }
                } else binding.homeBanner.visibility = View.GONE
            })
        }
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