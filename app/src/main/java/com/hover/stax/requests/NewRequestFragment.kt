package com.hover.stax.requests


import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.ViewGroup
import com.hover.stax.R
import com.hover.stax.channels.Channel
import com.hover.stax.contacts.ContactInput
import com.hover.stax.contacts.StaxContact
import com.hover.stax.databinding.FragmentRequestBinding
import com.hover.stax.pushNotification.PushNotificationTopicsInterface
import com.hover.stax.transfers.AbstractFormFragment
import com.hover.stax.utils.Constants
import com.hover.stax.utils.UIHelper
import com.hover.stax.utils.Utils
import com.hover.stax.views.AbstractStatefulInput
import com.hover.stax.views.Stax2LineItem
import com.hover.stax.views.StaxCardView
import com.hover.stax.views.StaxTextInputLayout
import org.koin.androidx.viewmodel.ext.android.getSharedViewModel


class NewRequestFragment : AbstractFormFragment(), PushNotificationTopicsInterface {

    private lateinit var requestViewModel: NewRequestViewModel
    private lateinit var amountInput: StaxTextInputLayout
    private lateinit var requesterNumberInput: StaxTextInputLayout
    private lateinit var noteInput: StaxTextInputLayout
    private lateinit var requesteeInput: ContactInput
    private lateinit var accountValue: Stax2LineItem
    private lateinit var shareCard: StaxCardView
    private lateinit var recipientValue: Stax2LineItem

    private var _binding: FragmentRequestBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        abstractFormViewModel = getSharedViewModel<NewRequestViewModel>()
        requestViewModel = abstractFormViewModel as NewRequestViewModel

        _binding = FragmentRequestBinding.inflate(inflater, container, false)

        init(binding.root)
        startObservers(binding.root)
        startListeners()
        setDefaultHelperText()
        setSummaryCardBackButton()
        setClickListeners()

        return binding.root
    }

    private fun setDefaultHelperText() = requesterNumberInput.setState(getString(R.string.account_num_desc), AbstractStatefulInput.NONE)

    override fun init(root: View) {
        amountInput = binding.editRequestCard.cardAmount.amountInput
        requesteeInput = binding.editRequestCard.cardRequestee.contactSelect
        requesterNumberInput = binding.editRequestCard.cardRequester.accountNumberInput
        noteInput = binding.editRequestCard.transferNote.noteInput

        recipientValue = binding.summaryCard.recipientValue
        accountValue = binding.summaryCard.accountValue
        shareCard = binding.shareCard.root

        amountInput.text = requestViewModel.amount.value
        noteInput.text = requestViewModel.note.value
        requesterNumberInput.text = requestViewModel.requesterNumber.value

        super.init(root)
    }

    override fun startObservers(root: View) {
        super.startObservers(root)

        channelsViewModel.activeChannel.observe(viewLifecycleOwner, {
            requestViewModel.setActiveChannel(it)
            accountValue.setTitle(it.toString())
        })

        with(requestViewModel) {
            amount.observe(viewLifecycleOwner, {
                binding.summaryCard.amountRow.visibility = if (validAmount()) View.VISIBLE else View.GONE
                binding.summaryCard.amountValue.text = Utils.formatAmount(it)
            })

            requesterNumber.observe(viewLifecycleOwner, { accountValue.setSubtitle(it) })
            activeChannel.observe(viewLifecycleOwner, { updateAcctNo(it) })
            recentContacts.observe(viewLifecycleOwner, { it?.let { contacts -> requesteeInput.setRecent(contacts, requireActivity()) } })
            isEditing.observe(viewLifecycleOwner, { showEdit(it) })

            note.observe(viewLifecycleOwner, {
                binding.summaryCard.noteRow.visibility = if (validNote()) View.VISIBLE else View.GONE
                binding.summaryCard.noteValue.text = it
            })
            
            requestee.observe(viewLifecycleOwner) { recipientValue.setContact(it) }
        }
    }

    override fun showEdit(isEditing: Boolean) {
        super.showEdit(isEditing)

        if (!isEditing) requestViewModel.createRequest()

        shareCard.visibility = if (isEditing) View.GONE else View.VISIBLE
        fab.visibility = if (isEditing) View.VISIBLE else View.GONE
    }

    override fun onContactSelected(requestCode: Int, contact: StaxContact) {
        requestViewModel.addRecipient(contact)
        requesteeInput.setSelected(contact)
    }

    private fun setSummaryCardBackButton() = binding.summaryCard.root.setOnClickIcon { requestViewModel.setEditing(true) }

    private fun updateAcctNo(channel: Channel?) {
        requesterNumberInput.text = channel?.accountNo
    }

    private fun startListeners() {
        amountInput.addTextChangedListener(amountWatcher)
        requesterNumberInput.addTextChangedListener(receivingAccountNumberWatcher)
        requesterNumberInput.onFocusChangeListener = OnFocusChangeListener { _: View?, hasFocus: Boolean ->
            if (!hasFocus) requesterNumberInput.setState(
                null,
                if (requestViewModel.requesterAcctNoError() == null) AbstractStatefulInput.SUCCESS else AbstractStatefulInput.NONE
            )
        }
        noteInput.addTextChangedListener(noteWatcher)

        requesteeInput.apply {
            setAutocompleteClickListener { view, _, position, _ ->
                val contact = view.getItemAtPosition(position) as StaxContact
                requestViewModel.addRecipient(contact)
            }
            addTextChangedListener(recipientWatcher)
            setChooseContactListener { contactPicker(Constants.GET_CONTACT, requireContext()) }
        }

        fab.setOnClickListener { fabClicked() }
    }

    private fun setClickListeners() {
        val activity = activity as RequestActivity
        binding.shareCard.smsShareSelection.setOnClickListener { activity.sendSms() }
        binding.shareCard.whatsappShareSelection.setOnClickListener { activity.sendWhatsapp() }
        binding.shareCard.copylinkShareSelection.setOnClickListener { activity.copyShareLink(it) }
    }

    private val amountWatcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
        override fun afterTextChanged(editable: Editable) {}
        override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            requestViewModel.setAmount(charSequence.toString())
        }
    }

    private val receivingAccountNumberWatcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        override fun afterTextChanged(s: Editable) {}
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            requestViewModel.setRequesterNumber(s.toString())
        }
    }

    private val noteWatcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
        override fun afterTextChanged(editable: Editable) {}
        override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            requestViewModel.setNote(charSequence.toString())
        }
    }

    private val recipientWatcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
        override fun afterTextChanged(editable: Editable) {}
        override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            requestViewModel.setRecipient(charSequence.toString())
        }
    }

    private fun fabClicked() {
        requestViewModel.removeInvalidRequestees()
        if (requestViewModel.isEditing.value!! && validates()) {
            updatePushNotifGroupStatus()
            requestViewModel.setEditing(false)
        } else UIHelper.flashMessage(requireActivity(), getString(R.string.toast_pleasefix))
    }

    private fun updatePushNotifGroupStatus() {
        joinRequestMoneyGroup(requireContext())
        leaveNoUsageGroup(requireContext())
        leaveNoRequestMoneyGroup(requireContext())
    }

    private fun validates(): Boolean {
        val channelError = channelsViewModel.errorCheck()
        channelDropdown.setState(channelError, if (channelError == null) AbstractStatefulInput.SUCCESS else AbstractStatefulInput.ERROR)

        val requesterAcctNoError = requestViewModel.requesterAcctNoError()
        requesterNumberInput.setState(requesterAcctNoError, if (requesterAcctNoError == null) AbstractStatefulInput.SUCCESS else AbstractStatefulInput.ERROR)

        val recipientError = requestViewModel.requesteeErrors()
        requesteeInput.setState(recipientError, if (recipientError == null) AbstractStatefulInput.SUCCESS else AbstractStatefulInput.ERROR)

        return channelError == null && requesterAcctNoError == null && recipientError == null
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }
}