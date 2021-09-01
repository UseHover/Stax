package com.hover.stax.library

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.hover.stax.R
import com.hover.stax.channels.Channel
import com.hover.stax.countries.CountryAdapter
import com.hover.stax.databinding.ActivityLibraryBinding
import com.hover.stax.navigation.AbstractNavigationActivity
import com.hover.stax.utils.UIHelper
import com.hover.stax.utils.Utils
import org.koin.androidx.viewmodel.ext.android.viewModel

class LibraryActivity : AbstractNavigationActivity(), ChannelsAdapter.DialListener, CountryAdapter.SelectListener {

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

    private fun setObservers(){
        with(viewModel){
            allChannels.observe(this@LibraryActivity) {
                it?.let {
                    binding!!.countryDropdown.updateChoices(it)
                    filterChannels(CountryAdapter.codeRepresentingAllCountries())
                }
            }

            filteredChannels.observe(this@LibraryActivity) { it?.let { updateList(it) } }
        }
    }

    private fun updateList(channels: List<Channel>) {
        if(!channels.isNullOrEmpty())
            binding!!.shortcodes.adapter = ChannelsAdapter(channels, this)
    }

    override fun countrySelect(countryCode: String) = viewModel.filterChannels(countryCode)

    override fun dial(shortCode: String) {
        Utils.logAnalyticsEvent(getString(R.string.clicked_dial_shortcode), this)

        val dialIntent = Intent(Intent.ACTION_CALL, Uri.parse("tel:".plus(shortCode.replace("#", Uri.encode("#"))))).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(dialIntent)
    }
}