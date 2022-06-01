package com.hover.stax.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import com.hover.sdk.actions.HoverAction
import com.hover.stax.R
import com.hover.stax.accounts.Account
import com.hover.stax.channels.Channel
import com.hover.stax.accounts.AccountsViewModel
import com.hover.stax.databinding.FragmentHomeBinding
import com.hover.stax.financialTips.FinancialTip
import com.hover.stax.financialTips.FinancialTipsViewModel
import com.hover.stax.inapp_banner.BannerViewModel
import com.hover.stax.utils.AnalyticsUtil
import com.hover.stax.utils.NavUtil
import com.hover.stax.utils.Utils
import com.hover.stax.utils.network.NetworkMonitor
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val bannerViewModel: BannerViewModel by viewModel()
    private val wellnessViewModel: FinancialTipsViewModel by viewModel()
    private val accountsViewModel: AccountsViewModel by sharedViewModel()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        AnalyticsUtil.logAnalyticsEvent(getString(R.string.visit_screen, getString(R.string.visit_home)), requireContext())
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupBanner()

        binding.airtime.setOnClickListener { navigateTo(getTransferDirection(HoverAction.AIRTIME)) }
        binding.transfer.setOnClickListener { navigateTo(getTransferDirection(HoverAction.P2P)) }
        binding.merchant.setOnClickListener { navigateTo(HomeFragmentDirections.actionNavigationHomeToMerchantFragment()) }
        binding.paybill.setOnClickListener { navigateTo(HomeFragmentDirections.actionNavigationHomeToPaybillFragment()) }

        NetworkMonitor.StateLiveData.get().observe(viewLifecycleOwner) {
            updateOfflineIndicator(it)
        }

        setUpWellnessTips()
        accountsViewModel.accounts.observe(viewLifecycleOwner, this::setKeVisibility)
    }

    private fun getTransferDirection(type: String) : NavDirections {
        return HomeFragmentDirections.actionNavigationHomeToNavigationTransfer(type)
    }
    private fun setupBanner() {
        with(bannerViewModel) {
            qualifiedBanner.observe(viewLifecycleOwner) { banner ->
                if (banner != null) {
                    AnalyticsUtil.logAnalyticsEvent(getString(R.string.displaying_in_app_banner, banner.id), requireContext())
                    binding.homeBanner.visibility = View.VISIBLE
                    binding.homeBanner.display(banner)

                    binding.homeBanner.setOnClickListener {
                        AnalyticsUtil.logAnalyticsEvent(getString(R.string.clicked_on_banner), requireContext())
                        Utils.openUrl(banner.url, requireActivity())
                        closeCampaign(banner.id)
                    }
                } else binding.homeBanner.visibility = View.GONE
            }
        }
    }

    private fun setKeVisibility(accounts: List<Account>?) {
        binding.merchant.visibility = if (showMpesaActions(accounts)) View.VISIBLE else View.GONE
        binding.paybill.visibility = if (showMpesaActions(accounts)) View.VISIBLE else View.GONE
    }

    private fun showMpesaActions(accounts: List<Account>?): Boolean = accounts?.any { it.countryAlpha2.contentEquals("KE", ignoreCase = true) } == true

    private fun navigateTo(navDirections: NavDirections) = (requireActivity() as MainActivity).checkPermissionsAndNavigate(navDirections)

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

    private fun showTip(tip: FinancialTip) {
        tip.date?.let {
            if (android.text.format.DateUtils.isToday(it.time)) {
                with(binding.wellnessCard) {
                    tipsCard.visibility = View.VISIBLE

                    title.text = tip.title
                    snippet.text = tip.snippet

                    contentLayout.setOnClickListener {
                        NavUtil.navigate(findNavController(), HomeFragmentDirections.actionNavigationHomeToWellnessFragment(tip.id))
                    }

                    readMoreLayout.setOnClickListener {
                        NavUtil.navigate(findNavController(), HomeFragmentDirections.actionNavigationHomeToWellnessFragment(null))
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
}