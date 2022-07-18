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
import com.hover.stax.databinding.FragmentSimBinding
import com.hover.stax.home.MainActivity
import com.hover.stax.utils.AnalyticsUtil
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import timber.log.Timber

class SimFragment : Fragment() {

	private var _binding: FragmentSimBinding? = null
	private val binding get() = _binding!!
	private val viewModel: AccountsViewModel by sharedViewModel()

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
		setObservers()
		binding.root.setContent { SimScreen(simScreenClickFunctions = getSimScreenClickFunctions()) }
	}

	private fun setObservers() {
		viewModel.simSubscriptionIds.observe(viewLifecycleOwner) {
			Timber.i("subscription ids size: ${it.size}")
		}
		viewModel.telecomAccounts.observe(viewLifecycleOwner) {
			Timber.i("telecomAccounts size ${it.size}")
		}
	}


	private fun getSimScreenClickFunctions(): SimScreenClickFunctions {
		fun onClickedAddNewAccount() =
			(requireActivity() as MainActivity).checkPermissionsAndNavigate(MainNavigationDirections.actionGlobalAddChannelsFragment()
				.setIsForTelecom(true))

		fun onClickedSettingsIcon() = navigateTo(SimFragmentDirections.toSettingsFragment())
		fun onBuyAirtimeClicked() = navigateTo(getTransferDirection(HoverAction.AIRTIME))

		return SimScreenClickFunctions(onClickedAddNewAccount = { onClickedAddNewAccount() },
			onClickedSettingsIcon = { onClickedSettingsIcon() },
			onClickedBuyAirtime = { onBuyAirtimeClicked() },
			onClickedCheckBalance = {})
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
}