package com.hover.stax.channels

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.hover.stax.databinding.FragmentChannelListBinding
import com.hover.stax.utils.UIHelper
import org.koin.androidx.viewmodel.ext.android.viewModel

class ChannelListFragment : Fragment(), ChannelsRecyclerViewAdapter.SelectListener {

    private var _binding: FragmentChannelListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ChannelsViewModel by viewModel()
    private var selectAdapter: ChannelsRecyclerViewAdapter = ChannelsRecyclerViewAdapter(ArrayList(), null)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentChannelListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.accountsRV.apply {
            layoutManager = UIHelper.setMainLinearManagers(requireActivity())
            adapter = selectAdapter
        }

        with(viewModel) {
            allChannels.observe(viewLifecycleOwner) { selectAdapter.updateList(it) }
            simChannels.observe(viewLifecycleOwner) { selectAdapter.updateList(it) }
        }
    }

    //TODO run fetch account actions
    override fun clickedChannel(channel: Channel) {

    }
}