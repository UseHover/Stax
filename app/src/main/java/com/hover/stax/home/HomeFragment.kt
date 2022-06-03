package com.hover.stax.home

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import com.hover.sdk.actions.HoverAction
import com.hover.stax.R
import com.hover.stax.accounts.Account
import com.hover.stax.accounts.AccountsViewModel
import com.hover.stax.addChannels.ChannelsViewModel
import com.hover.stax.bonus.Bonus
import com.hover.stax.bonus.BonusViewModel
import com.hover.stax.databinding.FragmentHomeBinding
import com.hover.stax.financialTips.FinancialTip
import com.hover.stax.financialTips.FinancialTipsViewModel
import com.hover.stax.utils.AnalyticsUtil
import com.hover.stax.utils.NavUtil
import com.hover.stax.utils.collectLatestLifecycleFlow
import com.hover.stax.utils.network.NetworkMonitor
import kotlinx.coroutines.flow.collect
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val wellnessViewModel: FinancialTipsViewModel by viewModel()
    private val accountsViewModel: AccountsViewModel by sharedViewModel()
    private val bonusViewModel: BonusViewModel by sharedViewModel()
    private val channelsViewModel: ChannelsViewModel by viewModel()

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
        binding.requestMoney.setOnClickListener{ navigateTo(HomeFragmentDirections.actionNavigationHomeToNavigationRequest())}

        NetworkMonitor.StateLiveData.get().observe(viewLifecycleOwner) {
            updateOfflineIndicator(it)
        }

        setUpWellnessTips()

        collectLatestLifecycleFlow(accountsViewModel.accounts) {
            setKeVisibility(it)
        }

        lifecycleScope.launchWhenStarted {
            channelsViewModel.accountEventFlow.collect {
                navigateTo(getTransferDirection(HoverAction.AIRTIME, bonusViewModel.bonuses.value.first().userChannel.toString()))
            }
        }
    }

    private fun getTransferDirection(type: String, channelId: String? = null): NavDirections {
        return HomeFragmentDirections.actionNavigationHomeToNavigationTransfer(type).also {
            if (channelId != null)
                it.channelId = channelId
        }
    }

    private fun setupBanner() {
        bonusViewModel.getBonusList()

        collectLatestLifecycleFlow(bonusViewModel.bonuses) { bonusList ->
            if (bonusList.isNotEmpty()) {
                with(binding.bonusCard) {
                    message.text = bonusList.first().message
                    learnMore.movementMethod = LinkMovementMethod.getInstance()
                }
                binding.bonusCard.apply {
                    cardBonus.visibility = View.VISIBLE
                    cta.setOnClickListener {
                        channelsViewModel // viewmodel must be instantiated in the main thread before it can be accessible on other threads
                        AnalyticsUtil.logAnalyticsEvent(getString(R.string.clicked_bonus_airtime_banner), requireActivity())
                        validateAccounts(bonusList.first())
                    }
                }
            } else binding.bonusCard.cardBonus.visibility = View.GONE
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

    private fun setUpWellnessTips() = collectLatestLifecycleFlow(wellnessViewModel.tips) {
        if (it.isNotEmpty())
            showTip(it.first())
        else
            binding.wellnessCard.tipsCard.visibility = View.GONE
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

    private fun validateAccounts(bonus: Bonus) {
        channelsViewModel.validateAccounts(bonus.userChannel)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}