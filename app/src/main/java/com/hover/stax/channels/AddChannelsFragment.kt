package com.hover.stax.channels

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import com.hover.stax.R
import com.hover.stax.balances.BalancesViewModel
import com.hover.stax.databinding.FragmentAddChannelsBinding
import com.hover.stax.utils.AnalyticsUtil
import com.hover.stax.utils.Constants
import com.hover.stax.utils.UIHelper
import com.hover.stax.utils.Utils
import com.hover.stax.views.RequestServiceDialog

import com.hover.stax.views.StaxDialog
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber


class AddChannelsFragment : Fragment(), ChannelsRecyclerViewAdapter.SelectListener {

    private val channelsViewModel: ChannelsViewModel by viewModel()
    private val balancesViewModel: BalancesViewModel by sharedViewModel()

    private var _binding: FragmentAddChannelsBinding? = null
    private val binding get() = _binding!!

    private val selectAdapter: ChannelsRecyclerViewAdapter = ChannelsRecyclerViewAdapter(ArrayList(0), this)
    private var tracker: SelectionTracker<Long>? = null

    private var dialog: StaxDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        refreshChannelsIfRequired()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAddChannelsBinding.inflate(inflater, container, false)

        AnalyticsUtil.logAnalyticsEvent(getString(R.string.visit_screen, getString(R.string.visit_link_account)), requireContext())
        initArguments()

        return binding.root
    }

    private fun initArguments() = arguments?.let { IS_FORCE_RETURN = it.getBoolean(FORCE_RETURN_DATA, true) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.channelsListCard.showProgressIndicator()

        binding.channelsListCard.setTitle(getString(R.string.add_accounts_to_stax))
        binding.selectedList.apply {
            layoutManager = UIHelper.setMainLinearManagers(requireContext())
            setHasFixedSize(true)
            isNestedScrollingEnabled = false
        }

        binding.channelsList.apply {
            layoutManager = UIHelper.setMainLinearManagers(requireContext())
            setHasFixedSize(true)
            adapter = selectAdapter
            isNestedScrollingEnabled = false
        }
        setupEmptyState()

        setUpMultiselect()
        setSearchInputWatcher()


        channelsViewModel.selectedChannels.observe(viewLifecycleOwner) { onSelectedLoaded(it) }
        channelsViewModel.simChannels.observe(viewLifecycleOwner) { if(it.isEmpty())  setError(R.string.channels_error_nosim) else Timber.i("loaded") }
        channelsViewModel.filteredChannels.observe(viewLifecycleOwner){ loadFilteredChannels(it) }
        channelsViewModel.allChannels.observe(viewLifecycleOwner) { Timber.i("Loaded all channels") }
    }
    private fun setupEmptyState() {
        binding.emptyState.root.visibility = GONE
        binding.emptyState.informUs.setOnClickListener {
            RequestServiceDialog(requireActivity()).showIt()
        }
    }

    private fun setSearchInputWatcher() {
        val searchInputWatcher: TextWatcher = object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun afterTextChanged(editable: Editable) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
               channelsViewModel.runChannelFilter(charSequence.toString())
            }
        }
        binding.searchInput.addTextChangedListener(searchInputWatcher)
    }
    private fun setUpMultiselect() {
        tracker = SelectionTracker.Builder(
            "channelSelection", binding.channelsList,
            ChannelKeyProvider(selectAdapter),
            ChannelLookup(binding.channelsList),
            StorageStrategy.createLongStorage()
        ).withSelectionPredicate(SelectionPredicates.createSelectAnything())
            .build()
        selectAdapter.setTracker(tracker!!)

        binding.continueBtn.setOnClickListener {
            aggregateSelectedChannels(tracker!!)
        }
    }

    private fun onSelectedLoaded(channels: List<Channel>) {
        binding.channelsListCard.hideProgressIndicator()

        showSelected(!channels.isNullOrEmpty())
        if (!channels.isNullOrEmpty())
            binding.selectedList.adapter = ChannelsRecyclerViewAdapter(channels, this)
    }

    private fun showSelected(visible: Boolean) {
        binding.selectedChannelsCard.visibility = if (visible) VISIBLE else GONE
        binding.channelsListCard.setBackButtonVisibility(if (visible) GONE else VISIBLE)
    }

    private fun loadFilteredChannels(channels: List<Channel>) {
        binding.channelsListCard.hideProgressIndicator()

        if (!channels.isNullOrEmpty()) {
            updateAdapter(Channel.sort(channels, false))
            binding.emptyState.root.visibility = GONE
            binding.channelsList.visibility = VISIBLE
            binding.errorText.visibility = GONE
        }
        else if(channelsViewModel.isInSearchMode()) showEmptyState()
        else setError(R.string.channels_error_nodata)
    }

    private fun showEmptyState() {
        val content = resources.getString(R.string.no_accounts_found_desc,  channelsViewModel.filterQuery.value!!)
        binding.emptyState.noAccountFoundDesc.apply {
            text = HtmlCompat.fromHtml(content, HtmlCompat.FROM_HTML_MODE_LEGACY)
            movementMethod = LinkMovementMethod.getInstance()
        }

        binding.emptyState.root.visibility = VISIBLE
        binding.channelsList.visibility = GONE
        binding.errorText.visibility = GONE
    }


    private fun updateAdapter(channels: List<Channel>) {
        selectAdapter.updateList(channels)
    }

    private fun setError(message: Int) {
        binding.errorText.apply {
                visibility = VISIBLE
                text = getString(message)
        }
    }

    private fun aggregateSelectedChannels(tracker: SelectionTracker<Long>) {
        if (tracker.selection.isEmpty)
            setError(R.string.channels_error_noselect)
        else {
            binding.errorText.visibility = GONE

            val selectedChannels = mutableListOf<Channel>()
            tracker.selection.forEach { selection ->
                selectedChannels.addAll(selectAdapter.channelList.filter { it.id.toLong() == selection })
            }

            channelsViewModel.setChannelsSelected(selectedChannels)
            channelsViewModel.createAccounts(selectedChannels)

            showCheckBalanceDialog(
                if (selectedChannels.size > 1) R.string.check_balance_alt_plural
                else R.string.check_balance_alt,
                selectedChannels
            )
        }
    }

    private fun showCheckBalanceDialog(message: Int, channels: List<Channel>) {
        dialog = StaxDialog(requireActivity())
            .setDialogTitle(R.string.check_balance_title)
            .setDialogMessage(message)
            .setNegButton(R.string.later) { runActions(channels, false) }
            .setPosButton(R.string.check_balance_title) { runActions(channels, true) }
        dialog!!.showIt()
    }

    private fun runActions(channels: List<Channel>, checkBalance: Boolean) {
        if (activity != null && isAdded)
            requireActivity().onBackPressed()

        if (checkBalance)
            balancesViewModel.actions.observe(viewLifecycleOwner) {
                if (channels.size == 1)
                    balancesViewModel.setRunning(channels.first())
                else
                    balancesViewModel.setAllRunning(requireActivity())
            }
    }

    override fun clickedChannel(channel: Channel) {
        if (!channel.selected)
            showCheckBalanceDialog(R.string.check_balance_alt, listOf(channel))
    }

    //channels will be loaded only once after install then deferred to weekly.
    private fun refreshChannelsIfRequired() {
        if (!Utils.getBoolean(Constants.CHANNELS_REFRESHED, requireActivity())) {
            Timber.i("Reloading channels")
            val wm = WorkManager.getInstance(requireContext())
            wm.beginUniqueWork(UpdateChannelsWorker.CHANNELS_WORK_ID, ExistingWorkPolicy.KEEP, UpdateChannelsWorker.makeWork()).enqueue()

            Utils.saveBoolean(Constants.CHANNELS_REFRESHED, true, requireActivity())
            return
        }

        Timber.i("Channels already reloaded")
    }

    override fun onDestroyView() {
        super.onDestroyView()

        dialog?.let { if (it.isShowing) it.dismiss() }
        _binding = null
    }

    companion object {
        var IS_FORCE_RETURN = true
        const val FORCE_RETURN_DATA = "force_return_data"
    }
}