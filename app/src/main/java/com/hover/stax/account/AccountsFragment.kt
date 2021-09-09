package com.hover.stax.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.hover.stax.channels.Channel
import com.hover.stax.channels.ChannelsRecyclerViewAdapter
import com.hover.stax.channels.ChannelsViewModel
import com.hover.stax.databinding.FragmentAccountsBinding

import com.hover.stax.utils.UIHelper
import org.koin.androidx.viewmodel.ext.android.viewModel

class AccountsFragment : Fragment(), ChannelsRecyclerViewAdapter.SelectListener, AccountsAdapter.SelectListener {

    private var _binding: FragmentAccountsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ChannelsViewModel by viewModel()
    private var selectAdapter: ChannelsRecyclerViewAdapter = ChannelsRecyclerViewAdapter(ArrayList(), null)

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

    //TODO run fetch account actions
    override fun clickedChannel(channel: Channel) {

    }

    override fun accountSelected(id: Int) {
        TODO("Run action on selected account")
    }
}