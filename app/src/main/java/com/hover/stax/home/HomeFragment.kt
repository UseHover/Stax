package com.hover.stax.home

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.hover.stax.R
import com.hover.stax.databinding.FragmentHomeBinding
import com.hover.stax.inapp_banner.BannerViewModel
import com.hover.stax.utils.Constants
import com.hover.stax.utils.Utils
import com.hover.stax.utils.network.NetworkMonitor
import com.hover.stax.wellness.WellnessTip
import com.hover.stax.wellness.WellnessViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val bannerViewModel: BannerViewModel by viewModel()
    private val wellnessViewModel: WellnessViewModel by viewModel()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        Utils.logAnalyticsEvent(getString(R.string.visit_screen, getString(R.string.visit_home)), requireContext())
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupBanner()

        binding.airtime.setOnClickListener { navigateTo(Constants.NAV_AIRTIME, requireActivity()) }
        binding.transfer.setOnClickListener { navigateTo(Constants.NAV_TRANSFER, requireActivity()) }

        NetworkMonitor.StateLiveData.get().observe(viewLifecycleOwner) {
            updateOfflineIndicator(it)
        }

        setUpWellnessTips()
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

    private fun updateOfflineIndicator(isConnected: Boolean) {
        binding.offlineBadge.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_internet_off, 0, 0, 0)
        binding.offlineBadge.visibility = if (isConnected) View.GONE else View.VISIBLE
    }

    private fun setUpWellnessTips() {
        wellnessViewModel.tips.observe(viewLifecycleOwner) {
            if (it.isNotEmpty())
                showTip(it.first())
            else
                binding.wellnessCard.tipsCard.visibility = View.GONE
        }
    }

    private fun showTip(tip: WellnessTip) {
        tip.date?.let {
            if (android.text.format.DateUtils.isToday(it.time)) {
                with(binding.wellnessCard) {
                    tipsCard.visibility = View.VISIBLE

                    title.text = tip.title
                    snippet.text = tip.snippet ?: tip.content

                    tipsCard.setOnClickListener {
                        findNavController().navigate(R.id.action_navigation_home_to_wellnessFragment)
                    }
                }
            } else
                Timber.i("No tips available today")
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