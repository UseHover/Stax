package com.hover.stax.library

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.hover.stax.R
import com.hover.stax.channels.Channel
import com.hover.stax.countries.CountryAdapter
import com.hover.stax.databinding.FragmentLibraryBinding
import com.hover.stax.permissions.PermissionUtils
import com.hover.stax.utils.UIHelper
import com.hover.stax.utils.Utils
import org.koin.androidx.viewmodel.ext.android.viewModel

class LibraryFragment : Fragment(), ChannelsAdapter.DialListener, CountryAdapter.SelectListener {

    private val viewModel: LibraryViewModel by viewModel()
    private var _binding: FragmentLibraryBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentLibraryBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Utils.logAnalyticsEvent(getString(R.string.visit_screen, LibraryFragment::class.java.simpleName), requireActivity())

        binding.countryDropdown.setListener(this)
        binding.shortcodes.layoutManager = UIHelper.setMainLinearManagers(requireActivity())

        setObservers()
    }

    private fun setObservers() {
        with(viewModel) {
            allChannels.observe(viewLifecycleOwner) {
                it?.let {
                    binding.countryDropdown.updateChoices(it)
                    filterChannels(CountryAdapter.codeRepresentingAllCountries())
                }
            }

            filteredChannels.observe(viewLifecycleOwner) { it?.let { updateList(it) } }
        }
    }

    private fun updateList(channels: List<Channel>) {
        if (!channels.isNullOrEmpty())
            binding.shortcodes.adapter = ChannelsAdapter(channels, this)
    }

    override fun countrySelect(countryCode: String) = viewModel.filterChannels(countryCode)

    override fun dial(shortCode: String) {
        Utils.logAnalyticsEvent(getString(R.string.clicked_dial_shortcode), requireActivity())

        val dialIntent = Intent(Intent.ACTION_CALL, Uri.parse("tel:".plus(shortCode.replace("#", Uri.encode("#"))))).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        if (PermissionUtils.has(arrayOf(Manifest.permission.CALL_PHONE), requireActivity()))
            startActivity(dialIntent)
        else
            UIHelper.flashMessage(requireActivity(), getString(R.string.enable_call_permission))
    }
}