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
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.core.os.bundleOf
import androidx.core.text.HtmlCompat
import androidx.fragment.app.DialogFragment
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
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import org.json.JSONException
import org.json.JSONObject
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber


class TransactionDetailsFragment : DialogFragment(), Target{

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
        viewModel.setTransaction(uuid!!)
        logView()

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
        setListeners();
    }

    private fun setListeners() {
        binding.transactionDetailsCard.setOnClickIcon { this.dismiss() }
        binding.primaryStatus.viewLogText.setOnClickListener { showUSSDLog() }
        with(binding.infoCard.detailsStaxUuid.content) { setOnClickListener { Utils.copyToClipboard(this.text.toString(), requireContext()) } }
        with(binding.infoCard.confirmCodeCopy.content) { setOnClickListener { Utils.copyToClipboard(this.text.toString(), requireContext()) } }
    }

    private fun showUSSDLog() {
        (requireActivity() as AbstractHoverCallerActivity).showUSSDLogBottomSheet(uuid!!)
    }

    private fun startObservers() {
        viewModel.transaction.observe(viewLifecycleOwner) { showTransaction(it) }
        viewModel.action.observe(viewLifecycleOwner) { it?.let { updateAction(it) } }
        viewModel.contact.observe(viewLifecycleOwner) { updateRecipient(it) }
        viewModel.account.observe(viewLifecycleOwner) { it?.let { updateAccount(it) } }
    }

    private fun setToPopupDesign() {
        binding.ftMainBg.setBackgroundColor(ContextCompat.getColor(requireActivity(), R.color.colorPrimary))
        binding.transactionDetailsCard.setIcon(R.drawable.ic_close_white)
        binding.transactionDetailsCard.setTitle(R.drawable.ic_close_white)
        binding.transactionDetailsCard.makeFlatView()
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
                this.dismiss()
            }
        }
    }

    private fun setupContactSupportButton(id: String, contactSupportTextView: TextView) {
        contactSupportTextView.setText(R.string.email_support)
        contactSupportTextView.setOnClickListener {
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

    private fun setDetailsData(transaction: StaxTransaction) {
        if(shouldShowNewBalance(transaction))
            binding.primaryStatus.newBalance.text = getString(R.string.new_balance, transaction.displayBalance)
        binding.primaryStatus.statusText.text = generateTitle(transaction)
        binding.primaryStatus.statusIcon.setImageResource(transaction.fullStatus.getIcon())

        binding.infoCard.detailsRecipientLabel.setText(if (transaction.transaction_type == HoverAction.RECEIVE) R.string.sender_label else R.string.recipient_label)
        binding.infoCard.detailsAmount.text = transaction.displayAmount
        binding.infoCard.detailsDate.text = humanFriendlyDateTime(transaction.updated_at)
        binding.infoCard.confirmCodeCopy.content.text = transaction.confirm_code
        binding.infoCard.detailsStaxUuid.content.text = transaction.uuid
        binding.infoCard.detailsStaxStatus.apply {
            text = transaction.fullStatus.getPlainTitle(requireContext())
            setCompoundDrawablesWithIntrinsicBounds(0, 0, transaction.fullStatus.getIcon(), 0)
        }

        binding.infoCard.detailsStaxReason.text = transaction.fullStatus.getReason()
        transaction.fee?.let{binding.infoCard.detailsFee.text = Utils.formatAmount(it)}
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
                UIHelper.loadPicasso(getString(R.string.root_url) + action.from_institution_logo, this)

            binding.secondaryStatus.statusText.apply {
                val content = viewModel.transaction.value!!.fullStatus.getStatusDetail(action, viewModel.messages.value?.last(), viewModel.sms.value, requireContext())
                text = HtmlCompat.fromHtml(content, HtmlCompat.FROM_HTML_MODE_LEGACY)
                movementMethod = LinkMovementMethod.getInstance()
            }
        }
        binding.infoCard.detailsStaxType.text = viewModel.transaction.value?.fullStatus?.getDisplayType(requireContext(), action)
        binding.infoCard.detailsFeeLabel.text = getString(R.string.transaction_fee, action.from_institution_name)
//        if () binding.infoCard.detailsInstitution.setSubtitle(action.from_institution_name)
    }

    private fun updateAccount(account: Account) {
        binding.infoCard.detailsStaxAccount.text = account.name
    }

    private fun updateRecipient(contact: StaxContact?) {
        if (contact != null) {
            if (!isFullScreen && viewModel.action.value != null)
                binding.transactionDetailsCard.setTitle(viewModel.transaction.value?.generateLongDescription(viewModel.action.value, contact, requireContext()))
            binding.infoCard.detailsRecipient.setContact(contact)
        }
        else binding.infoCard.detailsRecipient.setTitle(getString(R.string.self_choice))
    }

    private fun retryBountyClicked() {
        this.dismiss()

        viewModel.action.value?.let {
            (requireActivity() as MainActivity).makeRegularCall(it, R.string.clicked_retry_bounty_session)
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

    private fun logView() {
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
        } catch (e: IllegalStateException) { Timber.e(e) }
    }

    override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) { Timber.i("On bitmap failed") }
    override fun onPrepareLoad(placeHolderDrawable: Drawable?) { Timber.i("On prepare load") }
}