package com.hover.stax.library

import android.os.Bundle
import com.hover.stax.R
import com.hover.stax.channels.Channel
import com.hover.stax.countries.CountryAdapter
import com.hover.stax.databinding.ActivityLibraryBinding
import com.hover.stax.navigation.AbstractNavigationActivity
import com.hover.stax.utils.UIHelper
import com.hover.stax.utils.Utils
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class LibraryActivity : AbstractNavigationActivity(), CountryAdapter.SelectListener {

    private val viewModel: LibraryViewModel by viewModel()
    private var binding: ActivityLibraryBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Utils.logAnalyticsEvent(getString(R.string.visit_screen, LibraryActivity::class.java.simpleName), this)

        binding = ActivityLibraryBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        binding!!.countryDropdown.setListener(this)
        binding!!.shortcodes.layoutManager = UIHelper.setMainLinearManagers(this)

        setUpNav()
        setObservers()
    }

    private fun setObservers() {
        with(viewModel) {
            filteredChannels.observe(this@LibraryActivity) { it?.let { updateList(it) } }
            country.observe(this@LibraryActivity) { it?.let { binding?.countryDropdown?.setDropdownValue(it) } }
            allChannels.observe(this@LibraryActivity) { it?.let {
                binding!!.countryDropdown.updateChoices(it, viewModel.country.value)
                updateList(it)
            } }
        }
    }

    private fun updateList(channels: List<Channel>) {
        if (!channels.isNullOrEmpty())
            binding!!.shortcodes.adapter = ChannelsAdapter(channels)
    }

    override fun countrySelect(countryCode: String) = viewModel.setCountry(countryCode)
}