package com.hover.stax.presentation.simcard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavDirections
import com.hover.sdk.actions.HoverAction
import com.hover.stax.MainNavigationDirections
import com.hover.stax.R
import com.hover.stax.accounts.AccountsViewModel
import com.hover.stax.addChannels.ChannelsViewModel
import com.hover.stax.databinding.FragmentSimBinding
import com.hover.stax.domain.model.Account
import com.hover.stax.home.MainActivity
import com.hover.stax.home.NavHelper
import com.hover.stax.hover.AbstractHoverCallerActivity
import com.hover.stax.presentation.home.BalanceTapListener
import com.hover.stax.presentation.home.BalancesViewModel
import com.hover.stax.utils.AnalyticsUtil
import com.hover.stax.utils.UIHelper
import com.hover.stax.utils.collectLifecycleFlow
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import timber.log.Timber

class SimFragment : Fragment(), BalanceTapListener {

	private var _binding: FragmentSimBinding? = null
	private val binding get() = _binding!!
	private val balancesViewModel: BalancesViewModel by sharedViewModel()
	private val channelsViewModel: ChannelsViewModel by sharedViewModel()
	private val simViewModel : SimViewModel by sharedViewModel()

	override fun onCreateView(inflater: LayoutInflater,
	                          container: ViewGroup?,
	                          savedInstanceState: Bundle?): View {
		AnalyticsUtil.logAnalyticsEvent(getString(R.string.visit_screen,
			getString(R.string.visit_sim)), requireContext())
		_binding = FragmentSimBinding.inflate(inflater, container, false)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		observeBalances()
		binding.root.setContent {
			SimScreen(simScreenClickFunctions = getSimScreenClickFunctions(),
				balanceTapListener = this@SimFragment)
		}
	}

	private fun observeBalances() {
		collectLifecycleFlow(balancesViewModel.balanceAction) {
			attemptCallHover(balancesViewModel.userRequestedBalanceAccount.value, it)
		}

		collectLifecycleFlow(balancesViewModel.actionRunError) {
			UIHelper.flashMessage(requireActivity(), it)
		}
	}

	private fun attemptCallHover(account: Account?, action: HoverAction?) {
		action?.let { account?.let { callHover(account, action) } }
	}

	private fun callHover(account: Account, action: HoverAction) {
		(requireActivity() as AbstractHoverCallerActivity).runSession(account, action)
	}

	private fun getSimScreenClickFunctions(): SimScreenClickFunctions {
		fun onClickedAddNewAccount() = NavHelper(requireActivity() as MainActivity).requestBasicPerms()
		fun onClickedSettingsIcon() = navigateTo(SimFragmentDirections.toSettingsFragment())
		fun onBuyAirtimeClicked() = navigateTo(getTransferDirection(HoverAction.AIRTIME))

		return SimScreenClickFunctions(onClickedAddNewAccount = { onClickedAddNewAccount() },
			onClickedSettingsIcon = { onClickedSettingsIcon() },
			onClickedBuyAirtime = { onBuyAirtimeClicked() })
	}

	private fun getTransferDirection(type: String, channelId: String? = null): NavDirections {
		return SimFragmentDirections.toTransferFragment(type).also {
			if (channelId != null) it.channelId = channelId
		}
	}

	private fun navigateTo(navDirections: NavDirections) =
		(requireActivity() as MainActivity).checkPermissionsAndNavigate(navDirections)

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}

	override fun onTapBalanceRefresh(account: Account?) {
		if (account != null) {
			AnalyticsUtil.logAnalyticsEvent(getString(R.string.refresh_sim_airtime_balance),
				requireContext())
			balancesViewModel.requestBalance(account)
		}
	}

	override fun onTapBalanceDetail(accountId: Int) {}
}