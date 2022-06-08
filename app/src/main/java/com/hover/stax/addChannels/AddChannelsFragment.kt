package com.hover.stax.addChannels

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import com.hover.stax.R
import com.hover.stax.accounts.Account
import com.hover.stax.accounts.AccountsAdapter
import com.hover.stax.bonus.BonusViewModel
import com.hover.stax.channels.*
import com.hover.stax.countries.CountryAdapter
import com.hover.stax.databinding.FragmentAddChannelsBinding
import com.hover.stax.utils.AnalyticsUtil
import com.hover.stax.utils.UIHelper
import com.hover.stax.utils.Utils
import com.hover.stax.views.RequestServiceDialog
import com.hover.stax.views.StaxDialog
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

const val CHANNELS_REFRESHED = "has_refreshed_channels"

class AddChannelsFragment : Fragment(), ChannelsAdapter.SelectListener, CountryAdapter.SelectListener {

    private val channelsViewModel: ChannelsViewModel by viewModel()
    private val bonusViewModel: BonusViewModel by sharedViewModel()

    private var _binding: FragmentAddChannelsBinding? = null
    private val binding get() = _binding!!

    private val selectAdapter: ChannelsAdapter = ChannelsAdapter(this)
    private var tracker: SelectionTracker<Long>? = null

    private var dialog: StaxDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        refreshChannelsIfRequired()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAddChannelsBinding.inflate(inflater, container, false)

        AnalyticsUtil.logAnalyticsEvent(getString(R.string.visit_screen, getString(R.string.visit_link_account)), requireContext())
        initArguments()

        return binding.root
    }

    private fun initArguments() = arguments?.let { IS_FORCE_RETURN = it.getBoolean(FORCE_RETURN_DATA, true) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.channelsListCard.apply {
            showProgressIndicator()
            setTitle(getString(R.string.add_accounts_to_stax))
        }

        fillUpChannelLists()
        setupEmptyState()

        setUpMultiselect()
        setUpCountryChoice()
        setSearchInputWatcher()

        bonusViewModel.getBonusList()
        startObservers()

        setFabListener()
    }

    private fun startObservers() = with(channelsViewModel) {
        channelCountryList.observe(viewLifecycleOwner) { it?.let { binding.countryDropdown.updateChoices(it, countryChoice.value) } }
        sims.observe(viewLifecycleOwner) { Timber.v("Loaded ${it?.size} sims") }
        simCountryList.observe(viewLifecycleOwner) { Timber.v("Loaded ${it?.size} hnis") }
        accounts.observe(viewLifecycleOwner) { onSelectedLoaded(it) }
        filteredChannels.observe(viewLifecycleOwner) { loadFilteredChannels(it) }
        countryChoice.observe(viewLifecycleOwner) { it?.let { binding.countryDropdown.setDropdownValue(it) } }
    }

    private fun fillUpChannelLists() {
        binding.selectedList.apply {
            layoutManager = UIHelper.setMainLinearManagers(requireContext())
        }

        binding.channelsList.apply {
            layoutManager = UIHelper.setMainLinearManagers(requireContext())
            adapter = selectAdapter
        }
    }

    private fun setupEmptyState() {
        binding.emptyState.root.visibility = GONE
        binding.emptyState.informUs.setOnClickListener {
            RequestServiceDialog(requireActivity()).showIt()
        }
    }

    private fun setFabListener() {
        binding.continueBtn.setOnClickListener { saveAndGoHome(tracker!!) }
    }

    private fun setUpCountryChoice() {
        binding.countryDropdown.setListener(this)
    }

    private fun setSearchInputWatcher() {
        val searchInputWatcher: TextWatcher = object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun afterTextChanged(editable: Editable) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                channelsViewModel.updateSearch(charSequence.toString())
            }
        }
        binding.searchInput.addTextChangedListener(searchInputWatcher)
    }

    override fun countrySelect(countryCode: String) {
        channelsViewModel.updateCountry(countryCode)
    }

    private fun setUpMultiselect() {
        tracker = SelectionTracker.Builder(
            "channelSelection", binding.channelsList,
            ChannelKeyProvider(selectAdapter),
            ChannelLookup(binding.channelsList),
            StorageStrategy.createLongStorage()
        ).withSelectionPredicate(SelectionPredicates.createSelectAnything())
            .build()
        selectAdapter.setTracker(tracker!!)
    }

    private fun onSelectedLoaded(accounts: List<Account>) {
        binding.channelsListCard.hideProgressIndicator()

        showSelected(accounts.isNotEmpty())
        if (accounts.isNotEmpty())
            binding.selectedList.adapter = AccountsAdapter(accounts)
    }

    private fun showSelected(visible: Boolean) {
        binding.selectedChannelsCard.visibility = if (visible) VISIBLE else GONE
        binding.channelsListCard.setBackButtonVisibility(if (visible) GONE else VISIBLE)
    }

    private fun loadFilteredChannels(channels: List<Channel>) {
        binding.channelsListCard.hideProgressIndicator()

        if (channels.isNotEmpty()) {
            updateAdapter(channels.filterNot { it.selected })
            binding.emptyState.root.visibility = GONE
            binding.channelsList.visibility = VISIBLE
            binding.errorText.visibility = GONE
        } else showEmptyState()
    }

    private fun showEmptyState() {
        val content = resources.getString(R.string.no_accounts_found_desc, channelsViewModel.filterQuery.value!!)
        binding.emptyState.noAccountFoundDesc.apply {
            text = HtmlCompat.fromHtml(content, HtmlCompat.FROM_HTML_MODE_LEGACY)
            movementMethod = LinkMovementMethod.getInstance()
        }

        binding.emptyState.root.visibility = VISIBLE
        binding.channelsList.visibility = GONE
        binding.errorText.visibility = GONE
    }

    private fun updateAdapter(channels: List<Channel>) {
        selectAdapter.submitList(channels)
    }

    private fun setError(message: Int) {
        binding.errorText.apply {
            visibility = VISIBLE
            text = getString(message)
        }
    }

    private fun saveAndGoHome(tracker: SelectionTracker<Long>) {
        if (tracker.selection.isEmpty)
            setError(R.string.channels_error_noselect)
        else {
            binding.errorText.visibility = GONE
            aggregateSelectedChannels(tracker)
            findNavController().popBackStack()
        }
    }

    private fun aggregateSelectedChannels(tracker: SelectionTracker<Long>) {
        val selectedChannels = mutableListOf<Channel>()
        tracker.selection.forEach { selection ->
            selectedChannels.addAll(selectAdapter.currentList.filter { it.id.toLong() == selection })
        }

        channelsViewModel.createAccounts(selectedChannels)
    }

    override fun clickedChannel(channel: Channel) {
        AddChannelsFragmentDirections.actionNavigationLinkAccountToNavigationHome()
    }

    //channels will be loaded only once after install then deferred to weekly.
    private fun refreshChannelsIfRequired() {
        if (!Utils.getBoolean(CHANNELS_REFRESHED, requireActivity())) {
            Timber.i("Reloading channels")
            val wm = WorkManager.getInstance(requireContext())
            wm.beginUniqueWork(
                UpdateChannelsWorker.CHANNELS_WORK_ID, ExistingWorkPolicy.KEEP,
                UpdateChannelsWorker.makeWork()
            ).enqueue()

            Utils.saveBoolean(CHANNELS_REFRESHED, true, requireActivity())
            return
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        dialog?.let { if (it.isShowing) it.dismiss() }
        _binding = null
    }

    companion object {
        var IS_FORCE_RETURN = true
        const val FORCE_RETURN_DATA = "force_return_data"
    }
}