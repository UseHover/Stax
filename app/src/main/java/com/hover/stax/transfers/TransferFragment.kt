package com.hover.stax.transfers

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.hover.sdk.actions.HoverAction
import com.hover.stax.R
import com.hover.stax.actions.ActionSelect
import com.hover.stax.actions.ActionSelectViewModel
import com.hover.stax.contacts.ContactInput
import com.hover.stax.contacts.StaxContact
import com.hover.stax.databinding.FragmentTransferBinding
import com.hover.stax.requests.Request
import com.hover.stax.utils.Constants
import com.hover.stax.utils.UIHelper
import com.hover.stax.utils.Utils
import com.hover.stax.views.AbstractStatefulInput
import com.hover.stax.views.Stax2LineItem
import com.hover.stax.views.StaxTextInputLayout
import org.koin.androidx.viewmodel.ext.android.getSharedViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import timber.log.Timber


class TransferFragment : AbstractFormFragment(), ActionSelect.HighlightListener {

    private val actionSelectViewModel: ActionSelectViewModel by sharedViewModel()
    private lateinit var transferViewModel: TransferViewModel

    private lateinit var amountInput: StaxTextInputLayout
    private lateinit var noteInput: StaxTextInputLayout
    private lateinit var actionSelect: ActionSelect
    private lateinit var contactInput: ContactInput
    private lateinit var recipientValue: Stax2LineItem

    private lateinit var binding: FragmentTransferBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        abstractFormViewModel = getSharedViewModel<TransferViewModel>()
        transferViewModel = abstractFormViewModel as TransferViewModel

        binding = FragmentTransferBinding.inflate(inflater, container, false)

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
        noteInput = binding.editCard.noteInput
        recipientValue = binding.summaryCard.recipientValue

        amountInput.apply {
            text = transferViewModel.amount.value
            requestFocus()
        }
        noteInput.text = transferViewModel.note.value

        super.init(root)
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
                binding.summaryCard.accountValue.setTitle(channel.toString())
            })

            channelActions.observe(viewLifecycleOwner, {
                actionSelectViewModel.setActions(it)
                actionSelect.updateActions(it)
            })
        }

        with(transferViewModel) {
            amount.observe(viewLifecycleOwner, {
                binding.summaryCard.amountValue.text = Utils.formatAmount(it)
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
        noteInput.addTextChangedListener(noteWatcher)
        fab.setOnClickListener { fabClicked() }
    }

    private fun fabClicked() {
        if (transferViewModel.isEditing.value == true) {
            if (validates()) {
                transferViewModel.saveContact()
                transferViewModel.setEditing(false)
            } else UIHelper.flashMessage(requireActivity(), getString(R.string.toast_pleasefix))
        } else (activity as TransferActivity).submit()
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
        override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            transferViewModel.setRecipient(charSequence.toString())
        }
    }

    private val noteWatcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
        override fun afterTextChanged(editable: Editable) {}
        override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            transferViewModel.setNote(charSequence.toString())
        }
    }

    private fun validates(): Boolean {
        val amountError = transferViewModel.amountErrors()
        amountInput.setState(amountError, if (amountError == null) AbstractStatefulInput.SUCCESS else AbstractStatefulInput.ERROR)

        val channelError = channelsViewModel.errorCheck()
        channelDropdown.setState(channelError, if (channelError == null) AbstractStatefulInput.SUCCESS else AbstractStatefulInput.ERROR)

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

    override fun highlightAction(a: HoverAction?) {
        a?.let { actionSelectViewModel.setActiveAction(a) }
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
        Timber.e(r.toString())
        channelsViewModel.activeChannel.value?.let {
            transferViewModel.setRecipientSmartly(r, it)
        }

        channelsViewModel.setChannelFromRequest(r)
        amountInput.text = r.amount
        contactInput.setText(r.requester_number, false)

        transferViewModel.setEditing(r.amount.isNullOrEmpty())
        channelDropdown.setState(getString(R.string.channel_request_fieldinfo, r.requester_institution_id.toString()), AbstractStatefulInput.INFO)
        Utils.logAnalyticsEvent(getString(R.string.loaded_request_link), requireContext())
    }
}