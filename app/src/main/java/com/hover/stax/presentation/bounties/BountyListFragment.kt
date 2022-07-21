package com.hover.stax.presentation.bounties

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.hover.stax.countries.CountryAdapter
import com.hover.stax.data.remote.workers.UpdateBountyTransactionsWorker
import com.hover.stax.databinding.FragmentBountyListBinding
import com.hover.stax.domain.model.Bounty
import com.hover.stax.domain.model.ChannelBounties
import com.hover.stax.hover.AbstractHoverCallerActivity
import com.hover.stax.utils.*
import com.hover.stax.utils.network.NetworkMonitor
import com.hover.stax.views.AbstractStatefulInput
import com.hover.stax.views.StaxDialog
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber


class BountyListFragment : Fragment(), BountyListItem.SelectListener, CountryAdapter.SelectListener {

    private lateinit var networkMonitor: NetworkMonitor

    private val bountyViewModel: BountyViewModel by sharedViewModel()
    private val bountiesViewModel: BountiesViewModel by viewModel()

    private var _binding: FragmentBountyListBinding? = null
    private val binding get() = _binding!!

    private var dialog: StaxDialog? = null

    private val bountyAdapter = BountyAdapter(this)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        AnalyticsUtil.logAnalyticsEvent(getString(R.string.visit_screen, getString(R.string.visit_bounty_list)), requireActivity())

        _binding = FragmentBountyListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        networkMonitor = NetworkMonitor(requireActivity())

        initRecyclerView()

        startObservers()

        binding.bountiesRecyclerView.adapter = bountyAdapter
        binding.bountyCountryDropdown.isEnabled = false
        binding.countryFilter.apply {
//            showProgressIndicator()
            setOnClickIcon {
                NavUtil.navigate(findNavController(), BountyListFragmentDirections.actionBountyListFragmentToNavigationSettings())
            }
        }
    }

    override fun onResume() {
        super.onResume()
        forceUserToBeOnline()
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

    private fun initCountryDropdown(countryCodes: List<String>) = binding.bountyCountryDropdown.apply {
        setListener(this@BountyListFragment)
        updateChoices(countryCodes, bountyViewModel.country)
        isEnabled = true
    }

    private fun initRecyclerView() {
        binding.bountiesRecyclerView.layoutManager = UIHelper.setMainLinearManagers(context)
        bountyAdapter.setHasStableIds(true)
    }

    private fun startObservers() = with(bountiesViewModel) {
        collectLifecycleFlow(countryList) {
            initCountryDropdown(it)
        }

        collectLifecycleFlow(bountiesState) {
            when {
                it.loading -> {
                    showLoadingState()
                    Timber.e("Loading bounties")
                }
                !it.loading && it.bounties.isNotEmpty() -> {
                    showBounties(it.bounties)
                    Timber.e("Found ${it.bounties.size} bounties to display")
                }
                !it.loading && it.bounties.isEmpty() -> hideLoadingState()
                !it.loading && it.error.isNotEmpty() -> {
                    hideLoadingState()
                    Timber.e("An error occurred while loading bounties")
                }
            }
        }

        collectLifecycleFlow(country) {
            bountyAdapter.clear()
        }
    }

    private fun showBounties(channelBounties: List<ChannelBounties>) {
        if (channelBounties.isNotEmpty()/* && (bountiesViewModel.country == CountryAdapter.CODE_ALL_COUNTRIES
                    || bountiesViewModel.bountiesState.value.bounties.firstOrNull()?.bounties.f == bountyViewModel.country */) {
            hideLoadingState()

            Timber.e("Showing ${channelBounties.size} bounties")

            binding.msgNoBounties.visibility = View.GONE
            binding.bountiesRecyclerView.visibility = View.VISIBLE

            bountyAdapter.addItems(channelBounties)
        } else {
            binding.msgNoBounties.visibility = View.VISIBLE
            binding.bountiesRecyclerView.visibility = View.GONE
        }
    }

    override fun viewTransactionDetail(uuid: String?) {
        uuid?.let { NavUtil.showTransactionDetailsFragment(findNavController(), uuid) }
    }

    override fun viewBountyDetail(b: Bounty) {
        if (bountiesViewModel.isSimPresent(b)) showBountyDescDialog(b) else showSimErrorDialog(b)
    }

    override fun countrySelect(countryCode: String) {
        showLoadingState()

        bountiesViewModel.loadBounties(countryCode)
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

    private fun showLoadingState() {
        binding.bountyCountryDropdown.setState(getString(R.string.filtering_in_progress), AbstractStatefulInput.INFO)
        binding.bountiesRecyclerView.visibility = View.GONE
    }

    private fun hideLoadingState() {
        binding.bountyCountryDropdown.setState(null, AbstractStatefulInput.NONE)
        binding.bountiesRecyclerView.visibility = View.VISIBLE
    }

    private fun retrySimMatch(b: Bounty?) {
//        with(bountiesViewModel.sims) {
//            removeObservers(viewLifecycleOwner)
//            observe(viewLifecycleOwner) { b?.let { viewBountyDetail(b) } }
//        }
//
//        Hover.updateSimInfo(requireActivity())
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