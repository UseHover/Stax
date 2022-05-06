package com.hover.stax.transactions

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.hover.sdk.actions.HoverAction
import com.hover.sdk.api.Hover
import com.hover.sdk.transactions.Transaction
import com.hover.stax.ApplicationInstance
import com.hover.stax.R
import com.hover.stax.accounts.Account
import com.hover.stax.contacts.StaxContact
import com.hover.stax.databinding.FragmentTransactionBinding
import com.hover.stax.hover.AbstractHoverCallerActivity
import com.hover.stax.home.MainActivity
import com.hover.stax.utils.AnalyticsUtil.logAnalyticsEvent
import com.hover.stax.utils.AnalyticsUtil.logErrorAndReportToFirebase
import com.hover.stax.utils.DateUtils.humanFriendlyDateTime
import com.hover.stax.utils.UIHelper
import com.hover.stax.utils.Utils
import org.json.JSONException
import org.json.JSONObject
import org.koin.androidx.viewmodel.ext.android.viewModel

const val UUID = "uuid"

class TransactionDetailsFragment : Fragment() {

    private val viewModel: TransactionDetailsViewModel by viewModel()

    private var _binding: FragmentTransactionBinding? = null
    private val binding get() = _binding!!

    private val retryCounter = ApplicationInstance.txnDetailsRetryCounter

    private val args: TransactionDetailsFragmentArgs by navArgs()

    private lateinit var childFragManager: FragmentManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val uuid = requireArguments().getString(UUID)
        viewModel.setTransaction(uuid!!)
        logView(uuid)

        _binding = FragmentTransactionBinding.inflate(inflater, container, false)

        childFragManager = childFragmentManager

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startObservers()
        setListeners()
    }

    private fun setListeners() {
        binding.transactionDetailsCard.setOnClickIcon { findNavController().popBackStack() }
//        binding.header.viewLogText.setOnClickListener { showUSSDLog() }
        with(binding.details.detailsStaxUuid.content) { setOnClickListener { Utils.copyToClipboard(this.text.toString(), requireContext()) } }
        with(binding.details.confirmCodeCopy.content) { setOnClickListener { Utils.copyToClipboard(this.text.toString(), requireContext()) } }
    }

    private fun showUSSDLog() {
        val log = USSDLogBottomSheetFragment.newInstance(args.uuid)
        log.show(childFragManager, USSDLogBottomSheetFragment::class.java.simpleName)
    }

    private fun startObservers() {
        viewModel.transaction.observe(viewLifecycleOwner) { showTransaction(it) }
        viewModel.action.observe(viewLifecycleOwner) { it?.let { updateAction(it) } }
        viewModel.contact.observe(viewLifecycleOwner) { updateRecipient(it) }
        viewModel.account.observe(viewLifecycleOwner) { it?.let { updateAccount(it) } }
    }

    private fun setupRetryBountyButton() {
        val bountyButtonsLayout = binding.statusInfo.transactionRetryButtonLayoutId
        val retryButton = binding.statusInfo.btnRetry
        bountyButtonsLayout.visibility = VISIBLE
        retryButton.setOnClickListener { retryBountyClicked() }
    }

    private fun showButtonToClick(): TextView {
        val transactionButtonsLayout = binding.statusInfo.transactionRetryButtonLayoutId
        val retryButton = binding.statusInfo.btnRetry
        transactionButtonsLayout.visibility = VISIBLE
        return retryButton
    }

    private fun createRetryListener(transaction: StaxTransaction, retryButton: TextView) {
        retryButton.setOnClickListener {
            if (viewModel.account.value == null || viewModel.action.value == null || viewModel.contact.value == null || viewModel.transaction.value == null)
                UIHelper.flashMessage(requireContext(), getString(R.string.error_still_loading))
            else {
                updateRetryCounter(transaction.action_id)
                (requireActivity() as AbstractHoverCallerActivity).runSession(viewModel.account.value!!, viewModel.action.value!!, viewModel.wrapExtras(), 0)
            }
        }
    }

    private fun setupContactSupportButton(id: String, contactSupportTextView: TextView) {
        contactSupportTextView.setText(R.string.email_support)
        contactSupportTextView.setOnClickListener {
            resetTryAgainCounter(id)
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

    private fun shouldShowNewBalance(transaction: StaxTransaction) : Boolean {
        return !transaction.balance.isNullOrEmpty() && transaction.isSuccessful
    }

    private fun shouldContactSupport(id: String): Boolean = if (retryCounter[id] != null) retryCounter[id]!! >= 3 else false

    private fun showTransaction(transaction: StaxTransaction?) {
        if (transaction != null) {
            addRetryOrSupportButton(transaction)
            update(transaction)
        }
    }

    private fun addRetryOrSupportButton(transaction: StaxTransaction) {
        if (transaction.isRecorded)
            setupRetryBountyButton()
        else if (transaction.status == Transaction.FAILED) {
            val button = showButtonToClick()
            if (shouldContactSupport(transaction.action_id))
                setupContactSupportButton(transaction.action_id, button)
            else
                createRetryListener(transaction, button)
        }
        else binding.statusInfo.transactionRetryButtonLayoutId.visibility = GONE
    }

    private fun update(transaction: StaxTransaction) {
        updateHeader(transaction)
        updateDetails(transaction)
        setVisibleFields(transaction)
    }

    private fun updateHeader(transaction: StaxTransaction) = with(binding.transactionHeader) {
        binding.transactionDetailsCard.setTitle(transaction.description)
        if (shouldShowNewBalance(transaction)) {
            binding.transactionHeader.mainMessage.text = getString(R.string.new_balance, transaction.displayBalance)
        }
        binding.transactionHeader.statusText.text = generateTitle(transaction)
        binding.transactionHeader.statusIcon.setImageResource(transaction.fullStatus.getIcon())
    }

    private fun generateTitle(transaction: StaxTransaction): String {
        var textValue = transaction.fullStatus.getTitle(requireContext())
        if (transaction.fullStatus.getReason().isNotEmpty())
            textValue = textValue + ": " + transaction.fullStatus.getReason()
        return textValue
    }

    private fun updateDetails(transaction: StaxTransaction) = with(binding.details) {
        detailsDate.text = humanFriendlyDateTime(transaction.updated_at)
        amountValue.text = transaction.getSignedAmount(transaction.amount)
        newBalanceValue.text = Utils.formatAmount(transaction.balance.toString())
        recipientLabel.setText(if (transaction.transaction_type == HoverAction.RECEIVE) R.string.sender_label else R.string.recipient_label)
        confirmCodeCopy.content.text = transaction.confirm_code
        detailsStaxUuid.content.text = transaction.uuid
        detailsStaxStatus.apply {
            text = transaction.fullStatus.getPlainTitle(requireContext())
            setCompoundDrawablesWithIntrinsicBounds(0, 0, transaction.fullStatus.getIcon(), 0)
        }

        detailsStaxReason.text = transaction.fullStatus.getReason()
        transaction.fee?.let { binding.details.feeValue.text = Utils.formatAmount(it.toString()) }
    }

    private fun setVisibleFields(transaction: StaxTransaction) {
        binding.transactionHeader.mainMessage.visibility = if (shouldShowNewBalance(transaction)) VISIBLE else GONE
        binding.statusInfo.root.visibility = if (transaction.isSuccessful) GONE else VISIBLE
        binding.statusInfo.institutionLogo.visibility = if (transaction.isFailed) VISIBLE else GONE
        binding.details.amountRow.visibility = if (transaction.amount != null) VISIBLE else GONE
        binding.details.balanceRow.visibility = if (transaction.balance.isNullOrEmpty()) VISIBLE else GONE
        binding.details.feeRow.visibility = if (transaction.fee == null) GONE else VISIBLE
        binding.details.recipientRow.visibility = if (transaction.isRecorded || transaction.transaction_type == HoverAction.BALANCE) GONE else VISIBLE
        binding.details.recipInstitutionRow.visibility = if (transaction.isRecorded || transaction.transaction_type == HoverAction.BALANCE) GONE else VISIBLE
        binding.details.reasonRow.visibility = if(transaction.isFailed) VISIBLE else GONE
        binding.details.confirmCodeRow.visibility = if (transaction.isRecorded || transaction.confirm_code.isNullOrBlank()) GONE else VISIBLE
    }

    private fun updateAction(action: HoverAction) {
        if (viewModel.transaction.value != null) {
            binding.details.typeValue.text = viewModel.transaction.value!!.fullStatus.getDisplayType(requireContext(), action.to_institution_name)
            if (viewModel.transaction.value!!.isFailed)
                UIHelper.loadImage(requireContext(), getString(R.string.root_url) + action.from_institution_logo, binding.statusInfo.institutionLogo)
        }
    }

    private fun updateAccount(account: Account) {
        binding.details.paidWithValue.text = account.name
        binding.details.feeLabel.text = getString(R.string.transaction_fee, account.name)
    }

    private fun updateRecipient(contact: StaxContact?) {
        if (contact != null) {
            binding.details.recipientValue.visibility = VISIBLE
            binding.details.recipientValue.setContact(contact)
        } else binding.details.recipientValue.visibility = GONE
    }

    private fun retryBountyClicked() {
        viewModel.action.value?.let {
            (requireActivity() as MainActivity).makeRegularCall(it, R.string.clicked_retry_bounty_session)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun logView(uuid: String) {
        val data = JSONObject()
        try {
            data.put("uuid", uuid)
        } catch (e: JSONException) {
            logErrorAndReportToFirebase(TransactionDetailsFragment::class.java.simpleName, e.message!!, e)
        }

        logAnalyticsEvent(getString(R.string.visit_screen, getString(R.string.visit_transaction)), data, requireContext())
    }
}