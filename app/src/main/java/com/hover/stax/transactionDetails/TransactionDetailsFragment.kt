package com.hover.stax.transactionDetails

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
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
import com.hover.stax.home.MainActivity
import com.hover.stax.hover.AbstractHoverCallerActivity
import com.hover.stax.paybill.Paybill
import com.hover.stax.transactions.StaxTransaction
import com.hover.stax.utils.AnalyticsUtil.logAnalyticsEvent
import com.hover.stax.utils.AnalyticsUtil.logErrorAndReportToFirebase
import com.hover.stax.utils.DateUtils.humanFriendlyDateTime
import com.hover.stax.utils.NavUtil
import com.hover.stax.utils.UIHelper
import com.hover.stax.utils.UIHelper.loadImage
import com.hover.stax.utils.Utils
import org.json.JSONException
import org.json.JSONObject
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

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
        binding.transactionHeader.viewLogText.setOnClickListener { showUSSDLog() }
        with(binding.details.detailsStaxUuid.content) { setOnClickListener { Utils.copyToClipboard(this.text.toString(), requireContext()) } }
        with(binding.details.confirmCodeCopy.content) { setOnClickListener { Utils.copyToClipboard(this.text.toString(), requireContext()) } }
    }

    private fun showUSSDLog() {
        val log = USSDLogBottomSheetFragment.newInstance(args.uuid)
        log.show(childFragManager, USSDLogBottomSheetFragment::class.java.simpleName)
    }

    private fun startObservers() = with(viewModel) {
        transaction.observe(viewLifecycleOwner) { showTransaction(it) }
        action.observe(viewLifecycleOwner) { it?.let { updateAction(it) } }
        contact.observe(viewLifecycleOwner) { updateRecipient(it) }
        account.observe(viewLifecycleOwner) { it?.let { updateAccount(it) } }
        hoverTransaction.observe(viewLifecycleOwner) { it?.let { updateTransaction(it) } }
        messages.observe(viewLifecycleOwner) { it?.let { updateMessages(it) } }
        bonusAmt.observe(viewLifecycleOwner) { showBonusAmount(it) }

        val observer = Observer<Boolean> {
            Timber.i("Expecting sms $it")
            action.value?.let { a -> updateAction(a) }
        }
        isExpectingSMS.observe(viewLifecycleOwner, observer)
    }

    private fun showTransaction(transaction: StaxTransaction?) {
        if (transaction != null) {
            addRetryOrSupportButton(transaction)
            update(transaction)
        }
    }

    private fun update(transaction: StaxTransaction) {
        updateHeader(transaction)
        updateDetails(transaction)
        setVisibleFields(transaction)
    }

    private fun updateHeader(transaction: StaxTransaction) = with(binding.transactionHeader) {
        binding.transactionDetailsCard.setTitle(transaction.description)
        if (shouldShowNewBalance(transaction)) {
            mainMessage.text = getString(R.string.new_balance, transaction.displayBalance)
        }
        statusText.text = transaction.title(requireContext())
        statusIcon.setImageResource(transaction.getIcon())
    }

    private fun shouldShowNewBalance(transaction: StaxTransaction): Boolean {
        return !transaction.balance.isNullOrEmpty() && transaction.isSuccessful
    }

    private fun updateDetails(transaction: StaxTransaction) = with(binding.details) {
        detailsDate.text = humanFriendlyDateTime(transaction.updated_at)
        viewModel.action.value?.let {
            categoryValue.text = transaction.shortDescription(viewModel.action.value, requireContext())
        }

        statusValue.apply {
            text = transaction.humanStatus(requireContext())
            setCompoundDrawablesWithIntrinsicBounds(0, 0, transaction.getIcon(), 0)
        }

        amountValue.text = transaction.getSignedAmount(transaction.amount)
        transaction.fee?.let { binding.details.feeValue.text = Utils.formatAmount(it.toString()) }
        newBalanceValue.text = Utils.formatAmount(transaction.balance.toString())
        recipientLabel.text = getString(transaction.getRecipientLabel())
        confirmCodeCopy.content.text = transaction.confirm_code
        detailsStaxUuid.content.text = transaction.uuid
    }

    private fun setVisibleFields(transaction: StaxTransaction) {
        binding.transactionHeader.mainMessage.visibility = if (shouldShowNewBalance(transaction)) VISIBLE else GONE
        binding.statusInfo.root.visibility = if (transaction.isSuccessful) GONE else VISIBLE
//        binding.statusInfo.failureInfo.visibility = if (transaction.isSuccessful) GONE else VISIBLE //TODO root visibility is gone, test impact of this
        binding.statusInfo.institutionLogo.visibility = if (transaction.isFailed) VISIBLE else GONE
        binding.details.categoryRow.visibility = if (transaction.isFailed) VISIBLE else GONE
        binding.details.recipInstitutionRow.visibility = if (transaction.isRecorded) GONE else VISIBLE
        binding.details.amountRow.visibility = if (transaction.amount != null) VISIBLE else GONE
        binding.details.feeRow.visibility = if (transaction.fee == null) GONE else VISIBLE
        binding.details.balanceRow.visibility = if (shouldShowNewBalance(transaction)) VISIBLE else GONE
        binding.details.recipientRow.visibility = if (transaction.isRecorded || transaction.transaction_type == HoverAction.BALANCE) GONE else VISIBLE
        binding.details.confirmCodeRow.visibility = if (transaction.isRecorded || transaction.confirm_code.isNullOrBlank()) GONE else VISIBLE
    }

    private fun updateAction(action: HoverAction) {
        if (action.isOnNetwork) binding.details.recipInstitutionRow.visibility = GONE
        binding.details.institutionValue.setTitle(action.to_institution_name)
        viewModel.transaction.value?.let {
            binding.statusInfo.longDescription.text = it.longDescription(action, viewModel.messages.value?.last(), viewModel.sms.value, viewModel.isExpectingSMS.value ?: false, requireContext())
            binding.details.typeValue.text = it.humanTransactionType(requireContext(), action.to_institution_name)
            binding.details.categoryValue.text = it.shortDescription(action, requireContext())
            if (action.transaction_type == HoverAction.C2B)
                binding.details.institutionValue.setSubtitle(Paybill.extractBizNumber(action))
        }
        binding.statusInfo.institutionLogo.loadImage(requireContext(), getString(R.string.root_url) + action.from_institution_logo)
    }

    private fun updateAccount(account: Account) {
        binding.details.paidWithValue.text = account.name
        binding.details.feeLabel.text = getString(R.string.transaction_fee, account.name)
    }

    private fun updateTransaction(t: Transaction) {
        viewModel.action.value?.let {
            if (it.transaction_type == HoverAction.C2B && t.input_extras.has(HoverAction.ACCOUNT_KEY)) {
                binding.details.recipientValue.setTitle(t.input_extras.getString(HoverAction.ACCOUNT_KEY))
            }
        }
    }

    private fun updateMessages(ussdCallResponses: List<UssdCallResponse>?) {
        viewModel.action.value?.let {
            viewModel.transaction.value?.let { t ->
                binding.statusInfo.longDescription.text = t.longDescription(
                    it,
                    ussdCallResponses?.last(),
                    viewModel.sms.value,
                    viewModel.isExpectingSMS.value ?: false,
                    requireContext()
                )
            }
        }
    }

    private fun updateRecipient(contact: StaxContact?) = with(binding.details.recipientValue) {
        if (contact != null) {
            visibility = VISIBLE
            setContact(contact)
        } else visibility = GONE
    }

    private fun shouldContactSupport(id: String): Boolean = if (retryCounter[id] != null) retryCounter[id]!! >= 3 else false

    private fun addRetryOrSupportButton(transaction: StaxTransaction) {
        if (transaction.isRecorded)
            setupRetryBountyButton()
        else if (transaction.status == Transaction.FAILED) {
            val button = showButtonToClick()
            if (shouldContactSupport(transaction.action_id))
                setupContactSupportButton(transaction.action_id, button)
            else if (transaction.isRetryable)
                createRetryListener(transaction, button)
        } else binding.statusInfo.btnRetry.visibility = GONE
    }

    private fun setupRetryBountyButton() {
        binding.statusInfo.btnRetry.apply {
            visibility = VISIBLE
            setOnClickListener{ retryBountyClicked() }
        }
    }

    private fun showButtonToClick(): Button {
        return binding.statusInfo.btnRetry.also { it.visibility = VISIBLE }
    }

    private fun createRetryListener(transaction: StaxTransaction, retryButton: TextView) {
        retryButton.setOnClickListener {
            if (viewModel.account.value == null || viewModel.action.value == null || viewModel.transaction.value == null)
                UIHelper.flashMessage(requireContext(), getString(R.string.error_still_loading))
            else {
                retry(transaction)
            }
        }
    }

    private fun retry(transaction: StaxTransaction) {
        updateRetryCounter(transaction.action_id)
        if (transaction.transaction_type == HoverAction.BALANCE) {
            (requireActivity() as AbstractHoverCallerActivity)
                .runSession(viewModel.account.value!!, viewModel.action.value!!, viewModel.wrapExtras(), 0)
        } else {
            navToTransferDetail(transaction)
        }
    }

    private fun navToTransferDetail(transaction: StaxTransaction) {
        NavUtil.navigateTransfer(
            findNavController(), transaction.transaction_type,
            transaction.accountId.toString(), transaction.amount.toString(), transaction.counterparty_id
        )
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

    private fun retryBountyClicked() {
        viewModel.action.value?.let {
            (requireActivity() as MainActivity).makeRegularCall(it, R.string.clicked_retry_bounty_session)
        }
    }

    private fun showBonusAmount(amount: Int) = with(binding.details) {
        val txn = viewModel.transaction.value

        if(amount > 0 && (txn != null && txn.isSuccessful)){
            bonusRow.visibility = VISIBLE
            bonusAmount.text = amount.toString()
        } else {
            bonusRow.visibility = GONE
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