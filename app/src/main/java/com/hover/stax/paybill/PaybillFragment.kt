package com.hover.stax.paybill

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.hover.sdk.actions.HoverAction
import com.hover.stax.channels.Channel
import com.hover.stax.channels.ChannelsViewModel
import com.hover.stax.databinding.FragmentPaybillBinding
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class PaybillFragment : Fragment() {

    private var _binding: FragmentPaybillBinding? = null
    private val binding get() = _binding!!

    private val channelsViewModel: ChannelsViewModel by viewModel()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPaybillBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        channelsViewModel.setType(HoverAction.C2B)

        initSaveButton()
        startObservers()
    }

    private fun initSaveButton() = binding.saveBillLayout.saveBill.setOnCheckedChangeListener { _, isChecked ->
        binding.saveBillLayout.saveBillCard.visibility = if (isChecked) View.VISIBLE else View.GONE
    }

    private fun startObservers() {
        with(channelsViewModel) {
            binding.billDetailsLayout.accountDropdown.apply {
                setListener(this@with)
                setObservers(this@with, viewLifecycleOwner)
            }

            setupActionDropdownObservers(this, viewLifecycleOwner)
        }
    }

    private fun setupActionDropdownObservers(viewModel: ChannelsViewModel, lifecycleOwner: LifecycleOwner) {

        val activeChannelObserver = object : Observer<Channel> {
            override fun onChanged(t: Channel?) {
                Timber.i("Got new active channel: $t ${t?.countryAlpha2}")
            }
        }

        val actionsObserver = object : Observer<List<HoverAction>> {
            override fun onChanged(t: List<HoverAction>?) {
                Timber.i("Got new actions: %s", t?.size)
            }
        }

        viewModel.activeChannel.observe(lifecycleOwner, activeChannelObserver)
        viewModel.channelActions.observe(lifecycleOwner, actionsObserver)
    }

}
