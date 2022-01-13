package com.hover.stax.transfers

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.hover.sdk.actions.HoverAction
import com.hover.stax.R
import com.hover.stax.actions.ActionSelect
import com.hover.stax.actions.ActionSelectViewModel
import com.hover.stax.contacts.ContactInput
import com.hover.stax.contacts.StaxContact
import com.hover.stax.databinding.FragmentTransferBinding
import com.hover.stax.home.MainActivity
import com.hover.stax.requests.Request
import com.hover.stax.utils.AnalyticsUtil
import com.hover.stax.utils.Constants
import com.hover.stax.utils.UIHelper
import com.hover.stax.utils.Utils
import com.hover.stax.views.AbstractStatefulInput
import com.hover.stax.views.Stax2LineItem
import com.hover.stax.views.StaxTextInputLayout
import org.koin.androidx.viewmodel.ext.android.getSharedViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel


class TransferFragment : AbstractFormFragment(), ActionSelect.HighlightListener, NonTemplateVariableAdapter.NonTemplateVariableInputListener {

    private val actionSelectViewModel: ActionSelectViewModel by sharedViewModel()
    private lateinit var transferViewModel: TransferViewModel

    private lateinit var amountInput: StaxTextInputLayout
    private lateinit var actionSelect: ActionSelect
    private lateinit var contactInput: ContactInput
    private lateinit var recipientValue: Stax2LineItem

    private var _binding: FragmentTransferBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        abstractFormViewModel = getSharedViewModel<TransferViewModel>()
        transferViewModel = abstractFormViewModel as TransferViewModel

        setTransactionType(arguments!!.getString(Constants.TRANSACTION_TYPE)!!)

        _binding = FragmentTransferBinding.inflate(inflater, container, false)

        transferViewModel.reset()
        init(binding.root)
        startObservers(binding.root)
        startListeners()

        return binding.root
    }

    override fun init(root: View) {
        setTitle()

        amountInput = binding.editCard.amountInput
        contactInput = binding.editCard.contactSelect
        actionSelect = binding.editCard.actionSelect
        recipientValue = binding.summaryCard.recipientValue

        amountInput.apply {
            setText(transferViewModel.amount.value)
            requestFocus()
        }

        super.init(root)
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

    private fun setTransactionType(txnType: String) {
        transferViewModel.setTransactionType(txnType)
        channelsViewModel.setType(txnType)
    }

    private fun setTitle() {
        val titleRes = if (TransactionType.type == HoverAction.AIRTIME) R.string.cta_airtime else R.string.cta_transfer
        binding.editCard.transferCard.setTitle(getString(titleRes))
        binding.summaryCard.transferSummaryCard.setTitle(getString(titleRes))
    }

    override fun startObservers(root: View) {
        super.startObservers(root)

        actionSelectViewModel.activeAction.observe(viewLifecycleOwner, {
            binding.summaryCard.accountValue.setSubtitle(it.getNetworkSubtitle(requireContext()))
            actionSelect.selectRecipientNetwork(it)
            setRecipientHint(it)
        })

        with(channelsViewModel) {
            activeChannel.observe(viewLifecycleOwner, { channel ->
                transferViewModel.request.value?.let { request ->
                    transferViewModel.setRecipientSmartly(request, channel)
                }
                actionSelect.visibility = if (channel != null) View.VISIBLE else View.GONE
                channel?.let { binding.summaryCard.accountValue.setTitle(channel.toString()) }
            })

            channelActions.observe(viewLifecycleOwner, {
                actionSelectViewModel.setActions(it)
                actionSelect.updateActions(it)
            })

            accounts.observe(viewLifecycleOwner) {
                if (it.isEmpty())
                    setDropdownTouchListener(R.id.action_navigation_transfer_to_accountsFragment)
            }

            with(transferViewModel) {
                amount.observe(viewLifecycleOwner, {
                    it?.let {
                        binding.summaryCard.amountValue.text = Utils.formatAmount(it)
                    }
                })

                note.observe(viewLifecycleOwner, {
                    binding.summaryCard.noteRow.visibility = if (it.isNullOrEmpty()) View.GONE else View.VISIBLE
                    binding.summaryCard.noteValue.text = it
                })

                contact.observe(viewLifecycleOwner, { recipientValue.setContact(it) })

                recentContacts.observe(viewLifecycleOwner, {
                    if (!it.isNullOrEmpty()) {
                        contactInput.setRecent(it, requireActivity())
                        transferViewModel.contact.value?.let { ct -> contactInput.setSelected(ct) }
                    }
                })

                request.observe(viewLifecycleOwner, { it?.let { load(it) } })
            }
        }
    }


    private fun startListeners() {
        amountInput.apply {
            addTextChangedListener(amountWatcher)
            setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus)
                    amountInput.setState(
                            null,
                            if (transferViewModel.amountErrors() == null) AbstractStatefulInput.SUCCESS else AbstractStatefulInput.ERROR
                    )
                else
                    amountInput.setState(null, AbstractStatefulInput.NONE)
            }
        }

        contactInput.apply {
            setAutocompleteClickListener { view, _, position, _ ->
                val contact = view.getItemAtPosition(position) as StaxContact
                transferViewModel.setContact(contact)
            }
            addTextChangedListener(recipientWatcher)
            setChooseContactListener { contactPicker(Constants.GET_CONTACT, requireContext()) }
        }

        actionSelect.setListener(this)
        fab.setOnClickListener { fabClicked() }

        binding.summaryCard.transferSummaryCard.setOnClickIcon { transferViewModel.setEditing(true) }
    }

    private fun fabClicked() {
        if (validates()) {
            if (transferViewModel.isEditing.value == true) {
                transferViewModel.saveContact()
                transferViewModel.setEditing(false)
            } else {
                (requireActivity() as MainActivity).submit(accountDropdown.highlightedAccount
                        ?: channelsViewModel.activeAccount.value!!)
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
        actionSelect.setState(actionError, if (actionError == null) AbstractStatefulInput.SUCCESS else AbstractStatefulInput.ERROR)

        val recipientError = transferViewModel.recipientErrors(actionSelectViewModel.activeAction.value)
        contactInput.setState(recipientError, if (recipientError == null) AbstractStatefulInput.SUCCESS else AbstractStatefulInput.ERROR)

        return channelError == null && actionError == null && amountError == null && recipientError == null
    }

    override fun onContactSelected(requestCode: Int, contact: StaxContact) {
        transferViewModel.setContact(contact)
        contactInput.setSelected(contact)
    }

    override fun highlightAction(action: HoverAction?) {
        action?.let {
            actionSelectViewModel.setActiveAction(it)
            val tempList = mutableListOf<String>()
            tempList.add("Country")
            tempList.add("City")
            updateNonTemplateVariableStatus(tempList)
        }
    }

    private fun updateNonTemplateVariableStatus( variableKeys: List<String>) {
        val recyclerView = binding.editCard.nonTemplateVariableRecyclerView
        if(variableKeys.isEmpty()) recyclerView.visibility = View.GONE
        else {
            recyclerView.visibility = View.VISIBLE
            val nonTemplateVariableAdapter = NonTemplateVariableAdapter(variableKeys, this)
            recyclerView.layoutManager = UIHelper.setMainLinearManagers(requireContext())
            recyclerView.adapter = nonTemplateVariableAdapter
        }
    }

    private fun setRecipientHint(action: HoverAction) {
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

    private fun load(r: Request) {
        channelsViewModel.activeChannel.value?.let {
            transferViewModel.setRecipientSmartly(r, it)
        }

        channelsViewModel.setChannelFromRequest(r)
        amountInput.setText(r.amount)
        contactInput.setText(r.requester_number, false)

        transferViewModel.setEditing(r.amount.isNullOrEmpty())
        accountDropdown.setState(getString(R.string.channel_request_fieldinfo, r.requester_institution_id.toString()), AbstractStatefulInput.INFO)

        AnalyticsUtil.logAnalyticsEvent(getString(R.string.loaded_request_link), requireContext())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun nonTemplateVariableInputUpdated(key: String, value: String) {
        transferViewModel.updateNonTemplateVariables(key, value)
    }
}