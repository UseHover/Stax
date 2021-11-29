package com.hover.stax.transactions

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import android.widget.Button
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView
import com.hover.sdk.actions.HoverAction
import com.hover.sdk.api.Hover
import com.hover.sdk.transactions.Transaction
import com.hover.stax.ApplicationInstance
import com.hover.stax.R
import com.hover.stax.contacts.StaxContact
import com.hover.stax.databinding.FragmentTransactionBinding
import com.hover.stax.home.MainActivity
import com.hover.stax.navigation.NavigationInterface
import com.hover.stax.utils.AnalyticsUtil.Companion.logAnalyticsEvent
import com.hover.stax.utils.AnalyticsUtil.Companion.logErrorAndReportToFirebase
import com.hover.stax.utils.DateUtils.humanFriendlyDateTime
import com.hover.stax.utils.UIHelper
import com.hover.stax.utils.Utils
import org.json.JSONException
import org.json.JSONObject
import org.koin.androidx.viewmodel.ext.android.viewModel


class TransactionDetailsFragment : DialogFragment(), NavigationInterface {

    private val viewModel: TransactionDetailsViewModel by viewModel()
    private var _binding: FragmentTransactionBinding? = null
    private val binding get() = _binding!!
    private val retryCounter = ApplicationInstance.txnDetailsRetryCounter

    private var uuid: String? = null
    private var isFullScreen = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isFullScreen = requireArguments().getBoolean(IS_FULLSCREEN)
        if (isFullScreen) setFullScreen()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (!isFullScreen) popUpSize()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        uuid = requireArguments().getString(UUID)

        val data = JSONObject()
        try {
            data.put("uuid", uuid)
        } catch (e: JSONException) {
            logErrorAndReportToFirebase(TransactionDetailsFragment::class.java.simpleName, e.message!!, e)
        }

        logAnalyticsEvent(getString(R.string.visit_screen, getString(R.string.visit_transaction)), data, requireContext())
        _binding = FragmentTransactionBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun setFullScreen() {
        setStyle(STYLE_NO_FRAME, R.style.StaxDialogFullScreen)
    }

    private fun DialogFragment.popUpSize() {
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startObservers()
        if (!isFullScreen) setToPopupDesign()
        createUSSDMessagesRecyclerView()
        createSmsMessagesRecyclerView()
        viewModel.setTransaction(uuid!!)
        binding.transactionDetailsCard.setOnClickIcon { this.dismiss() }
        setupSeeMoreButton()
    }

    private fun startObservers() {
        viewModel.transaction.observe(viewLifecycleOwner, { showTransaction(it) })
        viewModel.action.observe(viewLifecycleOwner, { it?.let { showActionDetails(it) } })
        viewModel.contact.observe(viewLifecycleOwner, { updateRecipient(it) })
    }

    private fun setToPopupDesign() {
        binding.ftMainBg.setBackgroundColor(ContextCompat.getColor(requireActivity(), R.color.colorPrimary))
        binding.transactionDetailsCard.setIcon(R.drawable.ic_close_white)
        binding.transactionDetailsCard.setTitle(R.drawable.ic_close_white)

        binding.transactionDetailsCard.makeFlatView()
        binding.statusCard.makeFlatView()
        binding.messagesCard.makeFlatView()
    }

    private fun createUSSDMessagesRecyclerView() {
        val messagesView = binding.convoRecyclerView
        messagesView.layoutManager = UIHelper.setMainLinearManagers(requireActivity())
        messagesView.setHasFixedSize(true)
        viewModel.messages.observe(viewLifecycleOwner, { updateWithSessionDetails(it, messagesView) })
    }

    private fun createSmsMessagesRecyclerView() {
        val smsView = binding.smsRecyclerView
        smsView.layoutManager = UIHelper.setMainLinearManagers(requireActivity())
        smsView.setHasFixedSize(true)
        viewModel.sms.observe(viewLifecycleOwner, { updateWithSessionDetails(it, smsView) })
    }

    private fun updateWithSessionDetails(messages: List<UssdCallResponse>?, v: RecyclerView) {
        if (messages != null) {
            if (viewModel.transaction.value?.status == Transaction.SUCCEEDED)
                setStatusText(viewModel.action.value, viewModel.transaction.value)
            v.adapter = MessagesAdapter(messages)
        }
    }

    private fun setupSeeMoreButton() {
        val retryButton = binding.retrySubmit.btnRetry
        val bountyButtonsLayout = binding.retrySubmit.bountyRetryButtonLayoutId
        if (!isFullScreen) {
            bountyButtonsLayout.visibility = View.VISIBLE
            retryButton.setText(R.string.view_full_details)
            retryButton.setOnClickListener { recreateFullScreen() }
        }
    }

    private fun setupRetryBountyButton() {
        val bountyButtonsLayout = binding.retrySubmit.bountyRetryButtonLayoutId
        val retryButton = binding.retrySubmit.btnRetry
        bountyButtonsLayout.visibility = View.VISIBLE
        retryButton.setOnClickListener { retryBountyClicked() }
    }

    private fun showButtonToClick(): Button {
        val transactionButtonsLayout = binding.transactionRetryButtonLayoutId
        val retryButton = binding.btnRetryTransaction
        transactionButtonsLayout.visibility = View.VISIBLE
        return retryButton
    }

    private fun retryTransactionClicked(transaction: StaxTransaction, retryButton: Button) {
        retryButton.setOnClickListener {
            updateRetryCounter(transaction.action_id)
            this.dismiss()
            (requireActivity() as MainActivity).reBuildHoverSession(transaction)
        }
    }

    private fun setupContactSupportButton(id: String, contactSupportButton: Button) {
        contactSupportButton.setText(R.string.email_support)
        contactSupportButton.setOnClickListener {
            resetTryAgainCounter(id)
            this.dismiss()
            val deviceId = Hover.getDeviceId(requireContext())
            val subject = "Stax Transaction failure - support id- {${deviceId}}"
            Utils.openEmail(subject, requireActivity())
        }
    }

    private fun updateRetryCounter(id: String) {
        val currentCount: Int = if (retryCounter[id] != null) retryCounter[id]!! else 0
        retryCounter[id] = currentCount + 1
    }

    private fun resetTryAgainCounter(id: String) {
        retryCounter[id] = 0
    }

    private fun shouldContactSupport(id: String): Boolean = if (retryCounter[id] != null) retryCounter[id]!! >= 3 else false

    private fun showTransaction(transaction: StaxTransaction?) {
        if (transaction != null) {
            if (transaction.isRecorded)
                setupRetryBountyButton()
            else if (transaction.status == Transaction.FAILED) {
                val button = showButtonToClick()
                if (shouldContactSupport(transaction.action_id))
                    setupContactSupportButton(transaction.action_id, button)
                else
                    retryTransactionClicked(transaction, button)
            }
            updateDetails(transaction)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateDetails(transaction: StaxTransaction) {
        if (isFullScreen)
            binding.transactionDetailsCard.setTitle(transaction.description)
        else {
            if (viewModel.action.value != null)
                binding.transactionDetailsCard.setTitle(
                        transaction.generateLongDescription(viewModel.action.value, viewModel.contact.value, requireContext())
                )
            binding.infoCard.root.visibility = GONE
        }

        binding.infoCard.detailsRecipientLabel.setText(if (transaction.transaction_type == HoverAction.RECEIVE) R.string.sender_label else R.string.recipient_label)
        binding.infoCard.detailsAmount.text = transaction.displayAmount
        binding.infoCard.detailsDate.text = humanFriendlyDateTime(transaction.updated_at)
        binding.infoCard.detailsServiceId.text = transaction.confirm_code
        binding.infoCard.detailsStaxUuid.text = transaction.uuid

        setVisibleDetails(transaction)
        updateStatus(viewModel.action.value, transaction)
    }

    private fun setVisibleDetails(transaction: StaxTransaction) {
        binding.infoCard.amountRow.visibility = if (transaction.isRecorded || transaction.transaction_type == HoverAction.BALANCE) GONE else View.VISIBLE
        binding.infoCard.recipientRow.visibility = if (transaction.isRecorded || transaction.transaction_type == HoverAction.BALANCE) GONE else View.VISIBLE
        binding.infoCard.recipAccountRow.visibility = if (transaction.isRecorded || transaction.transaction_type == HoverAction.BALANCE) GONE else View.VISIBLE
        binding.infoCard.serviceIdRow.visibility = if (transaction.isRecorded || transaction.confirm_code.isNullOrBlank()) GONE else View.VISIBLE
    }

    private fun showActionDetails(action: HoverAction) {
        if (!isFullScreen) {
            binding.transactionDetailsCard.setTitle(viewModel.transaction.value?.generateLongDescription(action, viewModel.contact.value, requireContext()))
        }
        binding.infoCard.detailsNetwork.text = action?.from_institution_name
        updateStatus(action, viewModel.transaction.value)
    }

    private fun updateStatus(action: HoverAction?, transaction: StaxTransaction?) {
        if (action != null && transaction != null) {
            binding.statusCard.updateState(transaction.fullStatus.getIcon(), transaction.fullStatus.getBackgroundColor(), transaction.fullStatus.getTitle())
            setStatusText(action, transaction)
        }
    }

    private fun setStatusText(action: HoverAction?, transaction: StaxTransaction?) {
        if (transaction != null) {
            binding.statusText.text = transaction.fullStatus.getStatusDetail(action, viewModel.messages.value?.last(), viewModel.sms.value, requireContext())
        }
    }

    private fun updateRecipient(contact: StaxContact?) {
        if (contact != null) {
            if (!isFullScreen && viewModel.action.value != null)
                binding.transactionDetailsCard.setTitle(viewModel.transaction.value?.generateLongDescription(viewModel.action.value, contact, requireContext()))
            binding.infoCard.detailsRecipient.setContact(contact)
        }
    }

    private fun recreateFullScreen() {
        this.dismiss()
        val frag = newInstance(uuid!!, true)
        frag.show(parentFragmentManager, "dialogFrag")
    }

    private fun retryBountyClicked() {
        this.dismiss()

        viewModel.transaction.value?.let {
            (requireActivity() as MainActivity).retryCall(it.action_id)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val UUID = "uuid"
        const val IS_FULLSCREEN = "isFullScreen"

        fun newInstance(uuid: String, isFullScreen: Boolean): TransactionDetailsFragment {
            val fragment = TransactionDetailsFragment().apply {
                arguments = bundleOf(UUID to uuid, IS_FULLSCREEN to isFullScreen)
            }

            return fragment
        }
    }

}