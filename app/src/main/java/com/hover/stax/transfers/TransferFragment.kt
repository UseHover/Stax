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
package com.hover.stax.transfers

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.CallSuper
import androidx.core.content.ContextCompat.getColor
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.hover.sdk.actions.HoverAction
import com.hover.stax.R
import com.hover.stax.actions.ActionSelect
import com.hover.stax.contacts.StaxContact
import com.hover.stax.databinding.FragmentTransferBinding
import com.hover.stax.databinding.InputItemBinding
import com.hover.stax.hover.HoverSession
import com.hover.stax.hover.TransactionContract
import com.hover.stax.utils.*
import com.hover.stax.views.AbstractStatefulInput
import com.hover.stax.views.StaxDialog
import com.hover.stax.views.StaxTextInput
import org.koin.androidx.viewmodel.ext.android.getSharedViewModel
import timber.log.Timber

class TransferFragment : AbstractFormFragment(), ActionSelect.HighlightListener {

    private lateinit var transferViewModel: TransferViewModel

    private val args by navArgs<TransferFragmentArgs>()

    private var _binding: FragmentTransferBinding? = null
    private val binding get() = _binding!!

    private var nonStandardVariableAdapter: NonStandardVariableAdapter? = null
    private lateinit var nonStandardSummaryAdapter: NonStandardSummaryAdapter

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        abstractFormViewModel = getSharedViewModel<TransferViewModel>()
        transferViewModel = abstractFormViewModel as TransferViewModel
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setTransactionType(args.transactionType)

        _binding = FragmentTransferBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init(binding.root)

        startObservers(binding.root)
        startListeners()
        fillFromArgs()
    }

    private fun fillFromArgs() {
        args.accountId?.let { accountsViewModel.setActiveAccount(Integer.parseInt(it)) }
        args.amount?.let { binding.editCard.amountInput.setText(it) }
        args.contactId?.let { transferViewModel.setContact(it) }
        args.institutionId?.let { accountsViewModel.payWith(Integer.parseInt(it)) }
    }

    override fun init(root: View) {
        super.init(root)

        setTitle()

        if (actionSelectViewModel.filteredActions.value != null)
            binding.editCard.actionSelect.updateActions(actionSelectViewModel.filteredActions.value!!)
    }

    private fun setTitle() {
        val titleRes = if (accountsViewModel.getActionType() == HoverAction.AIRTIME) R.string.cta_airtime else R.string.cta_transfer
        binding.editCard.root.setTitle(getString(titleRes))
        binding.summaryCard.root.setTitle(getString(titleRes))
    }

    private fun setTransactionType(txnType: String) {
        accountsViewModel.setType(txnType)
    }

    override fun startObservers(root: View) {
        super.startObservers(root)

        observeActiveAccount()
        observeActions()
        observeActionSelection()
        observeSelectedContact()
        observeAmount()
        observeNote()
        observeRecentContacts()
        observeNonStandardVariables()
    }

    private fun observeActiveAccount() {
        accountsViewModel.activeAccount.observe(viewLifecycleOwner) { account ->
            account?.let {
                binding.summaryCard.accountValue.setTitle(it.toString())
                binding.summaryCard.feeLabel.text = getString(R.string.transfer_fee_label_who, it.toString())
            }

            val err = accountsViewModel.errorCheck()
            payWithDropdown.setState(err, if (err == null) AbstractStatefulInput.SUCCESS else AbstractStatefulInput.ERROR)
            binding.editCard.actionSelect.visibility = if (account != null) View.VISIBLE else View.GONE
        }
    }

    private fun observeActions() {
        accountsViewModel.institutionActions.observe(viewLifecycleOwner) {
            actionSelectViewModel.setActions(it)
        }

        accountsViewModel.bonusActions.observe(viewLifecycleOwner) {
            updateBonusBanner(actionSelectViewModel.activeAction.value, it)
        }

        actionSelectViewModel.filteredActions.observe(viewLifecycleOwner) {
            binding.editCard.actionSelect.updateActions(it)
        }
    }

    private fun observeActionSelection() {
        actionSelectViewModel.activeAction.observe(viewLifecycleOwner) {
            it?.let {
                binding.editCard.actionSelect.selectRecipientNetwork(it)
                setRecipientHint(it)
                showLookupOptions(it)
                updateBonusBanner(it, accountsViewModel.bonusActions.value)
            }
        }
    }

    private fun showLookupOptions(action: HoverAction) {
        Timber.e("action out params: %s", action.output_params)
        showVerifyRecipient(transferViewModel.contact.value, action)
        binding.summaryCard.verifyRecipientBtn.setOnClickListener { checkRecipient() }
        binding.summaryCard.feeRow.visibility = if (action.output_params?.opt("fee") != null) View.VISIBLE else ViewGroup.GONE
        setFeeState(null)
    }

    private fun setFeeState(amount: String?) {
        binding.summaryCard.feeValue.text = amount ?: getString(R.string.check_fee)
        binding.summaryCard.feeValue.setTextColor(getColor(requireContext(), if (amount == null) R.color.stax_state_blue else R.color.offWhite))
        binding.summaryCard.feeValue.setOnClickListener { if (amount == null) checkFee() else null }
    }

    private fun observeSelectedContact() {
        transferViewModel.contact.observe(viewLifecycleOwner) {
            it?.let {
                binding.summaryCard.recipientValue.setContact(it)
                showVerifyRecipient(it, actionSelectViewModel.activeAction.value)
            }
        }
    }

    private fun showVerifyRecipient(contact: StaxContact?, action: HoverAction?) {
        binding.summaryCard.verifyRecipientRow.visibility =
            if (contact?.hasName() == false && action?.output_params?.opt("recipientName") != null) View.VISIBLE
            else View.GONE
    }

    private fun observeAmount() {
        transferViewModel.amount.observe(viewLifecycleOwner) {
            it?.let {
                if (binding.editCard.amountInput.text.isEmpty() && it.isNotEmpty())
                    binding.editCard.amountInput.setText(it)
                binding.summaryCard.amountValue.text = Utils.formatAmount(it)
                setFeeState(null)
            }
        }
    }

    private fun observeNote() {
        transferViewModel.note.observe(viewLifecycleOwner) {
            binding.summaryCard.noteRow.visibility = if (it.isNullOrEmpty()) View.GONE else View.VISIBLE
            binding.summaryCard.noteValue.text = it
        }
    }

    private fun observeRecentContacts() {
        transferViewModel.recentContacts.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) {
                binding.editCard.contactSelect.setRecent(it, requireActivity())
                transferViewModel.contact.value?.let { ct ->
                    binding.editCard.contactSelect.setSelected(ct)
                }
            }
        }
    }

    private fun observeNonStandardVariables() {
        actionSelectViewModel.nonStandardVariables.observe(viewLifecycleOwner) { variables ->
            if (variables != null) {
                updateNonStandardInputs(variables)
                updateNonStandardSummary(variables)
            }
        }
    }

    private fun startListeners() {
        setAmountInputListener()
        setContactInputListener()
        binding.editCard.actionSelect.setListener(this)
    }

    private fun setAmountInputListener() {
        binding.editCard.amountInput.apply {
            addTextChangedListener(amountWatcher)
            setOnFocusChangeListener { _, hasFocus ->
                setInputState(hasFocus, this, transferViewModel.amountErrors(actionSelectViewModel.activeAction.value))
            }
        }
    }

    private fun setContactInputListener() {
        binding.editCard.contactSelect.apply {
            setSelected(transferViewModel.contact.value)
            setAutocompleteClickListener { view, _, position, _ ->
                val contact = view.getItemAtPosition(position) as StaxContact
                transferViewModel.setContact(contact)
            }
            addTextChangedListener(recipientWatcher)
            setChooseContactListener { startContactPicker(requireActivity()) }
        }
    }

    override fun onFinishForm() {
        transferViewModel.saveContact()
        transferViewModel.setEditing(false)
    }

    override fun onSubmitForm() {
        callHover(transfer, generateSessionBuilder())
        findNavController().popBackStack()
    }

    private fun checkRecipient() {
        val hsb = generateSessionBuilder()
        hsb.message(getString(R.string.check_recipient_for, transferViewModel.contact.value?.accountNumber))
        hsb.stopAt("recipientName")
        callHover(retrieveData, hsb)
    }

    private fun checkFee() {
        val hsb = generateSessionBuilder()
        hsb.message(getString(R.string.check_transfer_fee_for, transferViewModel.amount.value, hsb.action.from_institution_name))
        hsb.stopAt("fee")
        callHover(retrieveData, hsb)
    }

    private fun generateSessionBuilder(): HoverSession.Builder {
        return HoverSession.Builder(actionSelectViewModel.activeAction.value!!,
            payWithDropdown.getHighlightedAccount() ?: accountsViewModel.activeAccount.value!!,
            getExtras(), requireActivity())
    }

    private val transfer = registerForActivityResult(TransactionContract()) { data: Intent? ->
        goToDeets(data)
    }

    private val retrieveData = registerForActivityResult(TransactionContract()) { data: Intent? ->
        processForFee(data)
        processForRecipient(data)
        transferViewModel.setEditing(false)
    }

    private fun processForFee(data: Intent?) {
        val fee = processFor(data, "fee", "No fee information found")
        setFeeState(fee)
    }

    private fun processForRecipient(data: Intent?) {
        transferViewModel.contact.value?.let {
            it.updateNames(data)
            val dialog = StaxDialog(layoutInflater)
                .setDialogMessage(getString(R.string.check_recipient_result, it.accountNumber, it.name))
                .setPosButton(R.string.btn_ok) { }
            dialog.showIt()
            transferViewModel.saveContact()
        }
    }

    private fun processFor(data: Intent?, key: String, defaultVal: String): String? {
        if (data != null && data.hasExtra(com.hover.sdk.transactions.TransactionContract.COLUMN_PARSED_VARIABLES)) {
            val parsedVariables =
                data.getSerializableExtra(com.hover.sdk.transactions.TransactionContract.COLUMN_PARSED_VARIABLES) as HashMap<String, String>
            Timber.e("parsed vars is non-null: %s", parsedVariables)
            if (parsedVariables.containsKey(key) && parsedVariables[key] != null) {
                return parsedVariables[key]!!
            }
        }
        return defaultVal
    }

    private fun getExtras(): HashMap<String, String> {
        val extras = transferViewModel.wrapExtras(actionSelectViewModel.activeAction.value!!)
        extras.putAll(actionSelectViewModel.wrapExtras())
        return extras
    }

    private val amountWatcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
        override fun afterTextChanged(editable: Editable) {}
        override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            transferViewModel.setAmount(charSequence.toString().replace(",".toRegex(), ""))
        }
    }

    private val recipientWatcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
        override fun afterTextChanged(editable: Editable) {}
        override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, afterCount: Int) {
            transferViewModel.setRecipientNumber(charSequence.toString())
        }
    }

    override fun validates(): Boolean {
        val accountError = accountsViewModel.errorCheck()
        payWithDropdown.setState(accountError, if (accountError == null) AbstractStatefulInput.SUCCESS else AbstractStatefulInput.ERROR)

        val amountError = transferViewModel.amountErrors(actionSelectViewModel.activeAction.value)
        binding.editCard.amountInput.setState(amountError, if (amountError == null) AbstractStatefulInput.SUCCESS else AbstractStatefulInput.ERROR)

        val actionError = actionSelectViewModel.errorCheck()
        binding.editCard.actionSelect.setState(actionError, if (actionError == null) AbstractStatefulInput.SUCCESS else AbstractStatefulInput.ERROR)

        val recipientError = transferViewModel.recipientErrors(actionSelectViewModel.activeAction.value)
        binding.editCard.contactSelect.setState(recipientError, if (recipientError == null) AbstractStatefulInput.SUCCESS else AbstractStatefulInput.ERROR)

        val nonstandardErrors = validateNonStandardVars()

        return accountError == null && actionError == null && amountError == null && recipientError == null && nonstandardErrors
    }

    override fun onContactSelected(contact: StaxContact) {
        transferViewModel.setContact(contact)
        binding.editCard.contactSelect.setSelected(contact)
    }

    override fun highlightAction(action: HoverAction?) {
        action?.let { actionSelectViewModel.setActiveAction(it) }
    }

    private fun updateNonStandardInputs(variables: LinkedHashMap<String, String>) {
        val listView = binding.editCard.nonStandardVariables
        var index = 0
        for ((k, v) in variables) {
            if (listView.findViewWithTag<StaxTextInput>(k) == null)
                createVariableInput(k, v, listView)
            index++
        }
    }

    private fun createVariableInput(key: String, value: String, parent: ViewGroup) {
        val binding = InputItemBinding.inflate(LayoutInflater.from(context), parent, true)
        val inputTextWatcher: TextWatcher = object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun afterTextChanged(editable: Editable) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                nonStandardVarUpdate(key, charSequence.toString())
            }
        }

        binding.variableInput.addTextChangedListener(inputTextWatcher)
        binding.variableInput.setHint(key.splitCamelCase())
        binding.variableInput.tag = key
        binding.variableInput.setText(value)
    }

    fun nonStandardVarUpdate(key: String, value: String) {
        actionSelectViewModel.updateNonStandardVariables(key, value)
    }

    private fun validateNonStandardVars(): Boolean {
        if (!actionSelectViewModel.nonStandardVariables.value.isNullOrEmpty()) {
            for ((k, v) in actionSelectViewModel.nonStandardVariables.value!!) {
                if (v.isNullOrEmpty()) {
                    binding.editCard.nonStandardVariables.findViewWithTag<StaxTextInput>(k)
                        .setState(getString(R.string.enterValue_non_template_error, k.lowercase()), AbstractStatefulInput.ERROR)
                    return false
                } else
                    binding.editCard.nonStandardVariables.findViewWithTag<StaxTextInput>(k)
                        .setState(null, AbstractStatefulInput.SUCCESS)
            }
        }
        return true
    }

    private fun updateNonStandardSummary(variables: LinkedHashMap<String, String>) {
        val recyclerView = binding.summaryCard.nonStandardSummaryRecycler
        recyclerView.layoutManager = UIHelper.setMainLinearManagers(requireContext())
        nonStandardSummaryAdapter = NonStandardSummaryAdapter(variables)
        recyclerView.adapter = nonStandardSummaryAdapter
    }

    private fun setRecipientHint(action: HoverAction) {
        binding.summaryCard.accountValue.setSubtitle(action.getNetworkSubtitle(requireContext()))
        editCard?.findViewById<LinearLayout>(R.id.recipient_entry)?.visibility = if (action.requiresRecipient()) View.VISIBLE else View.GONE
        binding.summaryCard.recipientRow.visibility = if (action.requiresRecipient()) View.VISIBLE else View.GONE

        if (!action.requiresRecipient()) {
            binding.summaryCard.recipientValue.setContent(getString(R.string.self_choice), "")
        } else {
            binding.editCard.contactSelect.setHint(
                if (action.required_params.has(HoverAction.ACCOUNT_KEY))
                    getString(R.string.recipientacct_label)
                else
                    getString(R.string.recipientphone_label)
            )
        }
    }

    private fun updateBonusBanner(selected: HoverAction?, actions: List<HoverAction>?) {
        if (args.transactionType == HoverAction.AIRTIME) {

            with(binding.bonusLayout) {
                learnMore.movementMethod = LinkMovementMethod.getInstance()

                if (selected != null && selected.bonus_percent > 0) {
                    cardBonus.visibility = View.VISIBLE
                    title.text = getString(R.string.congratulations)
                    message.text = getString(R.string.you_are_getting_extra_airtime, selected.bonus_percent)
                    cta.visibility = View.GONE
                } else if (actions?.any { a -> a.bonus_percent > 0 } != true) {
                    cardBonus.visibility = View.GONE
                } else if (transferViewModel.isEditing.value == true) {
                    cardBonus.visibility = View.VISIBLE
                    val bonus = actions.first { a -> a.bonus_percent > 0 }
                    title.text = getString(R.string.get_extra_airtime)
                    message.text = bonus.bonus_message
                    cta.apply {
                        visibility = View.VISIBLE
                        text = getString(R.string.top_up_with, bonus.from_institution_name)

                        setOnClickListener {
                            AnalyticsUtil.logAnalyticsEvent(getString(R.string.clicked_bonus_airtime_banner), requireActivity())
                            accountsViewModel.payWith(bonus.from_institution_id)
                        }
                    }
                } else { cardBonus.visibility = View.GONE }
            }
        }
    }

    override fun showEdit(isEditing: Boolean) {
        super.showEdit(isEditing)
        updateBonusBanner(actionSelectViewModel.activeAction.value, accountsViewModel.bonusActions.value)
    }

    override fun onDestroyView() {
        super.onDestroyView()

        if (dialog != null && dialog!!.isShowing) dialog!!.dismiss()
        _binding = null
    }
}