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
package com.hover.stax.merchants

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.fragment.app.activityViewModels
import com.hover.sdk.actions.HoverAction
import com.hover.stax.R
import com.hover.stax.database.models.Merchant
import com.hover.stax.database.models.StaxContact
import com.hover.stax.databinding.FragmentMerchantBinding
import com.hover.stax.hover.HoverSession
import com.hover.stax.hover.TransactionContract
import com.hover.stax.transfers.AbstractFormFragment
import com.hover.stax.utils.Utils
import com.hover.stax.views.AbstractStatefulInput
import timber.log.Timber

class MerchantFragment : AbstractFormFragment() {

    private var _binding: FragmentMerchantBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: MerchantViewModel

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        val abstractFormViewModel: MerchantViewModel by activityViewModels()
        viewModel = abstractFormViewModel as MerchantViewModel
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMerchantBinding.inflate(inflater, container, false)
        com.hover.stax.core.AnalyticsUtil.logAnalyticsEvent(
            getString(
                R.string.visit_screen,
                getString(R.string.visit_merchant)
            ),
            requireActivity()
        )
        accountsViewModel.setType(HoverAction.MERCHANT)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init(binding.root)
        startObservers(binding.root)
        startListeners()
    }

    override fun startObservers(root: View) {
        super.startObservers(root)
        observeActiveAccount()
        observeActions()
        observeActionSelection()
        observeSelectedMerchant()
        observeAmount()
        observeRecentMerchants()
    }

    private fun observeActiveAccount() {
        accountsViewModel.activeAccount.observe(viewLifecycleOwner) { account ->
            account?.let { binding.summaryCard.accountValue.setTitle(it.toString()) }
            val err = accountsViewModel.errorCheck()
            payWithDropdown.setState(
                err,
                if (err == null) AbstractStatefulInput.SUCCESS else AbstractStatefulInput.ERROR
            )
        }
    }

    private fun observeActions() {
        accountsViewModel.institutionActions.observe(viewLifecycleOwner) {
            actionSelectViewModel.setActions(it)
        }

        actionSelectViewModel.filteredActions.observe(viewLifecycleOwner) {
            it?.let { if (it.isNotEmpty()) actionSelectViewModel.setActiveAction(it.first()) }
        }
    }

    private fun observeActionSelection() {
        actionSelectViewModel.activeAction.observe(viewLifecycleOwner) {
            it?.let { Timber.i("Updated action to ${it.public_id}") }
        }
    }

    private fun observeRecentMerchants() {
        viewModel.recentMerchants.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) {
                binding.editCard.merchantSelect.setRecent(it, requireActivity())
                viewModel.merchant.value?.let { m ->
                    binding.editCard.merchantSelect.setSelected(m)
                }
            }
        }
    }

    private fun observeSelectedMerchant() {
        viewModel.merchant.observe(viewLifecycleOwner) {
            it?.let {
                binding.summaryCard.recipientValue.setContent(it.businessName, it.tillNo)
            }
        }
    }

    private fun observeAmount() {
        viewModel.amount.observe(viewLifecycleOwner) {
            it?.let {
                if (binding.editCard.amountInput.text.isEmpty() && it.isNotEmpty())
                    binding.editCard.amountInput.setText(it)
                binding.summaryCard.amountValue.text = Utils.formatAmount(it)
            }
        }
    }

    private fun startListeners() {
        setAmountInputListener()
        setMerchantInputListener()
    }

    private fun setAmountInputListener() {
        binding.editCard.amountInput.apply {
            addTextChangedListener(amountWatcher)
            setOnFocusChangeListener { _, hasFocus ->
                setInputState(hasFocus, this, viewModel.amountErrors())
            }
        }
    }

    private fun setMerchantInputListener() {
        binding.editCard.merchantSelect.apply {
            setSelected(viewModel.merchant.value)
            setAutocompleteClickListener { view, _, position, _ ->
                val merchant = view.getItemAtPosition(position) as Merchant
                viewModel.setMerchant(merchant)
            }
            addTextChangedListener(recipientWatcher)
        }
    }

    override fun onFinishForm() {
        viewModel.saveMerchant()
        viewModel.setEditing(false)
    }

    override fun onSubmitForm() {
        val hsb = generateSessionBuilder()
        callHover(pay, hsb)
    }

    private val pay = registerForActivityResult(TransactionContract()) { data: Intent? ->
        goToDeets(data)
    }

    override fun onContactSelected(contact: StaxContact) {
        TODO("Not yet implemented")
    }

    private fun generateSessionBuilder(): HoverSession.Builder {
        return HoverSession.Builder(
            actionSelectViewModel.activeAction.value!!,
            payWithDropdown.getHighlightedAccount() ?: accountsViewModel.activeAccount.value!!,
            viewModel.wrapExtras(), requireActivity()
        )
    }

    private val amountWatcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
        override fun afterTextChanged(editable: Editable) {}
        override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            viewModel.setAmount(charSequence.toString().replace(",".toRegex(), ""))
        }
    }

    private val recipientWatcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
        override fun afterTextChanged(editable: Editable) {}
        override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, afterCount: Int) {
            viewModel.setMerchant(
                charSequence.toString(),
                payWithDropdown.getHighlightedAccount(),
                actionSelectViewModel.activeAction.value
            )
        }
    }

    override fun validates(): Boolean {
        val accountError = accountsViewModel.errorCheck()
        payWithDropdown.setState(
            accountError,
            if (accountError == null) AbstractStatefulInput.SUCCESS else AbstractStatefulInput.ERROR
        )

        val amountError = viewModel.amountErrors()
        binding.editCard.amountInput.setState(
            amountError,
            if (amountError == null) AbstractStatefulInput.SUCCESS else AbstractStatefulInput.ERROR
        )

        val actionError = actionSelectViewModel.errorCheck()

        val recipientError = viewModel.recipientErrors()
        binding.editCard.merchantSelect.setState(
            recipientError,
            if (recipientError == null) AbstractStatefulInput.SUCCESS else AbstractStatefulInput.ERROR
        )

        return accountError == null && actionError == null && amountError == null && recipientError == null
    }

    fun onMerchantSelected(merchant: Merchant) {
        viewModel.setMerchant(merchant)
        binding.editCard.merchantSelect.setSelected(merchant)
    }
}