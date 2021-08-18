package com.hover.stax.transactions

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.hover.sdk.actions.HoverAction
import com.hover.sdk.transactions.Transaction
import com.hover.stax.R
import com.hover.stax.contacts.StaxContact
import com.hover.stax.databinding.FragmentTransactionPopupBinding
import com.hover.stax.utils.DateUtils.humanFriendlyDate
import com.hover.stax.utils.UIHelper
import com.hover.stax.utils.Utils.logAnalyticsEvent

class PopupTransactionDetailsFragment(private val uuid: String, private val callback: PopupTransDetailsListener) : DialogFragment() {
    val TAG = "PopupTransDetailsFragment"
    val SHOW_BOUNTY_SUBMIT = "bounty_submit_button"
    val USSD_MSG_SIZE = 1;

    private var viewModel: TransactionDetailsViewModel? = null

    private lateinit var _binding: FragmentTransactionPopupBinding
    private val binding get() = _binding;

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        viewModel = ViewModelProvider(requireActivity()).get(TransactionDetailsViewModel::class.java)

        logAnalyticsEvent(getString(R.string.visit_screen, getString(R.string.visit_transaction)), requireContext())
        _binding = FragmentTransactionPopupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setFullScreen()
    }

    private fun DialogFragment.setFullScreen() {
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startObservers()
        setUssdSessionMessagesRecyclerView()
        viewModel!!.setTransaction(uuid)
        setupScreenButtons()
    }

    private fun setUssdSessionMessagesRecyclerView() {
        setUSSDMessagesRecyclerView()
    }

    private fun startObservers() {
        viewModel!!.transaction.observe(viewLifecycleOwner, { transaction: StaxTransaction? -> showTransaction(transaction) })
        viewModel!!.action.observe(viewLifecycleOwner, { action: HoverAction? -> showActionDetails(action) })
        viewModel!!.contact.observe(viewLifecycleOwner, { contact: StaxContact? -> updateRecipient(contact) })
    }

    private fun setupScreenButtons() {
        binding.btnSeeMore.setOnClickListener { v: View -> callback.onUUIDReceived(uuid) }
        binding.transactionDetailsCard.setOnClickIcon { callback.closePopUp() }
    }

    private fun showTransaction(transaction: StaxTransaction?) {
        if (transaction != null) {
            updateDetails(transaction)
            showNotificationCard(transaction.isRecorded || transaction.status == Transaction.PENDING)
            if (transaction.isRecorded && viewModel!!.action.value != null) updateNotificationCard(viewModel!!.action.value)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateDetails(transaction: StaxTransaction) {
        binding.infoCard.detailsRecipientLabel.setText(if (transaction.transaction_type == HoverAction.RECEIVE) R.string.sender_label else R.string.recipient_label)
        binding.infoCard.detailsAmount.setText(transaction.displayAmount)
        binding.infoCard.detailsDate.setText(humanFriendlyDate(transaction.initiated_at))
        if (transaction.confirm_code != null && !transaction.confirm_code.isEmpty()) binding.infoCard.detailsTransactionNumber.setText(transaction.confirm_code) else binding.infoCard.detailsTransactionNumber.setText(transaction.uuid)
        if (transaction.isRecorded) hideDetails()
    }

    private fun hideDetails() {
        binding.infoCard.amountRow.setVisibility(View.GONE)
        binding.infoCard.recipientRow.setVisibility(View.GONE)
        binding.infoCard.recipAccountRow.setVisibility(View.GONE)
    }

    private fun showActionDetails(action: HoverAction?) {
        if (action != null) {
            binding.infoCard.detailsNetwork.setText(action.from_institution_name)
            if (viewModel!!.transaction.value != null && viewModel!!.transaction.value!!.isRecorded) updateNotificationCard(action)
        }
    }

    private fun showNotificationCard(show: Boolean) {
        binding.notificationCard.setVisibility(if (show) View.VISIBLE else View.GONE)
    }

    @SuppressLint("ResourceAsColor")
    private fun updateNotificationCard(action: HoverAction?) {
        binding.notificationCard.setBackgroundColor(if (action!!.bounty_is_open) R.color.pending_brown else R.color.muted_green)
        binding.notificationCard.setTitle(R.string.checking_your_flow)
        binding.notificationCard.setIcon(if (action.bounty_is_open) R.drawable.ic_warning else R.drawable.ic_check)
        binding.notificationDetail.setText(Html.fromHtml(if (action.bounty_is_open) getString(R.string.bounty_flow_pending_dialog_msg) else getString(R.string.flow_done_desc)))
    }

    private fun updateRecipient(contact: StaxContact?) {
        if (contact != null) binding.infoCard.detailsRecipient.setContact(contact)
    }

    private fun setUSSDMessagesRecyclerView() {
        val messagesView: RecyclerView = binding.convoRecyclerView
        messagesView.layoutManager = UIHelper.setMainLinearManagers(requireActivity())
        messagesView.setHasFixedSize(true)

        viewModel!!.messages.observe(viewLifecycleOwner, { ussdCallResponses: List<UssdCallResponse?>? -> if (ussdCallResponses != null) messagesView.adapter = MessagesAdapter(ussdCallResponses, USSD_MSG_SIZE) })
    }

}

interface PopupTransDetailsListener {
    fun onUUIDReceived(uuid: String)
    fun closePopUp();
}