package com.hover.stax.transactionDetails

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcher
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.hover.sdk.actions.HoverAction
import com.hover.sdk.api.Hover
import com.hover.sdk.transactions.Transaction
import com.hover.stax.ApplicationInstance
import com.hover.stax.R
import com.hover.stax.contacts.StaxContact
import com.hover.stax.databinding.FragmentTransactionBinding
import com.hover.stax.domain.model.Account
import com.hover.stax.home.MainActivity
import com.hover.stax.hover.AbstractHoverCallerActivity
import com.hover.stax.merchants.Merchant
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
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<RelativeLayout>

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

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, backPressedCallback)
    }

    private val backPressedCallback = object: OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            handleBackNavigation()
        }
    }

    private fun setListeners() {
        binding.transactionDetailsCard.setOnClickIcon { handleBackNavigation() }

        binding.transactionHeader.viewLogText.setOnClickListener { showUSSDLog() }
        with(binding.details.detailsStaxUuid.content) { setOnClickListener { Utils.copyToClipboard(this.text.toString(), requireContext()) } }
        with(binding.details.confirmCodeCopy.content) { setOnClickListener { Utils.copyToClipboard(this.text.toString(), requireContext()) } }
    }

    private fun handleBackNavigation(){
        val isBounty = viewModel.transaction.value?.isRecorded ?: false

        if (isBounty) findNavController().popBackStack()
        else NavUtil.navigate(findNavController(), TransactionDetailsFragmentDirections.actionTxnDetailsFragmentToNavigationHistory())
    }

    private fun showUSSDLog() {
        val log = USSDLogBottomSheetFragment.newInstance(args.uuid)
        log.show(childFragManager, USSDLogBottomSheetFragment::class.java.simpleName)
    }

    private fun startObservers() = with(viewModel) {
        val txnObserver = object : Observer<Transaction> {
            override fun onChanged(t: Transaction?) {
                t?.let { Timber.e("Updating transaction messages ${t.uuid}") }
            }
        }

        transaction.observe(viewLifecycleOwner) { showTransaction(it) }
        action.observe(viewLifecycleOwner) { it?.let { updateAction(it) } }
        contact.observe(viewLifecycleOwner) { updateRecipient(it) }
        merchant.observe(viewLifecycleOwner) { updateRecipient(it) }
        account.observe(viewLifecycleOwner) { it?.let { updateAccount(it) } }
        hoverTransaction.observe(viewLifecycleOwner, txnObserver)
        messages.observe(viewLifecycleOwner) { it?.let { updateMessages(it) } }
        bonusAmt.observe(viewLifecycleOwner) { showBonusAmount(it) }


        val observer = object : Observer<Boolean> {
            override fun onChanged(t: Boolean?) {
                Timber.i("Expecting sms $t")
                action.value?.let { a -> updateAction(a) }
            }

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
        showShareExcitement(transaction)
    }

    private fun updateHeader(transaction: StaxTransaction) = with(binding.transactionHeader) {
        binding.transactionDetailsCard.setTitle(HoverAction.getHumanFriendlyType(requireContext(), transaction.transaction_type))
        if (shouldShowNewBalance(transaction)) {
            mainMessage.text = getString(R.string.new_balance, "", transaction.displayBalance)
        }
        statusText.text = transaction.title(requireContext())
        statusIcon.setImageResource(transaction.getIcon())
    }

    private fun shouldShowNewBalance(transaction: StaxTransaction): Boolean {
        return !transaction.balance.isNullOrEmpty() && transaction.isSuccessful
    }

    private fun updateDetails(transaction: StaxTransaction) = with(binding.details) {
        detailsDate.text = humanFriendlyDateTime(transaction.updated_at)
        typeValue.text = transaction.toString(requireContext())
        viewModel.action.value?.let {
            categoryValue.text = transaction.shortStatusExplain(viewModel.action.value, "", requireContext())
        }

        statusValue.apply {
            text = transaction.humanStatus(requireContext())
            setCompoundDrawablesWithIntrinsicBounds(0, 0, transaction.getIcon(), 0)
        }

        recipientValue.setTitle(transaction.counterpartyNo)
        amountValue.text = transaction.getSignedAmount(transaction.amount)
        transaction.fee?.let { binding.details.feeValue.text = Utils.formatAmount(it.toString()) }
        newBalanceValue.text = Utils.formatAmount(transaction.balance.toString())
        recipientLabel.text = getString(transaction.getRecipientLabel())
        confirmCodeCopy.content.text = transaction.confirm_code
        detailsStaxUuid.content.text = transaction.uuid
    }

    private fun setVisibleFields(transaction: StaxTransaction) {
        transaction.transaction_type
        binding.transactionHeader.mainMessage.visibility = if (shouldShowNewBalance(transaction)) VISIBLE else GONE
        binding.statusInfo.root.visibility = if (transaction.isSuccessful) GONE else VISIBLE
        binding.statusInfo.institutionLogo.visibility = if (transaction.isFailed) VISIBLE else GONE
        binding.details.categoryRow.visibility = if (transaction.isFailed) VISIBLE else GONE
        binding.details.paidWithRow.visibility = if (transaction.isRecorded) GONE else VISIBLE
        if (transaction.isRecorded) binding.details.recipInstitutionRow.visibility = GONE
        binding.details.amountRow.visibility = if (transaction.amount != null) VISIBLE else GONE
        binding.details.feeRow.visibility = if (transaction.fee == null) GONE else VISIBLE
        binding.details.balanceRow.visibility = if (shouldShowNewBalance(transaction)) VISIBLE else GONE
        binding.details.recipientRow.visibility = if (transaction.isRecorded || transaction.transaction_type == HoverAction.BALANCE) GONE else VISIBLE
        binding.details.confirmCodeRow.visibility = if (transaction.isRecorded || transaction.confirm_code.isNullOrBlank()) GONE else VISIBLE
    }

    private fun updateAction(action: HoverAction) {
        Timber.e("action ${action.public_id} update. to country is: ${action.to_country_alpha2.isEmpty()}")
        if (action.isOnNetwork) binding.details.recipInstitutionRow.visibility = GONE
        else binding.details.institutionValue.setTitle(action.to_institution_name)
        viewModel.transaction.value?.let {
            val msg = it.longStatus(action, viewModel.messages.value?.last(), viewModel.sms.value, viewModel.isExpectingSMS.value ?: false, requireContext())
            binding.statusInfo.longDescription.text = HtmlCompat.fromHtml(msg, HtmlCompat.FROM_HTML_MODE_LEGACY)
            binding.details.categoryValue.text = it.shortStatusExplain(action,"", requireContext())
            if (action.transaction_type == HoverAction.BILL)
                binding.details.institutionValue.setSubtitle(Paybill.extractBizNumber(action))
        }
        binding.statusInfo.institutionLogo.loadImage(requireContext(), getString(R.string.root_url) + action.from_institution_logo)
    }

    private fun updateAccount(account: Account) {
        binding.details.paidWithValue.text = account.name
        binding.details.feeLabel.text = getString(R.string.transaction_fee, account.name)
    }

    private fun updateMessages(ussdCallResponses: List<UssdCallResponse>?) {
        viewModel.action.value?.let {
            viewModel.transaction.value?.let { t ->
                val msg = t.longStatus(
                    it,
                    ussdCallResponses?.last(),
                    viewModel.sms.value,
                    viewModel.isExpectingSMS.value ?: false,
                    requireContext()
                )
                binding.statusInfo.longDescription.text = HtmlCompat.fromHtml(msg, HtmlCompat.FROM_HTML_MODE_LEGACY)
            }
        }
    }

    private fun updateRecipient(contact: StaxContact?) = with(binding.details.recipientValue) {
        if (contact != null) {
            visibility = VISIBLE
            setContact(contact)
        } else visibility = GONE
    }

    private fun updateRecipient(merchant: Merchant?) {
        updateRecipient(merchant?.businessName, merchant?.tillNo)
    }

    private fun updateRecipient(title: String?, sub: String?) = with(binding.details.recipientValue) {
        if (!title.isNullOrEmpty() || !sub.isNullOrEmpty()) {
            visibility = VISIBLE
            setContent(title, sub)
        } else visibility = GONE
    }

    private fun addRetryOrSupportButton(transaction: StaxTransaction) {
        if (transaction.isRecorded)
            binding.statusInfo.btnRetry.setOnClickListener { retryBounty() }
        else if (transaction.status == Transaction.FAILED) {
            if (shouldContactSupport(transaction.action_id))
                setupContactSupportButton(transaction.action_id, binding.statusInfo.btnRetry)
            else binding.statusInfo.btnRetry.setOnClickListener { maybeRetry(transaction) }
        }
        binding.statusInfo.btnRetry.visibility = if (transaction.canRetry) VISIBLE else GONE
    }

    private fun shouldContactSupport(id: String): Boolean = if (retryCounter[id] != null) retryCounter[id]!! >= 3 else false

    private fun setupContactSupportButton(id: String, contactSupportTextView: TextView) {
        contactSupportTextView.setText(R.string.email_support)
        contactSupportTextView.setOnClickListener {
            resetTryAgainCounter(id)
            val deviceId = Hover.getDeviceId(requireContext())
            val subject = "Stax Transaction failure - support id- {${deviceId}}"
            Utils.openEmail(subject, requireActivity())
        }
    }

    private fun maybeRetry(transaction: StaxTransaction) {
        if (viewModel.account.value == null || viewModel.action.value == null || viewModel.transaction.value == null)
            UIHelper.flashAndReportError(requireContext(), R.string.error_still_loading)
        else {
            retry(transaction)
        }
    }

    private fun retry(transaction: StaxTransaction) {
        updateRetryCounter(transaction.action_id)
        if (transaction.transaction_type == HoverAction.BALANCE) {
            (requireActivity() as AbstractHoverCallerActivity)
                .runSession(viewModel.account.value!!, viewModel.action.value!!, viewModel.wrapExtras(), 0)
        } else if (transaction.transaction_type == HoverAction.P2P || transaction.transaction_type == HoverAction.AIRTIME)
            navToTransferDetail(transaction)
//        else if (transaction.transaction_type == HoverAction.BILL)
//            navToPaybill()
//        else if (transaction.transaction_type == HoverAction.MERCHANT)
//            navToMerchant()
    }

    private fun navToTransferDetail(transaction: StaxTransaction) {
        NavUtil.navigateTransfer(
            findNavController(), transaction.transaction_type,
            transaction.accountId.toString(), transaction.amount.toString(), transaction.counterparty_id
        )
    }

    private fun updateRetryCounter(id: String) {
        val currentCount: Int = if (retryCounter[id] != null) retryCounter[id]!! else 0
        retryCounter[id] = currentCount + 1
    }

    private fun resetTryAgainCounter(id: String) {
        retryCounter[id] = 0
    }

    private fun retryBounty() {
        viewModel.action.value?.let {
            (requireActivity() as MainActivity).makeRegularCall(it, R.string.clicked_retry_bounty_session)
        }
    }

    private fun showShareExcitement(transaction: StaxTransaction) {
        val isTransactionSuccessful = !transaction.isRecorded && transaction.isSuccessful

        val shareMessage = when (transaction.transaction_type) {
            HoverAction.AIRTIME -> getString(R.string.airtime_purchase_message, getString(R.string.share_link))
            HoverAction.BALANCE -> getString(R.string.check_balance_message, getString(R.string.share_link))
            HoverAction.P2P -> getString(R.string.send_money_message, getString(R.string.share_link))
            else -> getString(R.string.share_msg)
        }

        bottomSheetBehavior = BottomSheetBehavior.from(binding.shareLayout.bottomSheet)
        val shouldShow = args.isNewTransaction && isTransactionSuccessful
        setBottomSheetVisibility(shouldShow, shareMessage)
    }

    private fun setBottomSheetVisibility(isVisible: Boolean, shareMessage: String) {
        var updatedState = BottomSheetBehavior.STATE_HIDDEN

        if (isVisible) {
            updatedState = BottomSheetBehavior.STATE_EXPANDED
            val animation = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_down)

            binding.shareLayout.bottomSheet.visibility = VISIBLE
            binding.shareLayout.bottomSheet.animation = animation
            binding.shareLayout.shareBtn.setOnClickListener { Utils.shareStax(requireActivity(), shareMessage) }
        }

        bottomSheetBehavior.state = updatedState
    }


    private fun showBonusAmount(amount: Int) = with(binding.details) {
        val txn = viewModel.transaction.value

        if (amount > 0 && (txn != null && txn.isSuccessful)) {
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