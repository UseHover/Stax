package com.hover.stax.bounties

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import com.hover.sdk.actions.HoverAction
import com.hover.sdk.api.Hover
import com.hover.stax.R
import com.hover.stax.channels.Channel
import com.hover.stax.channels.UpdateChannelsWorker
import com.hover.stax.countries.CountryAdapter
import com.hover.stax.databinding.FragmentBountyListBinding
import com.hover.stax.home.MainActivity
import com.hover.stax.transactions.StaxTransaction
import com.hover.stax.transactions.UpdateBountyTransactionsWorker
import com.hover.stax.utils.AnalyticsUtil
import com.hover.stax.utils.NavUtil
import com.hover.stax.utils.UIHelper
import com.hover.stax.utils.Utils
import com.hover.stax.utils.network.NetworkMonitor
import com.hover.stax.views.AbstractStatefulInput
import com.hover.stax.views.StaxDialog
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import timber.log.Timber


class BountyListFragment : Fragment(), BountyListItem.SelectListener, CountryAdapter.SelectListener {

    private lateinit var networkMonitor: NetworkMonitor

    private val bountyViewModel: BountyViewModel by sharedViewModel()
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

        startObservers()

        binding.bountiesRecyclerView.adapter = bountyAdapter
        binding.bountyCountryDropdown.isEnabled = false
        binding.countryFilter.apply {
            showProgressIndicator()
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

    private fun updateActionConfig() = Hover.initialize(requireActivity(), object : Hover.DownloadListener {
        override fun onError(p0: String?) {
            AnalyticsUtil.logErrorAndReportToFirebase(BountyListFragment::class.java.simpleName, "Failed to update action configs: $p0", null)
        }

        override fun onSuccess(p0: ArrayList<HoverAction>?) {
            Timber.i("Action configs initialized successfully $p0")
        }
    })

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

    private fun initCountryDropdown(channels: List<Channel>) = binding.bountyCountryDropdown.apply {
        setListener(this@BountyListFragment)
        updateChoices(channels, bountyViewModel.currentCountryFilter.value)
        isEnabled = true
    }

    private fun startObservers() = with(bountyViewModel) {
        val actionsObserver = object : Observer<List<HoverAction>> {
            override fun onChanged(t: List<HoverAction>?) {
                Timber.v("Actions update: ${t?.size}")
            }

        }
        val txnObserver = object : Observer<List<StaxTransaction>> {
            override fun onChanged(t: List<StaxTransaction>?) {
                Timber.v("Transactions update ${t?.size}")
            }
        }

        actions.observe(viewLifecycleOwner, actionsObserver)
        transactions.observe(viewLifecycleOwner, txnObserver)
        sims.observe(viewLifecycleOwner) { Timber.v("Sims update ${it.size}") }
        bounties.observe(viewLifecycleOwner) { updateChannelList(channels.value, it) }
        channels.observe(viewLifecycleOwner) {
            initCountryDropdown(it)
            updateChannelList(it, bounties.value)
        }
    }

    private fun updateChannelList(channels: List<Channel>?, bounties: List<Bounty>?) {
        binding.countryFilter.hideProgressIndicator()

        if (!channels.isNullOrEmpty() && !bounties.isNullOrEmpty() &&
            bountyViewModel.country == CountryAdapter.CODE_ALL_COUNTRIES || channels?.firstOrNull()?.countryAlpha2 == bountyViewModel.country
        ) {
            hideLoadingState()

            binding.msgNoBounties.visibility = View.GONE
            binding.bountiesRecyclerView.visibility = View.VISIBLE

            showBounties(channels, bounties!!)
        } else {
            binding.msgNoBounties.visibility = View.VISIBLE
            binding.bountiesRecyclerView.visibility = View.GONE
        }
    }

    private fun showBounties(channels: List<Channel>, bounties: List<Bounty>) = lifecycleScope.launch {
        val openBounties = bounties.filter { it.action.bounty_is_open || it.transactionCount != 0 }

        val channelBounties = channels.filter { c ->
            openBounties.any { it.action.channel_id == c.id }
        }.map { channel ->
            ChannelBounties(channel, openBounties.filter { it.action.channel_id == channel.id })
        }

        bountyAdapter.submitList(channelBounties)
    }

    override fun viewTransactionDetail(uuid: String?) {
        uuid?.let { NavUtil.showTransactionDetailsFragment(findNavController(), uuid) }
    }

    override fun viewBountyDetail(b: Bounty) {
        if (bountyViewModel.isSimPresent(b)) showBountyDescDialog(b) else showSimErrorDialog(b)
    }

    override fun countrySelect(countryCode: String) {
        showLoadingState()

        bountyViewModel.filterChannels(countryCode).observe(viewLifecycleOwner) { updateChannelList(it, bountyViewModel.bounties.value) }
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
        (requireActivity() as MainActivity).makeCall(b.action)
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
        with(bountyViewModel.sims) {
            removeObservers(viewLifecycleOwner)
            observe(viewLifecycleOwner) { b?.let { viewBountyDetail(b) } }
        }

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