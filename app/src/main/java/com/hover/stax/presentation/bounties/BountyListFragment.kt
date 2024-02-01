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
package com.hover.stax.presentation.bounties

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import com.hover.sdk.actions.HoverAction
import com.hover.sdk.api.Hover
import com.hover.stax.R
import com.hover.stax.channels.UpdateChannelsWorker
import com.hover.stax.data.remote.workers.UpdateBountyTransactionsWorker
import com.hover.stax.databinding.FragmentBountyListBinding
import com.hover.stax.domain.model.Bounty
import com.hover.stax.hover.BountyContract
import com.hover.stax.utils.AnalyticsUtil
import com.hover.stax.utils.NavUtil
import com.hover.stax.utils.Utils
import com.hover.stax.utils.collectLifecycleFlow
import com.hover.stax.utils.network.NetworkMonitor
import com.hover.stax.views.StaxDialog
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class BountyListFragment : Fragment() {

    private lateinit var networkMonitor: NetworkMonitor

    private val bountiesViewModel: BountyViewModel by viewModel()

    private var _binding: FragmentBountyListBinding? = null
    private val binding get() = _binding!!

    private var dialog: StaxDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        AnalyticsUtil.logAnalyticsEvent(getString(R.string.visit_screen, getString(R.string.visit_bounty_list)), requireActivity())

        _binding = FragmentBountyListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        networkMonitor = NetworkMonitor(requireActivity())

        initBountyList()
        observeBountyEvents()

        binding.countryFilter.apply {
            setOnClickIcon {
                NavUtil.navigate(findNavController(), BountyListFragmentDirections.actionBountyListFragmentToNavigationSettings())
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Hover.initialize(requireContext())
        forceUserToBeOnline()
    }

    private fun observeBountyEvents() = collectLifecycleFlow(bountiesViewModel.bountySelectEvent) {
        when (it) {
            is BountySelectEvent.ViewBountyDetail -> viewBountyDetail(it.bounty)
            is BountySelectEvent.ViewTransactionDetail -> NavUtil.showTransactionDetailsFragment(findNavController(), it.uuid)
        }
    }

    private fun forceUserToBeOnline() {
        if (isAdded && networkMonitor.isNetworkConnected) {
            updateActionConfig()
            updateChannelsWorker()
            updateBountyTransactionWorker()
        } else showOfflineDialog()
    }

    private fun updateActionConfig() = lifecycleScope.launch {
        Hover.updateActionConfigs(
            object : Hover.DownloadListener {
                override fun onError(p0: String?) {
                    AnalyticsUtil.logErrorAndReportToFirebase(BountyListFragment::class.java.simpleName, "Failed to update action configs: $p0", null)
                }

                override fun onSuccess(p0: ArrayList<HoverAction>?) {
                    Timber.i("Action configs initialized successfully $p0")
                }
            },
            requireActivity()
        )
    }

    private fun updateChannelsWorker() = with(WorkManager.getInstance(requireActivity())) {
        beginUniqueWork(UpdateChannelsWorker.CHANNELS_WORK_ID, ExistingWorkPolicy.REPLACE, UpdateChannelsWorker.makeWork()).enqueue()
        enqueueUniquePeriodicWork(UpdateChannelsWorker.TAG, ExistingPeriodicWorkPolicy.REPLACE, UpdateChannelsWorker.makeToil())
    }

    private fun updateBountyTransactionWorker() = with(WorkManager.getInstance(requireActivity())) {
        beginUniqueWork(UpdateBountyTransactionsWorker.BOUNTY_TRANSACTION_WORK_ID, ExistingWorkPolicy.REPLACE, UpdateBountyTransactionsWorker.makeWork()).enqueue()
        enqueueUniquePeriodicWork(UpdateBountyTransactionsWorker.TAG, ExistingPeriodicWorkPolicy.REPLACE, UpdateBountyTransactionsWorker.makeToil())
    }

    private fun showOfflineDialog() {
        binding.countryFilter.hideProgressIndicator()

        dialog = StaxDialog(requireActivity())
            .setDialogTitle(R.string.internet_required)
            .setDialogMessage(R.string.internet_required_bounty_desc)
            .setPosButton(R.string.try_again) { forceUserToBeOnline() }
            .setNegButton(R.string.btn_cancel) { requireActivity().finish() }
            .makeSticky()
        dialog!!.showIt()
    }

    private fun initBountyList() {
        binding.bountyList.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                BountyList(bountyViewModel = bountiesViewModel)
            }
        }
    }

    private fun viewBountyDetail(b: Bounty) {
        if (bountiesViewModel.isSimPresent(b)) showBountyDescDialog(b) else showSimErrorDialog(b)
    }

    private fun showSimErrorDialog(b: Bounty) {
        dialog = StaxDialog(requireActivity())
            .setDialogTitle(getString(R.string.bounty_sim_err_header))
            .setDialogMessage(getString(R.string.bounty_sim_err_desc, b.action.network_name))
            .setNegButton(R.string.btn_cancel, null)
            .setPosButton(R.string.retry) { if (activity != null) retrySimMatch(b) }
        dialog!!.showIt()
    }

    private fun showBountyDescDialog(b: Bounty) {
        dialog = StaxDialog(requireActivity())
            .setDialogTitle(
                getString(
                    R.string.bounty_claim_title, b.action.root_code,
                    HoverAction.getHumanFriendlyType(requireContext(), b.action.transaction_type)
                )
            )
            .setDialogMessage(getString(R.string.bounty_claim_explained, b.getInstructions(requireActivity())))
            .setPosButton(R.string.start_USSD_Flow) { startBounty(b) }
        dialog!!.showIt()
    }

    private fun startBounty(b: Bounty) {
        Utils.setFirebaseMessagingTopic("BOUNTY".plus(b.action.root_code))
        AnalyticsUtil.logAnalyticsEvent(getString(R.string.clicked_start_bounty), requireContext())
        bounty.launch(b.action)
    }

    private val bounty = registerForActivityResult(BountyContract()) { data: Intent? ->
        if (data != null && data.extras != null && data.extras!!.getString("uuid") != null) {
            NavUtil.showTransactionDetailsFragment(findNavController(), data.extras!!.getString("uuid")!!)
        }
    }

    private fun retrySimMatch(b: Bounty?) {
        b?.let { viewBountyDetail(b) }
        Hover.updateSimInfo(requireActivity())
    }

    override fun onPause() {
        super.onPause()
        dismissDialog()
    }

    private fun dismissDialog() {
        if (dialog != null && dialog!!.isShowing)
            dialog!!.dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        dismissDialog()

        _binding = null
    }
}