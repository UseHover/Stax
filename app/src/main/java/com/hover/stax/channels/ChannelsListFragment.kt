package com.hover.stax.channels

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.selection.*
import androidx.recyclerview.widget.RecyclerView
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


class ChannelsListFragment : Fragment(), ChannelsRecyclerViewAdapter.SelectListener {

    private val channelsViewModel: ChannelsViewModel by viewModel()
    private val balancesViewModel: BalancesViewModel by sharedViewModel()

    private var _binding: FragmentChannelsListBinding? = null
    private val binding get() = _binding!!

    private var tracker: SelectionTracker<Long>? = null
    private var multiSelectAdapter: ChannelsMultiSelectAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentChannelsListBinding.inflate(inflater, container, false)

        Utils.logAnalyticsEvent(getString(R.string.visit_screen, getString(R.string.visit_link_account)), requireContext())
        initArguments()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupSelectedChannels()
        setupSimSupportedChannels()
        observeChannelsLoadedStatus()
    }

    private fun initArguments() = arguments?.let { IS_FORCE_RETURN = it.getBoolean(FORCE_RETURN_DATA, true) }

    private fun setupSelectedChannels() {
        val selectedChannelsListView = binding.selectedChannelsRecyclerView
        selectedChannelsListView.layoutManager = UIHelper.setMainLinearManagers(requireContext())

        channelsViewModel.selectedChannels.observe(viewLifecycleOwner) { channels ->
            if (!channels.isNullOrEmpty()) {
                updateCardVisibilities(true)
                selectedChannelsListView.adapter = ChannelsRecyclerViewAdapter(channels, this)
            } else {
                updateCardVisibilities(false)
            }
        }
    }

    private fun updateCardVisibilities(visible: Boolean) {
        binding.selectedChannelsCard.visibility = if (visible) VISIBLE else GONE
        binding.simSupportedChannelsCard.setBackButtonVisibility(if (visible) GONE else VISIBLE)
    }

    private fun observeChannelsLoadedStatus() {
        channelsViewModel.setHasChannelsLoaded()
        channelsViewModel.hasChannelsLoaded().observe(viewLifecycleOwner) { hasLoaded -> if (hasLoaded != null && !hasLoaded) showEmptySimChannelsDialog() }
    }

    private fun setupSimSupportedChannels() {
        val titleString = when (Utils.variant) {
            Constants.VARIANT_1 -> R.string.add_an_account
            Constants.VARIANT_2, Constants.VARIANT_3 -> R.string.add_accounts_to_stax
            else -> R.string.add_an_account //default title
        }

        binding.simSupportedChannelsCard.setTitle(getString(titleString))

        val simSupportedChannelsListView = binding.simSupportedChannelsRecyclerView
        simSupportedChannelsListView.layoutManager = UIHelper.setMainLinearManagers(requireContext())

        channelsViewModel.simChannels.observe(viewLifecycleOwner, {
            if (!it.isNullOrEmpty()) {
                val channels = Channel.sort(it, false)

                when (Utils.variant) {
                    Constants.VARIANT_1 -> initSingleSelectList(simSupportedChannelsListView, channels)
                    Constants.VARIANT_2, Constants.VARIANT_3 -> initMultiSelectList(simSupportedChannelsListView, channels)
                }
            }
        })
    }

    private fun initMultiSelectList(channelsRecycler: RecyclerView, channels: List<Channel>) {
        channelsRecycler.setHasFixedSize(true)
        multiSelectAdapter = ChannelsMultiSelectAdapter(channels)
        channelsRecycler.adapter = multiSelectAdapter

        tracker = SelectionTracker.Builder(
            "channelSelection", channelsRecycler,
            StableIdKeyProvider(channelsRecycler), ChannelLookup(channelsRecycler), StorageStrategy.createLongStorage()
        ).withSelectionPredicate(SelectionPredicates.createSelectAnything())
            .build()
        multiSelectAdapter!!.setTracker(tracker!!)

        binding.continueBtn.apply {
            visibility = VISIBLE
            setOnClickListener {
                fetchSelectedChannels(tracker!!, channels)
            }
        }
    }

    private fun fetchSelectedChannels(tracker: SelectionTracker<Long>, channels: List<Channel>) {
        if (tracker.selection.isEmpty) {
            binding.noAccountSelectedError.apply {
                visibility = VISIBLE
                setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_error, 0, 0, 0)
            }
        } else {
            binding.noAccountSelectedError.visibility = GONE

            val selectedChannels = mutableListOf<Channel>()
            tracker.selection.forEach {
                selectedChannels.add(channels[it.toInt()])
            }

            saveChannels(selectedChannels, true)
        }
    }

    private fun initSingleSelectList(channelsRecycler: RecyclerView, channels: List<Channel>) {
        channelsRecycler.adapter = ChannelsRecyclerViewAdapter(channels, this)
    }

    private fun showEmptySimChannelsDialog() {
        StaxDialog(requireActivity())
            .setDialogTitle(R.string.no_connecion)
            .setDialogMessage(R.string.empty_channels_internet_err)
            .setPosButton(R.string.btn_ok) { if (isAdded) requireActivity().onBackPressed() }
            .showIt()
    }

    private fun showCheckBalanceDialog(channel: Channel) {
        StaxDialog(requireActivity())
            .setDialogTitle(R.string.check_balance_title)
            .setDialogMessage(R.string.check_balance_desc)
            .setNegButton(R.string.later) { saveChannels(listOf(channel), false) }
            .setPosButton(R.string.check_balance_title) { saveChannels(listOf(channel), true) }
            .showIt()
    }

    private fun saveChannels(channels: List<Channel>, checkBalance: Boolean) {
        channelsViewModel.setChannelSelected(channels)
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
            showCheckBalanceDialog(channel)
        else
            goToChannelsDetailsScreen(channel)
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }

    class ChannelLookup(val recyclerView: RecyclerView) : ItemDetailsLookup<Long>() {
        override fun getItemDetails(e: MotionEvent): ItemDetails<Long>? {
            val view = recyclerView.findChildViewUnder(e.x, e.y)

            return if (view != null)
                (recyclerView.getChildViewHolder(view) as ChannelsViewHolder).getItemDetails()
            else
                null
        }
    }

    companion object {
        var IS_FORCE_RETURN = true
        const val FORCE_RETURN_DATA = "force_return_data"
    }
}