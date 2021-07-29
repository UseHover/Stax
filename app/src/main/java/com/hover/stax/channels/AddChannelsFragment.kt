package com.hover.stax.channels

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.selection.*
import com.hover.stax.R
import com.hover.stax.balances.BalanceAdapter.BalanceListener
import com.hover.stax.balances.BalancesViewModel
import com.hover.stax.databinding.FragmentChannelsListBinding
import com.hover.stax.home.MainActivity
import com.hover.stax.utils.Constants
import com.hover.stax.utils.UIHelper
import com.hover.stax.utils.Utils
import com.hover.stax.views.StaxDialog
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber


class AddChannelsFragment : Fragment(), ChannelsRecyclerViewAdapter.SelectListener {

    private val channelsViewModel: ChannelsViewModel by viewModel()
    private val balancesViewModel: BalancesViewModel by sharedViewModel()

    private var _binding: FragmentChannelsListBinding? = null
    private val binding get() = _binding!!

    private var selectAdapter: ChannelsRecyclerViewAdapter = ChannelsRecyclerViewAdapter(ArrayList(0), this)
    private var tracker: SelectionTracker<Long>? = null
    private var dialog: StaxDialog? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentChannelsListBinding.inflate(inflater, container, false)

        Utils.logAnalyticsEvent(getString(R.string.visit_screen, getString(R.string.visit_link_account)), requireContext())
        initArguments()

        return binding.root
    }

    private fun initArguments() = arguments?.let { IS_FORCE_RETURN = it.getBoolean(FORCE_RETURN_DATA, true) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.channelsListCard.setTitle(getString(getTitle()))
        binding.selectedList.layoutManager = UIHelper.setMainLinearManagers(requireContext())
        binding.selectedList.setHasFixedSize(true)
        binding.channelsList.layoutManager = UIHelper.setMainLinearManagers(requireContext())
        binding.channelsList.setHasFixedSize(true)
        binding.channelsList.adapter = selectAdapter
        setUpMultiselect()

        channelsViewModel.selectedChannels.observe(viewLifecycleOwner) { channels -> onSelectedLoaded(channels) }
        channelsViewModel.simChannels.observe(viewLifecycleOwner) { channels -> onSimsLoaded(channels) }
        channelsViewModel.allChannels.observe(viewLifecycleOwner) { channels -> onAllLoaded(channels) }
    }

    private fun setUpMultiselect() {
        if (Utils.variant == Constants.VARIANT_2 || Utils.variant == Constants.VARIANT_3) {
            tracker = SelectionTracker.Builder(
                    "channelSelection", binding.channelsList,
                    ChannelKeyProvider(selectAdapter),
                    ChannelLookup(binding.channelsList),
                    StorageStrategy.createLongStorage()).withSelectionPredicate(SelectionPredicates.createSelectAnything())
                    .build()
            selectAdapter.setTracker(tracker!!)

            binding.continueBtn.apply {
                visibility = VISIBLE
                setOnClickListener {
                    aggregateSelectedChannels(tracker!!)
                }
            }
        }
    }

    private fun getTitle(): Int {
        return when (Utils.variant) {
            Constants.VARIANT_1 -> R.string.add_an_account
            Constants.VARIANT_2, Constants.VARIANT_3 -> R.string.add_accounts_to_stax
            else -> R.string.add_an_account //default title
        }
    }

    private fun onSelectedLoaded(channels: List<Channel>) {
        showSelected(!channels.isNullOrEmpty())
        if (!channels.isNullOrEmpty())
            binding.selectedList.adapter = ChannelsRecyclerViewAdapter(channels, this)
    }

    private fun showSelected(visible: Boolean) {
        binding.selectedChannelsCard.visibility = if (visible) VISIBLE else GONE
        binding.channelsListCard.setBackButtonVisibility(if (visible) GONE else VISIBLE)
    }

    private fun onSimsLoaded(channels: List<Channel>) {
        if (!channels.isNullOrEmpty()) {
            binding.errorText.visibility = GONE
            updateAdapter(Channel.sort(channels, false));
        }
    }

    private fun onAllLoaded(channels: List<Channel>) {
        if (!channels.isNullOrEmpty() && binding.channelsList.adapter?.itemCount == 0) {
            setError(R.string.channels_error_nosim)
            updateAdapter(Channel.sort(channels, false));
        } else if (channels.isNullOrEmpty())
            setError(R.string.channels_error_nodata)
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
            .setNegButton(R.string.later) { saveChannels(channels, false) }
            .setPosButton(R.string.check_balance_title) { saveChannels(channels, true) }
        dialog!!.showIt()
    }

    private fun saveChannels(channels: List<Channel>, checkBalance: Boolean) {
        channelsViewModel.setChannelsSelected(channels)
        requireActivity().onBackPressed()

        if (checkBalance) balancesViewModel.actions.observe(viewLifecycleOwner, {
            if (channels.size == 1)
                balancesViewModel.setRunning(channels.first().id)
            else
                balancesViewModel.setAllRunning(requireActivity())
        })
    }

    private fun goToChannelsDetailsScreen(channel: Channel) {
        val balanceListener: BalanceListener? = activity as MainActivity?
        balanceListener?.onTapDetail(channel.id)
    }

    override fun clickedChannel(channel: Channel) {
        if (IS_FORCE_RETURN || !channel.selected)
            showCheckBalanceDialog(R.string.check_balance_alt, listOf(channel))
        else
            goToChannelsDetailsScreen(channel)
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