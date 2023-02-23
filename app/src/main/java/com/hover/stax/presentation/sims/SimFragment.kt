/*
 * Copyright 2022 Stax
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hover.stax.presentation.sims

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import com.hover.sdk.actions.HoverAction
import com.hover.stax.R
import com.hover.stax.domain.model.USSDAccount
import com.hover.stax.home.MainActivity
import com.hover.stax.hover.AbstractBalanceCheckerFragment
import com.hover.stax.presentation.home.BalancesViewModel
import com.hover.stax.utils.AnalyticsUtil
import com.hover.stax.utils.UIHelper
import com.hover.stax.utils.collectLifecycleFlow
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class SimFragment : AbstractBalanceCheckerFragment() {

    private val balancesViewModel: BalancesViewModel by sharedViewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        ComposeView(requireContext()).apply {
            AnalyticsUtil.logAnalyticsEvent(
                getString(R.string.visit_screen, getString(R.string.visit_sim)), requireContext()
            )

            setContent {
                SimScreen(
                    refreshBalance = {  },
                    buyAirtime = { navigateTo(SimFragmentDirections.toTransferFragment(HoverAction.AIRTIME)) },
                    navTo = { dest -> navigateTo(dest) }
                )
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeBalances()
    }

    private fun observeBalances() {
//        collectLifecycleFlow(balancesViewModel.balanceAction) {
//            attemptCallHover(it.first, it.second)
//        }

        collectLifecycleFlow(balancesViewModel.actionRunError) {
            UIHelper.flashAndReportMessage(requireActivity(), it)
        }
    }

    private fun attemptCallHover(account: USSDAccount?, action: HoverAction?) {
        action?.let { account?.let { callHover(checkBalance, generateSessionBuilder(account, action)) } }
    }

    private fun navigateTo(dest: Int) = findNavController().navigate(dest)

    private fun navigateTo(navDirections: NavDirections) =
        (requireActivity() as MainActivity).checkPermissionsAndNavigate(navDirections)
}