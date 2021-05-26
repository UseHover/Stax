package com.hover.stax.requests


import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.hover.stax.R
import com.hover.stax.channels.Channel
import com.hover.stax.contacts.ContactInput
import com.hover.stax.contacts.StaxContact
import com.hover.stax.databinding.FragmentRequestBinding
import com.hover.stax.pushNotification.PushNotificationTopicsInterface
import com.hover.stax.transfers.AbstractFormFragment
import com.hover.stax.utils.UIHelper
import com.hover.stax.utils.Utils
import com.hover.stax.views.AbstractStatefulInput
import com.hover.stax.views.Stax2LineItem
import com.hover.stax.views.StaxCardView
import com.hover.stax.views.StaxTextInputLayout
import org.koin.androidx.viewmodel.ext.android.getViewModel


class NewRequestFragment: AbstractFormFragment(), RecipientAdapter.UpdateListener, PushNotificationTopicsInterface {

    private lateinit var requestViewModel: NewRequestViewModel
    private lateinit var amountInput: StaxTextInputLayout
    private lateinit var requesterNumberInput: StaxTextInputLayout
    private lateinit var noteInput: StaxTextInputLayout
    private lateinit var recipientInputList: RecyclerView
    private lateinit var addRecipientBtn: TextView
    private lateinit var recipientValueList: LinearLayout
    private lateinit var accountValue: Stax2LineItem
    private lateinit var shareCard: StaxCardView

    private var recipientAdapter: RecipientAdapter? = null
    private var recipientCount: Int = 0

    private lateinit var binding: FragmentRequestBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
//        channelDropdownViewModel = getViewModel()
        abstractFormViewModel = getViewModel<NewRequestViewModel>()
        requestViewModel = abstractFormViewModel as NewRequestViewModel

        binding = FragmentRequestBinding.inflate(inflater, container, false)

        init(binding.root)
        startObservers(binding.root)
        startListeners()

        return binding.root
    }

    override fun init(root: View) {
        amountInput = binding.editCard.cardAmount.amountInput
        recipientInputList = binding.editCard.cardRequestee.recipientList
        addRecipientBtn = binding.editCard.cardRequestee.addRecipientButton
        requesterNumberInput = binding.editCard.cardRequester.accountNumberInput
        noteInput = binding.editCard.transferNote.noteInput

        recipientValueList = binding.summaryCard.requesteeValueList
        accountValue = binding.summaryCard.accountValue
        shareCard = binding.shareCard.root

        amountInput.text = requestViewModel.amount.value
        noteInput.text = requestViewModel.note.value
        requesterNumberInput.text = requestViewModel.requesterNumber.value

        recipientAdapter = RecipientAdapter(requestViewModel.requestees.value, requestViewModel.recentContacts.value, this)
        recipientInputList.apply {
            layoutManager = UIHelper.setMainLinearManagers(requireContext())
            adapter = recipientAdapter
        }

        super.init(root)
    }

    override fun startObservers(root: View) {
        super.startObservers(root)

        channelDropdownViewModel.activeChannel.observe(viewLifecycleOwner, Observer {
            requestViewModel.setActiveChannel(it)
            accountValue.setTitle(it.toString())
        })

        with(requestViewModel){
            amount.observe(viewLifecycleOwner, Observer {
                binding.summaryCard.amountRow.visibility = if(validAmount()) View.VISIBLE else View.GONE
                binding.summaryCard.amountValue.text = Utils.formatAmount(it)
            })

            requesterNumber.observe(viewLifecycleOwner, Observer { accountValue.setSubtitle(it) })
            activeChannel.observe(viewLifecycleOwner, Observer { updateAcctNo(it) })
            recentContacts.observe(viewLifecycleOwner, Observer { recipientAdapter?.updateContactList(it) })
            isEditing.observe(viewLifecycleOwner, Observer { showEdit(it) })

            note.observe(viewLifecycleOwner, Observer {
                binding.summaryCard.noteRow.visibility = if(validNote()) View.VISIBLE else View.GONE
                binding.summaryCard.noteValue.text = it
            })

            requestees.observe(viewLifecycleOwner, Observer {
                if(!it.isNullOrEmpty()){
                    recipientValueList.removeAllViews()

                    it.forEach { contact ->
                        val li = Stax2LineItem(requireContext(), null)
                        li.setContact(contact)
                        recipientValueList.addView(li)
                    }

                    if(it.size != recipientCount){
                        recipientCount = it.size
                        recipientAdapter?.updateContactList(it)
                    }
                }
            })
        }
    }

    override fun showEdit(isEditing: Boolean){
        super.showEdit(isEditing)

        if(!isEditing) requestViewModel.createRequest()

        shareCard.visibility = if(isEditing) View.GONE else View.VISIBLE
        fab.visibility = if(isEditing) View.VISIBLE else View.GONE
    }

    private fun updateAcctNo(channel: Channel?){
        requesterNumberInput.text = channel?.accountNo
    }

    private fun startListeners(){
        amountInput.addTextChangedListener(amountWatcher)
        addRecipientBtn.setOnClickListener { requestViewModel.addRecipient(StaxContact("")) }
        requesterNumberInput.addTextChangedListener(receivingAccountNumberWatcher)
        requesterNumberInput.onFocusChangeListener = OnFocusChangeListener { _: View?, hasFocus: Boolean ->
            if (!hasFocus) requesterNumberInput.setState(
                null,
                if (requestViewModel.requesterAcctNoError() == null) AbstractStatefulInput.SUCCESS else AbstractStatefulInput.NONE
            )
        }
        noteInput.addTextChangedListener(noteWatcher)

        fab.setOnClickListener(this::fabClicked)
    }

    override fun onContactSelected(requestCode: Int, contact: StaxContact?) {
        requestViewModel.onUpdate(requestCode, contact!!)
        recipientAdapter?.notifyDataSetChanged()
    }

    override fun onUpdate(pos: Int, recipient: StaxContact?) {
        requestViewModel.onUpdate(pos, recipient!!)
    }

    override fun onClickContact(index: Int, c: Context?) {
        contactPicker(index, c!!)
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

    private fun fabClicked(v: View) {
        requestViewModel.removeInvalidRequestees()
        if (requestViewModel.isEditing.value!! && validates()) {
            updatePushNotifGroupStatus()
            requestViewModel.setEditing(false)
        } else UIHelper.flashMessage(requireActivity(), getString(R.string.toast_pleasefix))
    }

    private fun updatePushNotifGroupStatus() {
        joinByRequestMoneyNotifGroup(requireContext())
        stopReceivingNoActivityTopicNotifGroup(requireContext())
        stopReceivingNoRequestMoneyNotifGroup(requireContext())
    }

    private fun validates(): Boolean {
        val channelError = channelDropdownViewModel.errorCheck()
        channelDropdown.setState(channelError, if (channelError == null) AbstractStatefulInput.SUCCESS else AbstractStatefulInput.ERROR)

        val requesterAcctNoError = requestViewModel.requesterAcctNoError()
        requesterNumberInput.setState(requesterAcctNoError, if (requesterAcctNoError == null) AbstractStatefulInput.SUCCESS else AbstractStatefulInput.ERROR)

        val recipientError = requestViewModel.requesteeErrors()
        (recipientInputList.getChildAt(0) as ContactInput).setState(recipientError, if (recipientError == null) AbstractStatefulInput.SUCCESS else AbstractStatefulInput.ERROR)

        return channelError == null && requesterAcctNoError == null && recipientError == null
    }
}