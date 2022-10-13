package com.hover.stax.presentation.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import com.hover.sdk.actions.HoverAction
import com.hover.stax.MainNavigationDirections
import com.hover.stax.R
import com.hover.stax.addChannels.ChannelsViewModel
import com.hover.stax.databinding.FragmentHomeBinding
import com.hover.stax.domain.model.Account
import com.hover.stax.home.MainActivity
import com.hover.stax.hover.AbstractHoverCallerActivity
import com.hover.stax.utils.*
import com.hover.stax.views.StaxDialog
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel


class HomeFragment : Fragment(), FinancialTipClickInterface, BalanceTapListener {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val channelsViewModel: ChannelsViewModel by sharedViewModel()
    private val balancesViewModel: BalancesViewModel by sharedViewModel()
    private val homeViewModel: HomeViewModel by viewModel()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        AnalyticsUtil.logAnalyticsEvent(getString(R.string.visit_screen, getString(R.string.visit_home)), requireContext())
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setComposeView()

        observeForBalances()
        observeForBonus()
    }

    private fun getHomeClickFunctions(): HomeClickFunctions {
        fun onSendMoneyClicked() = navigateTo(getTransferDirection(HoverAction.P2P))
        fun onBuyAirtimeClicked() = navigateTo(getTransferDirection(HoverAction.AIRTIME))
        fun onBuyGoodsClicked() = navigateTo(HomeFragmentDirections.actionNavigationHomeToMerchantFragment())
        fun onPayBillClicked() = navigateTo(HomeFragmentDirections.actionNavigationHomeToPaybillFragment())
        fun onRequestMoneyClicked() = navigateTo(HomeFragmentDirections.actionNavigationHomeToNavigationRequest())
        fun onClickedAddNewAccount() = (requireActivity() as MainActivity).checkPermissionsAndNavigate(MainNavigationDirections.actionGlobalAddChannelsFragment())
        fun onClickedTermsAndConditions() = Utils.openUrl(getString(R.string.terms_and_condition_url), requireContext())
        fun onClickedSettingsIcon() = navigateTo(HomeFragmentDirections.toSettingsFragment())
        fun onClickedRewards() = navigateTo(HomeFragmentDirections.actionGlobalRewardsFragment())

        return HomeClickFunctions(
            onSendMoneyClicked = { onSendMoneyClicked() },
            onBuyAirtimeClicked = { onBuyAirtimeClicked() },
            onBuyGoodsClicked = { onBuyGoodsClicked() },
            onPayBillClicked = { onPayBillClicked() },
            onRequestMoneyClicked = { onRequestMoneyClicked() },
            onClickedAddNewAccount = { onClickedAddNewAccount() },
            onClickedTC = { onClickedTermsAndConditions() },
            onClickedSettingsIcon = { onClickedSettingsIcon() },
            onClickedRewards = { onClickedRewards() }
        )
    }

    private fun setComposeView() {
        binding.root.setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        binding.root.setContent {
            HomeScreen(
                channelsViewModel,
                homeClickFunctions = getHomeClickFunctions(),
                tipInterface = this@HomeFragment,
                balanceTapListener = this@HomeFragment,
                homeViewModel = homeViewModel
            )
        }
    }

    private fun observeForBonus() {
        collectLifecycleFlow(channelsViewModel.accountEventFlow) {
            if ( homeViewModel.homeState.value.bonuses.isNotEmpty())
                navigateTo(getTransferDirection(HoverAction.AIRTIME, homeViewModel.homeState.value.bonuses.first().userChannel.toString()))
        }
    }

    private fun observeForBalances() {
        collectLifecycleFlow(balancesViewModel.balanceAction) {
            attemptCallHover(balancesViewModel.userRequestedBalanceAccount.value, it)
        }

        collectLifecycleFlow(channelsViewModel.accountCallback) {
            askToCheckBalance(it)
        }

        collectLifecycleFlow(balancesViewModel.actionRunError) {
            UIHelper.flashAndReportError(requireActivity(), it)
        }
    }

    private fun getTransferDirection(type: String, channelId: String? = null): NavDirections {
        return HomeFragmentDirections.actionNavigationHomeToNavigationTransfer(type).also {
            if (channelId != null) it.channelId = channelId
        }
    }

    private fun attemptCallHover(account: Account?, action: HoverAction?) {
        action?.let { account?.let { callHover(account, action) } }
    }

    private fun callHover(account: Account, action: HoverAction) {
        (requireActivity() as AbstractHoverCallerActivity).runSession(account, action)
    }

    private fun askToCheckBalance(account: Account) {
        val dialog = StaxDialog(requireActivity()).setDialogTitle(R.string.check_balance_title)
            .setDialogMessage(R.string.check_balance_desc).setNegButton(R.string.later, null)
            .setPosButton(R.string.check_balance_title) { onTapBalanceRefresh(account) }
        dialog.showIt()
    }

    private fun navigateTo(navDirections: NavDirections) = (requireActivity() as MainActivity).checkPermissionsAndNavigate(navDirections)

    override fun onTipClicked(tipId: String?) {
        val destination = HomeFragmentDirections.actionNavigationHomeToWellnessFragment().apply { setTipId(tipId) }
        NavUtil.navigate(findNavController(), destination)
    }

    override fun onTapBalanceRefresh(account: Account?) {
        if (account != null) {
            AnalyticsUtil.logAnalyticsEvent(getString(R.string.refresh_balance), requireContext())
            balancesViewModel.requestBalance(account)
        }
    }

    override fun onTapBalanceDetail(accountId: Int) {
        findNavController().navigate(HomeFragmentDirections.actionNavigationHomeToAccountDetailsFragment(accountId))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}