package com.hover.stax.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import com.hover.sdk.actions.HoverAction
import com.hover.stax.R
import com.hover.stax.addChannels.ChannelsViewModel
import com.hover.stax.bonus.BonusViewModel
import com.hover.stax.databinding.FragmentHomeBinding
import com.hover.stax.presentation.home.HomeViewModel
import com.hover.stax.utils.AnalyticsUtil
import com.hover.stax.utils.NavUtil
import com.hover.stax.utils.Utils
import org.koin.androidx.viewmodel.ext.android.sharedViewModel


class HomeFragment : Fragment(), FinancialTipClickInterface {

	private var _binding: FragmentHomeBinding? = null
	private val binding get() = _binding!!

	private val bonusViewModel: BonusViewModel by sharedViewModel()
	private val channelsViewModel: ChannelsViewModel by sharedViewModel()

	private val homeViewModel: HomeViewModel by sharedViewModel()

	override fun onCreateView(inflater: LayoutInflater,
	                          container: ViewGroup?,
	                          savedInstanceState: Bundle?): View {
		AnalyticsUtil.logAnalyticsEvent(getString(R.string.visit_screen,
			getString(R.string.visit_home)), requireContext())
		_binding = FragmentHomeBinding.inflate(inflater, container, false)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		homeViewModel.getBonusList()
		homeViewModel.getAccounts()
		homeViewModel.getFinancialTips()

		binding.root.setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
		binding.root.setContent {
			HomeScreen(homeViewModel = homeViewModel,
				channelsViewModel = channelsViewModel,
				onSendMoneyClicked = { navigateTo(getTransferDirection(HoverAction.P2P)) },
				onBuyAirtimeClicked = { navigateTo(getTransferDirection(HoverAction.AIRTIME)) },
				onBuyGoodsClicked = { navigateTo(HomeFragmentDirections.actionNavigationHomeToMerchantFragment()) },
				onPayBillClicked = { navigateTo(HomeFragmentDirections.actionNavigationHomeToPaybillFragment()) },
				onRequestMoneyClicked = { navigateTo(HomeFragmentDirections.actionNavigationHomeToNavigationRequest()) },
				onClickedTC = {
					Utils.openUrl(getString(R.string.terms_and_condition_url),
						requireContext())
				},
				context = requireContext(),
				tipInterface = this@HomeFragment)
		}

		lifecycleScope.launchWhenStarted {
			channelsViewModel.accountEventFlow.collect {
				navigateTo(getTransferDirection(HoverAction.AIRTIME,
					bonusViewModel.bonuses.value.first().userChannel.toString()))
			}
		}
	}

	private fun getTransferDirection(type: String, channelId: String? = null): NavDirections {
		return HomeFragmentDirections.actionNavigationHomeToNavigationTransfer(type).also {
			if (channelId != null) it.channelId = channelId
		}
	}

	private fun navigateTo(navDirections: NavDirections) =
		(requireActivity() as MainActivity).checkPermissionsAndNavigate(navDirections)

	override fun onTipClicked(tipId: String?) {
		NavUtil.navigate(findNavController(),
			HomeFragmentDirections.actionNavigationHomeToWellnessFragment(tipId))
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}
}