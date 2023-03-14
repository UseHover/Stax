package com.hover.stax.accounts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.hover.stax.R
import com.hover.stax.databinding.FragmentUsdcAccountBinding
import com.hover.stax.hover.AbstractBalanceCheckerFragment
import com.hover.stax.presentation.accounts.UsdcAccountScreen
import com.hover.stax.presentation.home.BalancesViewModel
import com.hover.stax.utils.AnalyticsUtil
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class USDCAccountDetailFragment : AbstractBalanceCheckerFragment() {

	private val viewModel: AccountDetailViewModel by sharedViewModel()
	private val balancesViewModel: BalancesViewModel by sharedViewModel()

	private var _binding: FragmentUsdcAccountBinding? = null
	private val binding get() = _binding!!


	private val args: USDCAccountDetailFragmentArgs by navArgs()

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding = FragmentUsdcAccountBinding.inflate(inflater, container, false)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		AnalyticsUtil.logAnalyticsEvent(getString(R.string.visit_screen, getString(R.string.visit_channel)), requireActivity())

		binding.root.setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
		binding.root.setContent {
			UsdcAccountScreen(viewModel, balancesViewModel, findNavController())
		}

		viewModel.setAccount(args.accountId)
	}
}