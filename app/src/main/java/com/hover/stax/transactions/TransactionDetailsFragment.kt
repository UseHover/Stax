package com.hover.stax.transactions

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.core.os.bundleOf
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
import com.hover.stax.contacts.StaxContact
import com.hover.stax.databinding.FragmentTransactionBinding
import com.hover.stax.home.MainActivity
import com.hover.stax.utils.AnalyticsUtil.logAnalyticsEvent
import com.hover.stax.utils.AnalyticsUtil.logErrorAndReportToFirebase
import com.hover.stax.utils.DateUtils
import com.hover.stax.utils.NavUtil
import com.hover.stax.utils.UIHelper
import com.hover.stax.utils.UIHelper.loadImage
import com.hover.stax.utils.Utils
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import org.json.JSONException
import org.json.JSONObject
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber


class TransactionDetailsFragment : Fragment() {

    private val viewModel: TransactionDetailsViewModel by viewModel()
    private var _binding: FragmentTransactionBinding? = null
    private val binding get() = _binding!!
    private val retryCounter = ApplicationInstance.txnDetailsRetryCounter

    private val args: TransactionDetailsFragmentArgs by navArgs()

    private lateinit var childFragManager: FragmentManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        viewModel.setTransaction(args.uuid)

        val data = JSONObject()
        try {
            data.put("uuid", args.uuid)
        } catch (e: JSONException) {
            logErrorAndReportToFirebase(TransactionDetailsFragment::class.java.simpleName, e.message!!, e)
        }

        logAnalyticsEvent(getString(R.string.visit_screen, getString(R.string.visit_transaction)), data, requireContext())
        _binding = FragmentTransactionBinding.inflate(inflater, container, false)

        childFragManager = childFragmentManager

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startObservers()

        binding.transactionDetailsCard.setOnClickIcon { NavUtil.navigate(findNavController(), TransactionDetailsFragmentDirections.actionTxnDetailsFragmentToNavigationHome()) }
        binding.primaryStatus.viewLogText.setOnClickListener { showUSSDLog() }
        with(binding.infoCard.detailsStaxUuid.content) { setOnClickListener { Utils.copyToClipboard(this.text.toString(), requireContext()) } }
        with(binding.infoCard.detailsServiceId.content) { setOnClickListener { Utils.copyToClipboard(this.text.toString(), requireContext()) } }
    }

    private fun showUSSDLog() {
        val log = USSDLogBottomSheetFragment.newInstance(args.uuid)
        log.show(childFragManager, USSDLogBottomSheetFragment::class.java.simpleName)
    }

    private fun startObservers() = with(viewModel) {
        transaction.observe(viewLifecycleOwner) { showTransaction(it) }
        action.observe(viewLifecycleOwner) { it?.let { showActionDetails(it) } }
        contact.observe(viewLifecycleOwner) { updateRecipient(it) }
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
            } else binding.secondaryStatus.transactionRetryButtonLayoutId.visibility = GONE
            updateDetails(transaction)
        }
    }

    private fun showButtonToClick(): TextView {
        val transactionButtonsLayout = binding.secondaryStatus.transactionRetryButtonLayoutId
        val retryButton = binding.secondaryStatus.btnRetryTransaction
        transactionButtonsLayout.visibility = VISIBLE
        return retryButton
    }

    private fun setupRetryBountyButton() {
        val bountyButtonsLayout = binding.secondaryStatus.transactionRetryButtonLayoutId
        val retryButton = binding.secondaryStatus.btnRetryTransaction
        bountyButtonsLayout.visibility = VISIBLE
        retryButton.setOnClickListener { retryBountyClicked() }
    }

    private fun retryBountyClicked() {
        viewModel.transaction.value?.let {
            (requireActivity() as MainActivity).retryCall(it.action_id)
        }
    }

    private fun retryTransactionClicked(transaction: StaxTransaction, retryButton: TextView) {
        retryButton.setOnClickListener {
            updateRetryCounter(transaction.action_id)
            if (transaction.isBalanceType) (requireActivity() as MainActivity).reBuildHoverSession(transaction)
            else (requireActivity() as MainActivity).navigateTransferAutoFill(transaction.transaction_type, transaction.uuid)
        }
    }

    private fun shouldShowNewBalance(transaction: StaxTransaction): Boolean {
        return !transaction.isBalanceType && !transaction.balance.isNullOrEmpty() && transaction.isSuccessful
    }

    @SuppressLint("SetTextI18n")
    private fun updateDetails(transaction: StaxTransaction) {
        val title = when (transaction.transaction_type) {
            HoverAction.P2P -> getString(R.string.send_money)
            HoverAction.AIRTIME -> getString(R.string.buy_airtime)
            else -> transaction.transaction_type
        }
        binding.transactionDetailsCard.setTitle(title)

        setDetailsData(transaction)
        setVisibleDetails(transaction)
        updateDetailsRequiringAction(viewModel.action.value, viewModel.transaction.value)
        updateStatus(viewModel.action.value, transaction)
    }

    private fun setDetailsData(transaction: StaxTransaction) = with(binding.infoCard) {
        if (shouldShowNewBalance(transaction)) {
            binding.primaryStatus.newBalance.apply {
                text = getString(R.string.new_balance, transaction.displayBalance)
                visibility = VISIBLE
            }
        }
        detailsRecipientLabel.setText(if (transaction.transaction_type == HoverAction.RECEIVE) R.string.sender_label else R.string.recipient_label)
        detailsAmount.text = transaction.displayAmount
        detailsDate.text = DateUtils.humanFriendlyDateTime(transaction.updated_at)
        detailsServiceId.content.text = transaction.confirm_code
        detailsStaxUuid.content.text = transaction.uuid
        detailsStaxStatus.apply {
            text = transaction.fullStatus.getPlainTitle(requireContext())
            setCompoundDrawablesWithIntrinsicBounds(0, 0, transaction.fullStatus.getIcon(), 0)
        }

        detailsStaxReason.text = transaction.fullStatus.getReason()
        transaction.fee?.let { binding.infoCard.detailsFee.text = Utils.formatAmount(it) }
    }

    private fun setVisibleDetails(transaction: StaxTransaction) = with(binding.infoCard) {
        reasonRow.visibility = if (transaction.isFailed) VISIBLE else GONE
        amountRow.visibility = if (transaction.isRecorded || transaction.transaction_type == HoverAction.BALANCE) GONE else VISIBLE
        recipientRow.visibility = if (transaction.isRecorded || transaction.transaction_type == HoverAction.BALANCE) GONE else VISIBLE
        recipAccountRow.visibility = if (transaction.isRecorded || transaction.transaction_type == HoverAction.BALANCE) GONE else VISIBLE
        serviceIdRow.visibility = if (transaction.isRecorded || transaction.confirm_code.isNullOrBlank()) GONE else VISIBLE
        feeRow.visibility = if (transaction.fee == null) GONE else VISIBLE
    }

    private fun updateDetailsRequiringAction(action: HoverAction?, transaction: StaxTransaction?) {
        if (action != null && transaction != null) {
            binding.infoCard.detailsStaxType.text = transaction.fullStatus.getDisplayType(requireContext(), action)
            binding.infoCard.detailsStaxAccount.text = action.from_institution_name
            binding.infoCard.detailsFeeLabel.text = getString(R.string.transaction_fee, action.from_institution_name)
        }
    }

    private fun showActionDetails(action: HoverAction) {
        binding.infoCard.detailsNetwork.text = action.from_institution_name
        updateStatus(action, viewModel.transaction.value)
        updateDetailsRequiringAction(viewModel.action.value, viewModel.transaction.value)
    }

    private fun updateStatus(action: HoverAction?, transaction: StaxTransaction?) {
        if (action != null && transaction != null) {
            setPrimaryStatus(transaction)
            setSecondaryStatus(action, transaction)
        }
    }

    private fun setPrimaryStatus(transaction: StaxTransaction?) {
        transaction?.let {
            var textValue = transaction.fullStatus.getTitle(requireContext())
            if (transaction.fullStatus.getReason().isNotEmpty()) textValue = textValue + ": " + transaction.fullStatus.getReason()

            binding.primaryStatus.apply {
                statusText.text = textValue
                statusIcon.setImageResource(transaction.fullStatus.getIcon())
            }
        }
    }

    private fun setSecondaryStatus(action: HoverAction?, transaction: StaxTransaction?) {
        transaction?.let {
            if (transaction.isSuccessful) binding.secondaryStatus.root.visibility = GONE
            else {
                binding.secondaryStatus.root.visibility = VISIBLE
                binding.secondaryStatus.statusText.apply {
                    val content = transaction.fullStatus.getStatusDetail(action, viewModel.messages.value?.last(), viewModel.sms.value, requireContext())
                    text = HtmlCompat.fromHtml(content, HtmlCompat.FROM_HTML_MODE_LEGACY)
                    movementMethod = LinkMovementMethod.getInstance()
                }
                if (transaction.isFailed) action?.let {
                    binding.secondaryStatus.statusIcon.loadImage(this@TransactionDetailsFragment, getString(R.string.root_url).plus(it.from_institution_logo))
                }
                else binding.secondaryStatus.statusIcon.visibility = GONE
            }
        }
    }

    private fun updateRecipient(contact: StaxContact?) {
        if (contact != null) {
            binding.infoCard.detailsRecipient.setContact(contact)
        } else binding.infoCard.detailsRecipient.setTitle(getString(R.string.self_choice))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}