/*
 * Copyright 2022 Stax
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import com.hover.stax.R
import com.hover.stax.database.models.Channel
import com.hover.stax.channels.UpdateChannelsWorker
import com.hover.stax.countries.CountryAdapter
import com.hover.stax.databinding.FragmentAddChannelsBinding
import com.hover.stax.domain.model.Account
import com.hover.stax.utils.AnalyticsUtil
import com.hover.stax.utils.UIHelper
import com.hover.stax.views.RequestServiceDialog
import com.hover.stax.views.StaxDialog
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import timber.log.Timber

const val CHANNELS_REFRESHED = "has_refreshed_channels"

class AddChannelsFragment : Fragment(), ChannelsAdapter.SelectListener, CountryAdapter.SelectListener {

    private val channelsViewModel: ChannelsViewModel by sharedViewModel()

    private var _binding: FragmentAddChannelsBinding? = null
    private val binding get() = _binding!!

    private val selectAdapter: ChannelsAdapter = ChannelsAdapter(this)
    private var tracker: SelectionTracker<Long>? = null

    private var dialog: StaxDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        refreshChannels()
    }

    private fun refreshChannels() {
        val wm = WorkManager.getInstance(requireContext())
        wm.beginUniqueWork(
            UpdateChannelsWorker.CHANNELS_WORK_ID, ExistingWorkPolicy.KEEP,
            UpdateChannelsWorker.makeWork()
        ).enqueue()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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

        startObservers()
        setFabListener()
    }

    private fun startObservers() = with(channelsViewModel) {
        val channelsObserver =
            Observer<List<Channel>> { t ->
                t?.let {
                    loadFilteredChannels(it)
                }
            }

        channelCountryList.observe(viewLifecycleOwner) { it?.let { binding.countryDropdown.updateChoices(it, countryChoice.value) } }
        sims.observe(viewLifecycleOwner) { Timber.v("${this@AddChannelsFragment.javaClass.simpleName} Loaded ${it?.size} sims") }
        simCountryList.observe(viewLifecycleOwner) {
            Timber.v("${this@AddChannelsFragment.javaClass.simpleName} Loaded ${it?.size} hnis")
        }
        accounts.observe(viewLifecycleOwner) { onSelectedLoaded(it) }
        countryChoice.observe(viewLifecycleOwner) { it?.let { binding.countryDropdown.setDropdownValue(it) } }
        filteredChannels.observe(viewLifecycleOwner, channelsObserver)
    }

    private fun fillUpChannelLists() {
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
        channelsViewModel.updateSearch(null)
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
//        if (accounts.isNotEmpty())
//            binding.selectedList.adapter = AccountsAdapter(accounts)
    }

    private fun showSelected(visible: Boolean) {
//        binding.selectedChannelsCard.visibility = if (visible) VISIBLE else GONE
        binding.channelsListCard.setBackButtonVisibility(if (visible) GONE else VISIBLE)
    }

    private fun loadFilteredChannels(channels: List<Channel>) {
        binding.channelsListCard.hideProgressIndicator()

        if (channels.isNotEmpty()) {
            updateAdapter(channels)
            binding.emptyState.root.visibility = GONE
            binding.channelsList.visibility = VISIBLE
            binding.errorText.visibility = GONE
        } else showEmptyState()
    }

    private fun showEmptyState() {
        val content = resources.getString(R.string.no_accounts_found_desc, channelsViewModel.filterQuery.value ?: getString(R.string.empty_channel_placeholder))
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