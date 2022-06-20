package com.hover.stax.requests


import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.annotation.CallSuper
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.hover.stax.R
import com.hover.stax.domain.model.Account
import com.hover.stax.contacts.ContactInput
import com.hover.stax.contacts.StaxContact
import com.hover.stax.databinding.FragmentRequestBinding
import com.hover.stax.notifications.PushNotificationTopicsInterface
import com.hover.stax.transfers.AbstractFormFragment
import com.hover.stax.utils.AnalyticsUtil
import com.hover.stax.utils.Utils
import com.hover.stax.utils.collectLatestLifecycleFlow
import com.hover.stax.views.*
import org.koin.androidx.viewmodel.ext.android.getSharedViewModel
import timber.log.Timber


class NewRequestFragment : AbstractFormFragment(), PushNotificationTopicsInterface, RequestSenderInterface {

    private lateinit var requestViewModel: NewRequestViewModel

    private lateinit var amountInput: StaxTextInput
    private lateinit var requesterNumberInput: StaxTextInput
    private lateinit var noteInput: StaxTextInput
    private lateinit var requesteeInput: ContactInput
    private lateinit var accountValue: Stax2LineItem
    private lateinit var shareCard: StaxCardView
    private lateinit var recipientValue: Stax2LineItem

    private var requestDialog: StaxDialog? = null
    private var _binding: FragmentRequestBinding? = null
    private val binding get() = _binding!!

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        abstractFormViewModel = getSharedViewModel<NewRequestViewModel>()
        requestViewModel = abstractFormViewModel as NewRequestViewModel
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRequestBinding.inflate(inflater, container, false)
        AnalyticsUtil.logAnalyticsEvent(getString(R.string.visit_screen, getString(R.string.visit_new_request)), requireActivity())

        init(binding.root)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        startObservers(binding.root)
        startListeners()
        setDefaultHelperText()
        setSummaryCardBackButton()
        setClickListeners()

        handleBackPress()
    }

    private fun setDefaultHelperText() = requesterNumberInput.setState(
        getString(R.string.account_num_desc),
        AbstractStatefulInput.NONE
    )

    override fun init(root: View) {
        amountInput = binding.editCard.cardAmount.amountInput
        requesteeInput = binding.editCard.cardRequestee.contactSelect
        requesterNumberInput = binding.editCard.cardRequester.accountNumberInput
        noteInput = binding.editCard.transferNote.noteInput

        recipientValue = binding.summaryCard.recipientValue
        accountValue = binding.summaryCard.accountValue
        shareCard = binding.shareCard.root

        super.init(root)
    }

    override fun startObservers(root: View) {
        super.startObservers(root)

        //This is to prevent the SAM constructor from being compiled to singleton causing breakages. See
        //https://stackoverflow.com/a/54939860/2371515
        val accountsObserver = Observer<Account?> { a ->
            a?.let {
                requestViewModel.setActiveAccount(it)
                accountValue.setTitle(it.toString())
            }
        }

        with(accountsViewModel) {
            collectLatestLifecycleFlow(accounts){
                if (it.isEmpty())
                    setDropdownTouchListener(NewRequestFragmentDirections.actionNavigationRequestToAccountsFragment())
            }

            activeAccount.observe(viewLifecycleOwner, accountsObserver)
        }

        with(requestViewModel) {
            amount.observe(viewLifecycleOwner) {
                binding.summaryCard.amountRow.visibility = if (validAmount()) View.VISIBLE else View.GONE
                binding.summaryCard.amountValue.text = it?.let { Utils.formatAmount(it) }
            }

            requesterNumber.observe(viewLifecycleOwner) { accountValue.setSubtitle(it) }
            activeAccount.observe(viewLifecycleOwner) { updateAcctNo(it?.accountNo) }

            recentContacts.observe(viewLifecycleOwner) { it?.let { contacts -> requesteeInput.setRecent(contacts, requireActivity()) } }
            isEditing.observe(viewLifecycleOwner) { showEdit(it) }

            note.observe(viewLifecycleOwner) {
                binding.summaryCard.noteRow.visibility = if (validNote()) View.VISIBLE else View.GONE
                binding.summaryCard.noteValue.text = it
            }

            requestee.observe(viewLifecycleOwner) { recipientValue.setContact(it) }
        }
    }

    override fun showEdit(isEditing: Boolean) {
        super.showEdit(isEditing)

        if (!isEditing) requestViewModel.createRequest()

        shareCard.visibility = if (isEditing) View.GONE else View.VISIBLE
        fab.visibility = if (isEditing) View.VISIBLE else View.GONE
    }

    override fun onContactSelected(contact: StaxContact) {
        requestViewModel.addRecipient(contact)
        requesteeInput.setSelected(contact)
    }

    private fun setSummaryCardBackButton() = binding.summaryCard.root.setOnClickIcon { requestViewModel.setEditing(true) }

    private fun updateAcctNo(accountNo: String?) {
        requesterNumberInput.setText(accountNo)
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
            setChooseContactListener { startContactPicker(requireActivity()) }
        }
    }

    private fun setClickListeners() = with(binding.shareCard) {
        smsShareSelection.setOnClickListener { sendSms(requestViewModel, requireActivity()) }
        whatsappShareSelection.setOnClickListener { sendWhatsapp(requestViewModel, requireActivity()) }
        copylinkShareSelection.setOnClickListener { copyShareLink(it, requestViewModel, requireActivity()) }
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
            Timber.e(charSequence.toString())
            requestViewModel.setRecipient(charSequence.toString())
        }
    }

    override fun onFinishForm() {
        updatePushNotifGroupStatus()
        requestViewModel.setEditing(false)
    }

    override fun onSubmitForm() { }

    private fun updatePushNotifGroupStatus() {
        joinRequestMoneyGroup(requireContext())
        leaveNoUsageGroup(requireContext())
        leaveNoRequestMoneyGroup(requireContext())
    }

    override fun validates(): Boolean {
        val accountError = requestViewModel.accountError()
        payWithDropdown.setState(accountError, if (accountError == null) AbstractStatefulInput.SUCCESS else AbstractStatefulInput.ERROR)

        val requesterAcctNoError = requestViewModel.requesterAcctNoError()
        requesterNumberInput.setState(requesterAcctNoError, if (requesterAcctNoError == null) AbstractStatefulInput.SUCCESS else AbstractStatefulInput.ERROR)

        val recipientError = requestViewModel.requesteeErrors()
        requesteeInput.setState(recipientError, if (recipientError == null) AbstractStatefulInput.SUCCESS else AbstractStatefulInput.ERROR)

        requestViewModel.activeAccount.value?.let {
            if (!requestViewModel.isValidAccount()) {
                payWithDropdown.setState(getString(R.string.incomplete_account_setup_header), AbstractStatefulInput.ERROR)
                return false
            }
        }

        return accountError == null && requesterAcctNoError == null && recipientError == null
    }

    private fun handleBackPress() = requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            when {
                !requestViewModel.isEditing.value!! && requestViewModel.formulatedRequest.value == null -> requestViewModel.setEditing(true)
                !requestViewModel.isEditing.value!! && requestViewModel.formulatedRequest.value != null -> askAreYouSure()
                else -> findNavController().popBackStack()
            }
        }
    })

    private fun askAreYouSure() {
        requestDialog = StaxDialog(requireActivity())
            .setDialogTitle(R.string.reqsave_head)
            .setDialogMessage(R.string.reqsave_msg)
            .setPosButton(R.string.btn_save) { saveUnsent() }
            .setNegButton(R.string.btn_dontsave) { findNavController().popBackStack() }
        requestDialog!!.showIt()
    }

    private fun saveUnsent() {
        requestViewModel.saveRequest()
        AnalyticsUtil.logAnalyticsEvent(getString(R.string.saved_unsent_request), requireActivity())
        findNavController().popBackStack()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        if (dialog != null && dialog!!.isShowing) dialog!!.dismiss()
        if (requestDialog != null && requestDialog!!.isShowing) requestDialog!!.dismiss()
        _binding = null
    }
}