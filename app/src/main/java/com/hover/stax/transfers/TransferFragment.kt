package com.hover.stax.transfers

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.annotation.CallSuper
import androidx.core.content.ContextCompat.getColor
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.hover.sdk.actions.HoverAction
import com.hover.stax.R
import com.hover.stax.actions.ActionSelect
import com.hover.stax.addChannels.ChannelsViewModel
import com.hover.stax.bonus.BonusViewModel
import com.hover.stax.contacts.StaxContact
import com.hover.stax.databinding.FragmentTransferBinding
import com.hover.stax.hover.AbstractHoverCallerActivity
import com.hover.stax.hover.FEE_REQUEST
import com.hover.stax.utils.AnalyticsUtil
import com.hover.stax.utils.UIHelper
import com.hover.stax.utils.Utils
import com.hover.stax.utils.collectLifecycleFlow
import com.hover.stax.views.AbstractStatefulInput
import org.koin.androidx.viewmodel.ext.android.getSharedViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel


class TransferFragment : AbstractFormFragment(), ActionSelect.HighlightListener, NonStandardVariableAdapter.NonStandardVariableInputListener {

    private val bonusViewModel: BonusViewModel by sharedViewModel()
    private lateinit var transferViewModel: TransferViewModel
    private val channelsViewModel: ChannelsViewModel by viewModel()

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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        setTransactionType(args.transactionType)

        _binding = FragmentTransferBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init(binding.root)

        bonusViewModel.getBonusList()

        startObservers(binding.root)
        startListeners()
        fillFromArgs()
    }

    private fun fillFromArgs() {
        args.accountId?.let { accountsViewModel.setActiveAccount(Integer.parseInt(it)) }
        args.amount?.let { binding.editCard.amountInput.setText(it) }
        args.contactId?.let { transferViewModel.setContact(it) }
        args.channelId?.let { channelsViewModel.validateAccounts(Integer.parseInt(it)) }
    }

    override fun init(root: View) {
        super.init(root)

        setTitle()

        if (actionSelectViewModel.filteredActions.value != null)
            binding.editCard.actionSelect.updateActions(actionSelectViewModel.filteredActions.value!!)

        setUpFee()
    }

    private fun setTitle() {
        val titleRes = if (accountsViewModel.getActionType() == HoverAction.AIRTIME) R.string.cta_airtime else R.string.cta_transfer
        binding.editCard.root.setTitle(getString(titleRes))
        binding.summaryCard.root.setTitle(getString(titleRes))
    }

    private fun setUpFee() {
        binding.summaryCard.feeValue.text = getString(R.string.check_fee)
        binding.summaryCard.feeValue.textSize = 13.0F
        binding.summaryCard.feeValue.setTextColor(getColor(requireContext(), R.color.stax_state_blue))
        binding.summaryCard.feeValue.setOnClickListener { checkFee() }
        showCheckFeeOption()
    }

    private fun checkFee() {
        callHover(FEE_REQUEST)
    }

    private fun showCheckFeeOption() {
        binding.summaryCard.feeValue.visibility = if (actionSelectViewModel.activeAction.value?.requiredParams?.contains("fee") == true) View.VISIBLE else ViewGroup.GONE
    }

    private fun setTransactionType(txnType: String) {
        accountsViewModel.setType(txnType)
    }

    override fun startObservers(root: View) {
        super.startObservers(root)
        observeAccountList()
        observeActiveAccount()
        observeActions()
        observeActionSelection()
        observeSelectedContact()
        observeAmount()
        observeNote()
        observeRecentContacts()
        observeNonStandardVariables()
        observeAccountsEvent()
    }

    private fun observeActiveAccount() {
        accountsViewModel.activeAccount.observe(viewLifecycleOwner) { account ->
            account?.let { binding.summaryCard.accountValue.setTitle(it.toString()) }
            val err = accountsViewModel.errorCheck()
            payWithDropdown.setState(err, if (err == null) AbstractStatefulInput.SUCCESS else AbstractStatefulInput.ERROR)
            binding.editCard.actionSelect.visibility = if (account != null) View.VISIBLE else View.GONE
        }
    }

    private fun observeActionSelection() {
        actionSelectViewModel.activeAction.observe(viewLifecycleOwner) {
            it?.let {
                binding.editCard.actionSelect.selectRecipientNetwork(it)
                setRecipientHint(it)
            }
        }
    }

    private fun observeActions() {
        accountsViewModel.channelActions.observe(viewLifecycleOwner) {
            actionSelectViewModel.setActions(it)
            showBonusBanner(it.firstOrNull())
        }

        actionSelectViewModel.filteredActions.observe(viewLifecycleOwner) {
            binding.editCard.actionSelect.updateActions(it)
        }
    }

    private fun observeSelectedContact() {
        transferViewModel.contact.observe(viewLifecycleOwner) {
            it?.let {
                binding.summaryCard.recipientValue.setContact(it)
            }
        }
    }

    private fun observeAccountList() = collectLifecycleFlow(accountsViewModel.accountList) {
        if (it.accounts.isEmpty())
            setDropdownTouchListener(TransferFragmentDirections.actionNavigationTransferToAccountsFragment())
    }

    private fun observeAmount() {
        transferViewModel.amount.observe(viewLifecycleOwner) {
            it?.let {
                if (binding.editCard.amountInput.text.isEmpty() && it.isNotEmpty())
                    binding.editCard.amountInput.setText(it)
                binding.summaryCard.amountValue.text = Utils.formatAmount(it)
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

    private fun observeAccountsEvent() {
        collectLifecycleFlow(channelsViewModel.accountEventFlow) {
            val bonus = bonusViewModel.bonusList.value.bonuses.firstOrNull() ?: return@collectLifecycleFlow
            accountsViewModel.setActiveAccountFromChannel(bonus.userChannel)
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
                setInputState(hasFocus, this, transferViewModel.amountErrors())
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
        callHover(0)
        findNavController().popBackStack()
    }

    private fun callHover(requestCode: Int) {
        (requireActivity() as AbstractHoverCallerActivity).runSession(
            payWithDropdown.getHighlightedAccount() ?: accountsViewModel.activeAccount.value!!,
            actionSelectViewModel.activeAction.value!!, getExtras(), requestCode
        )
    }

    private fun getExtras(): HashMap<String, String> {
        val extras = transferViewModel.wrapExtras()
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

        val amountError = transferViewModel.amountErrors()
        binding.editCard.amountInput.setState(amountError, if (amountError == null) AbstractStatefulInput.SUCCESS else AbstractStatefulInput.ERROR)

        val actionError = actionSelectViewModel.errorCheck()
        binding.editCard.actionSelect.setState(actionError, if (actionError == null) AbstractStatefulInput.SUCCESS else AbstractStatefulInput.ERROR)

        val recipientError = transferViewModel.recipientErrors(actionSelectViewModel.activeAction.value)
        binding.editCard.contactSelect.setState(recipientError, if (recipientError == null) AbstractStatefulInput.SUCCESS else AbstractStatefulInput.ERROR)

        val noNonStandardVarError = nonStandardVariableAdapter?.validates() ?: true

        return accountError == null && actionError == null && amountError == null && recipientError == null && noNonStandardVarError
    }

    override fun onContactSelected(contact: StaxContact) {
        transferViewModel.setContact(contact)
        binding.editCard.contactSelect.setSelected(contact)
    }

    override fun highlightAction(action: HoverAction?) {
        action?.let { actionSelectViewModel.setActiveAction(it) }
    }

    private fun updateNonStandardInputs(variables: LinkedHashMap<String, String>) {
        val recyclerView = binding.editCard.nonStandardVariableRecyclerView
        nonStandardVariableAdapter = NonStandardVariableAdapter(variables, this, recyclerView)
        recyclerView.layoutManager = UIHelper.setMainLinearManagers(requireContext())
        recyclerView.adapter = nonStandardVariableAdapter
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
                if (action.requiredParams.contains(HoverAction.ACCOUNT_KEY))
                    getString(R.string.recipientacct_label)
                else
                    getString(R.string.recipientphone_label)
            )
        }
    }

    private fun showBonusBanner(activeAction: HoverAction?) {
        if (args.transactionType == HoverAction.AIRTIME) {
            val bonus = bonusViewModel.bonusList.value.bonuses.firstOrNull() ?: return

            with(binding.bonusLayout) {
                cardBonus.visibility = View.VISIBLE
                learnMore.movementMethod = LinkMovementMethod.getInstance()

                if (activeAction?.channel_id == bonus.purchaseChannel) {
                    title.text = getString(R.string.congratulations)
                    message.text = getString(R.string.valid_account_bonus_msg)
                    cta.visibility = View.GONE
                } else {
                    title.text = getString(R.string.get_extra_airtime)
                    message.text = getString(R.string.invalid_account_bonus_msg)
                    cta.apply {
                        visibility = View.VISIBLE
                        text = getString(R.string.top_up_with_mpesa)

                        setOnClickListener {
                            AnalyticsUtil.logAnalyticsEvent(getString(R.string.clicked_bonus_airtime_banner), requireActivity())
                            channelsViewModel.validateAccounts(bonus.userChannel)
                        }
                    }
                }
            }
        }
    }

    override fun showEdit(isEditing: Boolean) {
        super.showEdit(isEditing)

        if (!isEditing)
            binding.bonusLayout.cardBonus.visibility = View.GONE
        else
            showBonusBanner(actionSelectViewModel.activeAction.value)
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