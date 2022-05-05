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
import com.hover.stax.utils.DateUtils
import com.hover.stax.utils.NavUtil
import com.hover.stax.utils.UIHelper
import com.hover.stax.utils.Utils
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import org.json.JSONException
import org.json.JSONObject
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

const val UUID = "uuid"

class TransactionDetailsFragment : Fragment(), Target {

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
        setListeners();
    }

    private fun setListeners() {
        binding.transactionDetailsCard.setOnClickIcon { NavUtil.navigate(findNavController(), TransactionDetailsFragmentDirections.actionTxnDetailsFragmentToNavigationHome()) }
        binding.primaryStatus.viewLogText.setOnClickListener { showUSSDLog() }
        with(binding.infoCard.detailsStaxUuid.content) { setOnClickListener { Utils.copyToClipboard(this.text.toString(), requireContext()) } }
        with(binding.infoCard.confirmCodeCopy.content) { setOnClickListener { Utils.copyToClipboard(this.text.toString(), requireContext()) } }
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
        val bountyButtonsLayout = binding.secondaryStatus.transactionRetryButtonLayoutId
        val retryButton = binding.secondaryStatus.btnRetry
        bountyButtonsLayout.visibility = VISIBLE
        retryButton.setOnClickListener { retryBountyClicked() }
    }

    private fun showButtonToClick(): TextView {
        val transactionButtonsLayout = binding.secondaryStatus.transactionRetryButtonLayoutId
        val retryButton = binding.secondaryStatus.btnRetry
        transactionButtonsLayout.visibility = VISIBLE
        return retryButton
    }

    private fun retryTransactionClicked(transaction: StaxTransaction, retryButton: TextView) {
        retryButton.setOnClickListener {
            if (viewModel.account.value == null || viewModel.action.value == null || viewModel.contact.value == null || viewModel.transaction.value == null)
                UIHelper.flashMessage(requireContext(), getString(R.string.error_still_loading))
            else {
                updateRetryCounter(transaction.action_id)
                (requireActivity() as AbstractHoverCallerActivity).run(viewModel.account.value!!, viewModel.action.value!!, viewModel.wrapExtras(), 0)
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
        return !transaction.isBalanceType && !transaction.balance.isNullOrEmpty() && transaction.isSuccessful
    }

    private fun shouldContactSupport(id: String): Boolean = if (retryCounter[id] != null) retryCounter[id]!! >= 3 else false

    private fun showTransaction(transaction: StaxTransaction?) {
        if (transaction != null) {
            addRetryOrSupportButton(transaction)
            updateDetails(transaction)
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
                retryTransactionClicked(transaction, button)
        }
        else binding.secondaryStatus.transactionRetryButtonLayoutId.visibility = GONE
    }

    @SuppressLint("SetTextI18n")
    private fun updateDetails(transaction: StaxTransaction) {
        setTitle(transaction)
        setDetailsData(transaction)
        setVisibleDetails(transaction)
    }

    private fun setTitle(transaction: StaxTransaction) {
        binding.transactionDetailsCard.setTitle(transaction.description)
    }

    private fun generateTitle(transaction: StaxTransaction): String {
        var textValue = transaction.fullStatus.getTitle(requireContext())
        if (transaction.fullStatus.getReason().isNotEmpty())
            textValue = textValue + ": " + transaction.fullStatus.getReason()
        return textValue
    }

    private fun setDetailsData(transaction: StaxTransaction) = with(binding.infoCard) {
        if (shouldShowNewBalance(transaction))
            binding.primaryStatus.newBalance.text = getString(R.string.new_balance, transaction.displayBalance)
        binding.primaryStatus.statusText.text = generateTitle(transaction)
        binding.primaryStatus.statusIcon.setImageResource(transaction.fullStatus.getIcon())

        detailsRecipientLabel.setText(if (transaction.transaction_type == HoverAction.RECEIVE) R.string.sender_label else R.string.recipient_label)
        detailsAmount.text = transaction.displayAmount
        detailsDate.text = humanFriendlyDateTime(transaction.updated_at)
        confirmCodeCopy.content.text = transaction.confirm_code
        detailsStaxUuid.content.text = transaction.uuid
        detailsStaxStatus.apply {
            text = transaction.fullStatus.getPlainTitle(requireContext())
            setCompoundDrawablesWithIntrinsicBounds(0, 0, transaction.fullStatus.getIcon(), 0)
        }

        detailsStaxReason.text = transaction.fullStatus.getReason()
        transaction.fee?.let { binding.infoCard.detailsFee.text = Utils.formatAmount(it) }
    }

    private fun setVisibleDetails(transaction: StaxTransaction) {
        binding.primaryStatus.newBalance.visibility = if (shouldShowNewBalance(transaction)) VISIBLE else GONE
        binding.secondaryStatus.root.visibility = if (transaction.isSuccessful) GONE else VISIBLE
        binding.secondaryStatus.statusIcon.visibility = if (transaction.isFailed) VISIBLE else GONE
        binding.infoCard.reasonRow.visibility = if(transaction.isFailed) VISIBLE else GONE
        binding.infoCard.amountRow.visibility = if (transaction.isRecorded || transaction.transaction_type == HoverAction.BALANCE) GONE else VISIBLE
        binding.infoCard.recipientRow.visibility = if (transaction.isRecorded || transaction.transaction_type == HoverAction.BALANCE) GONE else VISIBLE
        binding.infoCard.recipInstitutionRow.visibility = if (transaction.isRecorded || transaction.transaction_type == HoverAction.BALANCE) GONE else VISIBLE
        binding.infoCard.confirmCodeRow.visibility = if (transaction.isRecorded || transaction.confirm_code.isNullOrBlank()) GONE else VISIBLE
        binding.infoCard.feeRow.visibility = if(transaction.fee == null) GONE else VISIBLE

    }

    private fun updateAction(action: HoverAction) {
        if (viewModel.transaction.value != null) {
            if (viewModel.transaction.value!!.isFailed)
                UIHelper.loadImage(requireContext(), getString(R.string.root_url) + action.from_institution_logo, binding.secondaryStatus.statusIcon)

            binding.secondaryStatus.statusText.apply {
                val content = viewModel.transaction.value!!.fullStatus.getStatusDetail(
                    action,
                    viewModel.messages.value?.last(),
                    viewModel.sms.value,
                    requireContext()
                )
                text = HtmlCompat.fromHtml(content, HtmlCompat.FROM_HTML_MODE_LEGACY)
                movementMethod = LinkMovementMethod.getInstance()
            }
        }
    }

    private fun updateAccount(account: Account) {
        binding.infoCard.detailsStaxAccount.text = account.name
    }

    private fun updateRecipient(contact: StaxContact?) {
        if (contact != null) {
            binding.infoCard.detailsRecipient.setContact(contact)
        }
        else binding.infoCard.detailsRecipient.setTitle(getString(R.string.self_choice))
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

    override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
        try {
            val d = RoundedBitmapDrawableFactory.create(requireContext().resources, bitmap)
            d.isCircular = true
            binding.secondaryStatus.statusIcon.setImageDrawable(d)
        } catch (e: IllegalStateException) {
            Timber.e(e)
        }
    }

    override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
        Timber.i("On bitmap failed")
    }

    override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
        Timber.i("On prepare load")
    }
}