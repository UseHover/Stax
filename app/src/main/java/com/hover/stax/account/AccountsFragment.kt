package com.hover.stax.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.hover.sdk.actions.HoverAction
import com.hover.stax.channels.Channel
import com.hover.stax.channels.ChannelsRecyclerViewAdapter
import com.hover.stax.channels.ChannelsViewModel
import com.hover.stax.databinding.FragmentAccountsBinding
import com.hover.stax.requests.RequestActivity
import com.hover.stax.transfers.TransferActivity
import com.hover.stax.utils.UIHelper
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class AccountsFragment : Fragment(), ChannelsRecyclerViewAdapter.SelectListener, AccountsAdapter.SelectListener {

    private var _binding: FragmentAccountsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ChannelsViewModel by viewModel()
    private var selectAdapter: ChannelsRecyclerViewAdapter = ChannelsRecyclerViewAdapter(ArrayList(), this)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAccountsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.accountsRV.apply {
            layoutManager = UIHelper.setMainLinearManagers(requireActivity())
            adapter = selectAdapter
        }

        binding.accountListCard.setOnClickIcon { findNavController().popBackStack() }

        with(viewModel) {
            allChannels.observe(viewLifecycleOwner) { selectAdapter.updateList(it) }
            simChannels.observe(viewLifecycleOwner) { selectAdapter.updateList(it) }
        }
    }

    //TODO set channels selected for channels that need accounts fetched.
    override fun clickedChannel(channel: Channel) {
        lifecycleScope.launch {
            val fetchAction = viewModel.getFetchAccountAction(channel.id)

            if (fetchAction != null) {
                fetchAccounts(fetchAction, channel)
            } else {
                viewModel.setChannelsSelected(listOf(channel))
                viewModel.createAccounts(listOf(channel))
                findNavController().popBackStack()
            }
        }
    }

    private fun fetchAccounts(action: HoverAction, channel: Channel) {
        (activity as? TransferActivity)?.makeCall(action, channel)
                ?: (activity as? RequestActivity)?.makeCall(action, channel)
    }

    override fun accountSelected(account: Account) {
    }
}