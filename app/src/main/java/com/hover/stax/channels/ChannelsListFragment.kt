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
import com.hover.stax.utils.UIHelper
import com.hover.stax.utils.Utils
import com.hover.stax.views.StaxDialog
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber


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
        val simSupportedChannelsListView = binding.simSupportedChannelsRecyclerView
        simSupportedChannelsListView.layoutManager = UIHelper.setMainLinearManagers(requireContext())

        channelsViewModel.simChannels.observe(viewLifecycleOwner, {
            if (!it.isNullOrEmpty()) {
                //TODO check variant here
                initMultiSelectList(binding.simSupportedChannelsRecyclerView, it)
            }
        })
    }

    private fun initMultiSelectList(channelsRecycler: RecyclerView, channels: List<Channel>) {
        channelsRecycler.setHasFixedSize(true)
        multiSelectAdapter = ChannelsMultiSelectAdapter(Channel.sort(channels, false))
        channelsRecycler.adapter = multiSelectAdapter

        tracker = SelectionTracker.Builder(
            "channelSelection", channelsRecycler,
            StableIdKeyProvider(channelsRecycler), ChannelLookup(channelsRecycler), StorageStrategy.createLongStorage()
        ).withSelectionPredicate(SelectionPredicates.createSelectAnything())
            .build()
        multiSelectAdapter!!.setTracker(tracker!!)

        tracker?.addObserver(object : SelectionTracker.SelectionObserver<Long>() {
            override fun onSelectionChanged() {
                super.onSelectionChanged()

                Timber.e("Selected ${tracker?.selection?.size()}")
            }
        })
    }

    private fun initSingleSelectList(channelsRecycler: RecyclerView, channels: List<Channel>) {
        channelsRecycler.adapter = ChannelsRecyclerViewAdapter(Channel.sort(channels, false), this)
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
            .setNegButton(R.string.later) { saveChannel(channel, false) }
            .setPosButton(R.string.check_balance_title) { saveChannel(channel, true) }
            .showIt()
    }

    private fun saveChannel(channel: Channel, checkBalance: Boolean) {
        channelsViewModel.setChannelSelected(channel)
        requireActivity().onBackPressed()

        if (checkBalance) balancesViewModel.actions.observe(viewLifecycleOwner, { balancesViewModel.setRunning(channel.id) })
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