package com.hover.stax.library

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.hover.stax.R
import com.hover.stax.channels.Channel
import com.hover.stax.countries.CountryAdapter
import com.hover.stax.databinding.FragmentLibraryBinding
import com.hover.stax.utils.AnalyticsUtil
import com.hover.stax.utils.UIHelper
import org.koin.androidx.viewmodel.ext.android.viewModel

class LibraryFragment : Fragment(), CountryAdapter.SelectListener {

    private val viewModel: LibraryViewModel by viewModel()
    private var _binding: FragmentLibraryBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLibraryBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        AnalyticsUtil.logAnalyticsEvent(getString(R.string.visit_screen, LibraryFragment::class.java.simpleName), requireActivity())

        binding.countryDropdown.setListener(this)
        binding.shortcodes.layoutManager = UIHelper.setMainLinearManagers(requireActivity())

        setObservers()
    }

    private fun setObservers() {
        with(viewModel) {
            filteredChannels.observe(viewLifecycleOwner) { it?.let { updateList(it) } }

            country.observe(viewLifecycleOwner) { it?.let { binding.countryDropdown.setDropdownValue(it) } }

            allChannels.observe(viewLifecycleOwner) {
                it?.let {
                    binding.countryDropdown.updateChoices(it, viewModel.country.value)
                    updateList(it)
                }
            }
        }
    }

    private fun updateList(channels: List<Channel>) {
        if (!channels.isNullOrEmpty())
            binding.shortcodes.adapter = ChannelsAdapter(channels)
    }

    override fun countrySelect(countryCode: String) {
        viewModel.setCountry(countryCode)
    }
}