package com.hover.stax.transfers

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.hover.sdk.actions.HoverAction
import com.hover.stax.R
import com.hover.stax.actions.ActionSelect
import com.hover.stax.actions.ActionSelectViewModel
import com.hover.stax.contacts.ContactInput
import com.hover.stax.contacts.StaxContact
import com.hover.stax.databinding.FragmentTransferBinding
import com.hover.stax.home.MainActivity
import com.hover.stax.utils.AnalyticsUtil
import com.hover.stax.utils.Constants
import com.hover.stax.utils.UIHelper
import com.hover.stax.utils.Utils
import com.hover.stax.views.AbstractStatefulInput
import com.hover.stax.views.Stax2LineItem
import com.hover.stax.views.StaxTextInputLayout
import org.koin.androidx.viewmodel.ext.android.getSharedViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import timber.log.Timber


class TransferFragment : AbstractFormFragment(), ActionSelect.HighlightListener, NonStandardVariableAdapter.NonStandardVariableInputListener {

    private val actionSelectViewModel: ActionSelectViewModel by sharedViewModel()
    private lateinit var transferViewModel: TransferViewModel

    private val args by navArgs<TransferFragmentArgs>()

    private lateinit var amountInput: StaxTextInputLayout
    private lateinit var recipientInstitutionSelect: ActionSelect
    private lateinit var contactInput: ContactInput
    private lateinit var recipientValue: Stax2LineItem

    private var _binding: FragmentTransferBinding? = null
    private val binding get() = _binding!!

    private lateinit var nonStandardSummaryAdapter: NonStandardSummaryAdapter
    private var nonStandardVariableAdapter: NonStandardVariableAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        abstractFormViewModel = getSharedViewModel<TransferViewModel>()
        transferViewModel = abstractFormViewModel as TransferViewModel

        setTransactionType(args.transactionType)
        _binding = FragmentTransferBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onPause() {
        super.onPause()
        transferViewModel.setEditing(true)
    }

    override fun onResume() {
        super.onResume()

        amountInput.setHint(getString(R.string.transfer_amount_label))
        accountDropdown.setHint(getString(R.string.account_label))
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        transferViewModel.reset()
        init(binding.root)
        startObservers(binding.root)
        startListeners()
    }

    override fun init(root: View) {
        setTitle()

        amountInput = binding.editCard.amountInput
        contactInput = binding.editCard.contactSelect
        recipientInstitutionSelect = binding.editCard.actionSelect
        recipientValue = binding.summaryCard.recipientValue

        if (actionSelectViewModel.filteredActions.value != null)
            recipientInstitutionSelect.updateActions(actionSelectViewModel.filteredActions.value!!)

        super.init(root)

        accountDropdown.setFetchAccountListener(this)

        amountInput.apply {
            setText(transferViewModel.amount.value)
            requestFocus()
        }
    }

    private fun setTitle() {
        val titleRes = if (TransactionType.type == HoverAction.AIRTIME) R.string.cta_airtime else R.string.cta_transfer
        binding.editCard.transferCard.setTitle(getString(titleRes))
        binding.summaryCard.transferSummaryCard.setTitle(getString(titleRes))
    }

    private fun setTransactionType(txnType: String) {
        transferViewModel.setTransactionType(txnType)
        channelsViewModel.setType(txnType)
    }

    override fun startObservers(root: View) {
        super.startObservers(root)
        observeActionSelection()
        observeAccountList()
        observeActiveChannel()
        observeActions()
        observeAmount()
        observeNote()
        observeRecentContacts()
        observeNonStandardVariables()
        observeAutoFillToInstitution()
        with(transferViewModel) {
            contact.observe(viewLifecycleOwner) { recipientValue.setContact(it) }
            request.observe(viewLifecycleOwner) {
                AnalyticsUtil.logAnalyticsEvent(getString(R.string.loaded_request_link), requireContext())
                it?.let { transferViewModel.view(it) }
            }
        }
    }

    private fun observeActionSelection() {
        actionSelectViewModel.activeAction.observe(viewLifecycleOwner) {
            recipientInstitutionSelect.selectRecipientNetwork(it)
            setRecipientHint(it)
        }
    }

    private fun observeActiveChannel() {
        channelsViewModel.activeChannel.observe(viewLifecycleOwner) { channel ->
            channel?.let {
                transferViewModel.request.value?.let { request ->
                    transferViewModel.setRecipientSmartly(request.requester_number, it)
                }
                binding.summaryCard.accountValue.setTitle(it.toString())
            }
            recipientInstitutionSelect.visibility = if (channel != null) View.VISIBLE else View.GONE
        }
    }

    private fun observeActions() {
        channelsViewModel.channelActions.observe(viewLifecycleOwner) {
            actionSelectViewModel.setActions(it)
        }
        actionSelectViewModel.filteredActions.observe(viewLifecycleOwner) {
            recipientInstitutionSelect.updateActions(it)
        }
    }

    private fun observeAccountList() {
        channelsViewModel.accounts.observe(viewLifecycleOwner) {
            if (it.isEmpty())
                setDropdownTouchListener(TransferFragmentDirections.actionNavigationTransferToAccountsFragment())
        }
    }

    private fun observeAmount() {
        transferViewModel.amount.observe(viewLifecycleOwner) {
            it?.let {
                binding.summaryCard.amountValue.text = Utils.formatAmount(it)
            }
        }
    }
    private fun observeAutoFillToInstitution() {
        transferViewModel.completeAutoFilling.observe(viewLifecycleOwner) {
            it?.let {
                completeAutoFilling(it.first, it.second)
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
                contactInput.setRecent(it, requireActivity())
                transferViewModel.contact.value?.let { ct -> contactInput.setSelected(ct) }
            }
        }
    }

    private fun observeNonStandardVariables() {
        actionSelectViewModel.nonStandardVariables.observe(viewLifecycleOwner) { variables ->
            if (variables != null) {
                updateNonStandardForEntryList(variables)
                updateNonStandardForSummaryCard(variables)
            }
        }
    }

    private fun startListeners() {
        setAmountInputListener()
        setContactInputListener()

        recipientInstitutionSelect.setListener(this)
        fab.setOnClickListener { fabClicked() }

        binding.summaryCard.transferSummaryCard.setOnClickIcon { transferViewModel.setEditing(true) }
    }

    private fun setAmountInputListener() {
        amountInput.apply {
            addTextChangedListener(amountWatcher)
            setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus)
                    this.setState(
                        null,
                        if (transferViewModel.amountErrors() == null) AbstractStatefulInput.SUCCESS else AbstractStatefulInput.ERROR
                    )
                else
                    this.setState(null, AbstractStatefulInput.NONE)
            }
        }
    }

    private fun setContactInputListener() {
        contactInput.apply {
            setAutocompleteClickListener { view, _, position, _ ->
                val contact = view.getItemAtPosition(position) as StaxContact
                transferViewModel.setContact(contact)
            }
            addTextChangedListener(recipientWatcher)
            setChooseContactListener { contactPicker(Constants.GET_CONTACT, requireContext()) }
        }
    }

    private fun fabClicked() {
        if (validates()) {
            if (transferViewModel.isEditing.value == true) {
                transferViewModel.saveContact()
                transferViewModel.setEditing(false)
            } else {
                (requireActivity() as MainActivity).submit(
                    accountDropdown.highlightedAccount ?: channelsViewModel.activeAccount.value!!
                )
                findNavController().popBackStack()
            }
        } else UIHelper.flashMessage(requireActivity(), getString(R.string.toast_pleasefix))
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
            with(transferViewModel) {
                if (afterCount == 0) {
                    resetRecipient()
                    recipientValue.setContent("", "")
                } else
                    setRecipient(charSequence.toString())
            }
        }
    }

    private fun validates(): Boolean {
        val amountError = transferViewModel.amountErrors()
        amountInput.setState(amountError, if (amountError == null) AbstractStatefulInput.SUCCESS else AbstractStatefulInput.ERROR)

        val channelError = channelsViewModel.errorCheck()
        accountDropdown.setState(channelError, if (channelError == null) AbstractStatefulInput.SUCCESS else AbstractStatefulInput.ERROR)

        val actionError = actionSelectViewModel.errorCheck()
        recipientInstitutionSelect.setState(actionError, if (actionError == null) AbstractStatefulInput.SUCCESS else AbstractStatefulInput.ERROR)

        val recipientError = transferViewModel.recipientErrors(actionSelectViewModel.activeAction.value)
        contactInput.setState(recipientError, if (recipientError == null) AbstractStatefulInput.SUCCESS else AbstractStatefulInput.ERROR)

        val noNonStandardVarError = nonStandardVariableAdapter?.validates() ?: true

        channelsViewModel.activeAccount.value?.let {
            if (!channelsViewModel.isValidAccount()) {
                accountDropdown.setState(getString(R.string.incomplete_account_setup_header), AbstractStatefulInput.ERROR)
                fetchAccounts(it)
                return false
            }
        }

        return channelError == null && actionError == null && amountError == null && recipientError == null && noNonStandardVarError
    }

    override fun onContactSelected(requestCode: Int, contact: StaxContact) {
        transferViewModel.setContact(contact)
        contactInput.setSelected(contact)
    }

    override fun highlightAction(action: HoverAction?) {
        action?.let { actionSelectViewModel.setActiveAction(it) }
    }

    private fun updateNonStandardForEntryList(variables: LinkedHashMap<String, String>) {
        val recyclerView = binding.editCard.nonStandardVariableRecyclerView
        nonStandardVariableAdapter = NonStandardVariableAdapter(variables, this, recyclerView)
        recyclerView.layoutManager = UIHelper.setMainLinearManagers(requireContext())
        recyclerView.adapter = nonStandardVariableAdapter
    }

    private fun updateNonStandardForSummaryCard(variables: LinkedHashMap<String, String>) {
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
            recipientValue.setContent(getString(R.string.self_choice), "")
        } else {
            transferViewModel.forceUpdateContactUI()
            contactInput.setHint(
                if (action.requiredParams.contains(HoverAction.ACCOUNT_KEY))
                    getString(R.string.recipientacct_label)
                else
                    getString(R.string.recipientphone_label)
            )
        }
    }

    private fun completeAutoFilling(institutionId: Int?, showEditing: Boolean) {
        institutionId?.let {channelsViewModel.setChannelFromInstitutionId(institutionId)}
        transferViewModel.contact.value?.let { contactInput.setText(it.shortName(), false) }
        amountInput.setText(transferViewModel.amount.value)
        transferViewModel.setEditing(showEditing)
        accountDropdown.setState(getString(R.string.channel_request_fieldinfo, institutionId.toString()), AbstractStatefulInput.INFO)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (dialog != null && dialog!!.isShowing) dialog!!.dismiss()
        _binding = null
    }

    override fun nonStandardVarUpdate(key: String, value: String) {
        actionSelectViewModel.updateNonStandardVariables(key, value)
    }
}