package com.hover.stax.presentation.bounties

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
import com.hover.stax.hover.AbstractHoverCallerActivity
import com.hover.stax.utils.AnalyticsUtil
import com.hover.stax.utils.NavUtil
import com.hover.stax.utils.Utils
import com.hover.stax.utils.collectLifecycleFlow
import com.hover.stax.utils.network.NetworkMonitor
import com.hover.stax.views.StaxDialog
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber


class BountyListFragment : Fragment()  {

    private lateinit var networkMonitor: NetworkMonitor

    private val bountiesViewModel: BountyViewModel by viewModel()

    private var _binding: FragmentBountyListBinding? = null
    private val binding get() = _binding!!

    private var dialog: StaxDialog? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
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
        Hover.initialize(requireActivity(), object : Hover.DownloadListener {
            override fun onError(p0: String?) {
                AnalyticsUtil.logErrorAndReportToFirebase(BountyListFragment::class.java.simpleName, "Failed to update action configs: $p0", null)
            }

            override fun onSuccess(p0: ArrayList<HoverAction>?) {
                Timber.i("Action configs initialized successfully $p0")
            }
        })
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
                    HoverAction.getHumanFriendlyType(requireContext(), b.action.transaction_type), b.action.bounty_amount
                )
            )
            .setDialogMessage(getString(R.string.bounty_claim_explained, b.action.bounty_amount, b.getInstructions(requireActivity())))
            .setPosButton(R.string.start_USSD_Flow) { startBounty(b) }
        dialog!!.showIt()
    }

    private fun startBounty(b: Bounty) {
        Utils.setFirebaseMessagingTopic("BOUNTY".plus(b.action.root_code))
        (requireActivity() as AbstractHoverCallerActivity).makeRegularCall(b.action, R.string.clicked_start_bounty)
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