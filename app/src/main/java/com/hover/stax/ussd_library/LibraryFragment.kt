package com.hover.stax.ussd_library

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import com.hover.stax.R
import com.hover.stax.addChannels.ChannelsViewModel
import com.hover.stax.channels.Channel
import com.hover.stax.countries.CountryAdapter
import com.hover.stax.databinding.FragmentLibraryBinding
import com.hover.stax.presentation.home.components.TopBar
import com.hover.stax.transactions.TransactionHistoryFragmentDirections
import com.hover.stax.ui.theme.StaxTheme
import com.hover.stax.utils.AnalyticsUtil
import com.hover.stax.utils.NavUtil
import com.hover.stax.utils.UIHelper
import com.hover.stax.utils.network.NetworkMonitor
import com.hover.stax.views.RequestServiceDialog
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class LibraryFragment : Fragment(), CountryAdapter.SelectListener, LibraryChannelsAdapter.FavoriteClickInterface {

    private val viewModel: ChannelsViewModel by viewModel()
    private var _binding: FragmentLibraryBinding? = null
    private val binding get() = _binding!!

    private val libraryAdapter = LibraryChannelsAdapter(this)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLibraryBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        AnalyticsUtil.logAnalyticsEvent(getString(R.string.visit_screen, LibraryFragment::class.java.simpleName), requireActivity())

        initToolbar()

        binding.countryCard.showProgressIndicator()
        binding.countryDropdown.setListener(this)

        binding.shortcodes.apply {
            layoutManager = UIHelper.setMainLinearManagers(requireActivity())
            adapter = libraryAdapter
        }

        setupEmptyState()
        setSearchInputWatcher()
        setObservers()
    }

    private fun initToolbar() {
        binding.toolbar.setContent {
            StaxTheme { Toolbar() }
        }
    }

    @Composable
    private fun Toolbar() {
        val hasNetwork by NetworkMonitor.StateLiveData.get().observeAsState(initial = false)
        TopBar(title = R.string.library_cardhead, isInternetConnected = hasNetwork,
            { navigate(TransactionHistoryFragmentDirections.actionGlobalNavigationSettings()) },
            { navigate(TransactionHistoryFragmentDirections.actionGlobalRewardsFragment()) })
    }

    private fun navigate(navDirections: NavDirections) = NavUtil.navigate(findNavController(), navDirections)

    private fun setObservers() {
        with(viewModel) {
            channelCountryList.observe(viewLifecycleOwner) { it?.let { binding.countryDropdown.updateChoices(it, countryChoice.value) } }
            sims.observe(viewLifecycleOwner) { Timber.e("${this@LibraryFragment.javaClass.simpleName} Loaded ${it?.size} sims") }
            simCountryList.observe(viewLifecycleOwner) { Timber.e("${this@LibraryFragment.javaClass.simpleName} Loaded ${it?.size} hnis") }
            filteredChannels.observe(viewLifecycleOwner) { it?.let { updateList(it) } }
            countryChoice.observe(viewLifecycleOwner) { it?.let { binding.countryDropdown.setDropdownValue(it) } }
        }
    }

    private fun setupEmptyState() {
        binding.emptyState.root.visibility = View.GONE
        binding.emptyState.informUs.setOnClickListener {
            RequestServiceDialog(requireActivity()).showIt()
        }
    }

    private fun setSearchInputWatcher() {
        val searchInputWatcher: TextWatcher = object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun afterTextChanged(editable: Editable) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                viewModel.updateSearch(charSequence.toString())
            }
        }
        binding.searchInput.addTextChangedListener(searchInputWatcher)
    }

    private fun updateList(channels: List<Channel>) {
        binding.countryCard.hideProgressIndicator()

        if (channels.isNotEmpty()) showList(channels)
        else showEmptyState()
    }

    private fun showList(channels: List<Channel>) {
        libraryAdapter.submitList(channels)
        binding.emptyState.root.visibility = View.GONE
        binding.shortcodesParent.visibility = VISIBLE
    }

    private fun showEmptyState() {
        val content = resources.getString(R.string.no_accounts_found_desc, viewModel.filterQuery.value ?: getString(R.string.empty_channel_placeholder))
        binding.emptyState.noAccountFoundDesc.apply {
            text = HtmlCompat.fromHtml(content, HtmlCompat.FROM_HTML_MODE_LEGACY)
            movementMethod = LinkMovementMethod.getInstance()
        }
        binding.emptyState.root.visibility = VISIBLE
        binding.shortcodesParent.visibility = View.GONE
    }

    override fun countrySelect(countryCode: String) {
        viewModel.updateCountry(countryCode)
    }

    override fun onFavoriteIconClicked(channel: Channel) {
        viewModel.updateChannel(channel)
    }
}