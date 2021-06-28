package com.hover.stax.channels

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.fragment.app.Fragment
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


class ChannelsListFragment : Fragment(), ChannelsRecyclerViewAdapter.SelectListener {

    private val channelsViewModel: ChannelsViewModel by viewModel()
    private val balancesViewModel: BalancesViewModel by sharedViewModel()

    private var _binding: FragmentChannelsListBinding? = null
    private val binding get() = _binding!!

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
                simSupportedChannelsListView.adapter = ChannelsRecyclerViewAdapter(Channel.sort(it, false), this)
            }
        })
    }

    private fun showEmptySimChannelsDialog() {
        StaxDialog(requireActivity())
            .setDialogTitle(R.string.no_connecion)
            .setDialogMessage(R.string.empty_channels_internet_err)
            .setPosButton(R.string.btn_ok) { if(isAdded) requireActivity().onBackPressed() }
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

    override fun clickedChannel(channel: Channel?) {
        if (IS_FORCE_RETURN || !channel!!.selected)
            showCheckBalanceDialog(channel!!)
        else
            goToChannelsDetailsScreen(channel)
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }

    companion object {
        var IS_FORCE_RETURN = true
        const val FORCE_RETURN_DATA = "force_return_data"
    }
}