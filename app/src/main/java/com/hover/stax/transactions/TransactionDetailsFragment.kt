package com.hover.stax.transactions

import android.annotation.SuppressLint
import android.content.Intent
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
import com.hover.stax.contacts.StaxContact
import com.hover.stax.databinding.FragmentTransactionBinding
import com.hover.stax.home.MainActivity
import com.hover.stax.home.SDKIntent
import com.hover.stax.utils.AnalyticsUtil.logAnalyticsEvent
import com.hover.stax.utils.AnalyticsUtil.logErrorAndReportToFirebase
import com.hover.stax.utils.AnalyticsUtil.logFailedAction
import com.hover.stax.utils.DateUtils.humanFriendlyDateTime
import com.hover.stax.utils.UIHelper
import com.hover.stax.utils.Utils
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import org.json.JSONException
import org.json.JSONObject
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber


class TransactionDetailsFragment : DialogFragment(), Target {

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
        binding.transactionDetailsCard.setOnClickIcon { this.dismiss() }
        binding.primaryStatus.viewLogText.setOnClickListener { showUSSDLog() }
        with(binding.infoCard.detailsStaxUuid.content) { setOnClickListener { Utils.copyToClipboard(this.text.toString(), requireContext()) } }
        with(binding.infoCard.detailsServiceId.content) { setOnClickListener { Utils.copyToClipboard(this.text.toString(), requireContext()) } }
    }

    private fun showUSSDLog() {
        (requireActivity() as MainActivity).showUSSDLogBottomSheet(uuid!!)
    }

    private fun startObservers() {
        viewModel.transaction.observe(viewLifecycleOwner) { showTransaction(it) }
        viewModel.action.observe(viewLifecycleOwner) { it?.let { showActionDetails(it) } }
        viewModel.contact.observe(viewLifecycleOwner) { updateRecipient(it) }
        viewModel.actionAndChannelPair.observe(viewLifecycleOwner) {Timber.i("${it.first.public_id} in actionAndChannel loaded")}
    }

    private fun setToPopupDesign() {
        binding.ftMainBg.setBackgroundColor(ContextCompat.getColor(requireActivity(), R.color.colorPrimary))
        binding.transactionDetailsCard.setIcon(R.drawable.ic_close_white)
        binding.transactionDetailsCard.setTitle(R.drawable.ic_close_white)
        binding.transactionDetailsCard.makeFlatView()
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
        this.dismiss()
        viewModel.transaction.value?.let {
            val tType = if(it.isRecorded) getString(R.string.bounty) else it.transaction_type
            logAnalyticsEvent(getString(R.string.clicked_retry_session, tType), requireContext())
            val intent = SDKIntent.create(it.action_id, requireContext())
            callSDKSafely(intent, it.action_id)
        }
    }

    private fun retryTransactionClicked(transaction: StaxTransaction, retryButton: TextView) {
        retryButton.setOnClickListener {
            updateRetryCounter(transaction.action_id)
            this.dismiss()
            val mainActivity  = (requireActivity() as MainActivity)
            if (transaction.isBalanceType) callSession(transaction)
            else mainActivity.navigateTransferAutoFill(transaction.transaction_type, transaction.uuid)
        }
    }

    private fun callSession(transaction: StaxTransaction) {
        val actionAndChannelPair = viewModel.actionAndChannelPair.value!!
        val intent = SDKIntent.create(transaction, actionAndChannelPair, requireContext())
        callSDKSafely(intent, transaction.action_id)
    }

    private fun callSDKSafely(intent: Intent, actionId: String) {
        try {
            val mainActivity  = (requireActivity() as MainActivity)
            mainActivity.sdkLauncherForSingleBalance.launch(intent)
        }
        catch (e : Exception) {
            logFailedAction(actionId, requireActivity())
            Timber.e(e)
        }
    }

    private fun shouldShowNewBalance(transaction: StaxTransaction): Boolean {
        return !transaction.isBalanceType && !transaction.balance.isNullOrEmpty() && transaction.isSuccessful
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
        }

        setDetailsData(transaction)
        setVisibleDetails(transaction)
        updateDetailsRequiringAction(viewModel.action.value, viewModel.transaction.value)
        updateStatus(viewModel.action.value, transaction)
    }

    private fun setDetailsData(transaction: StaxTransaction) {
        if (shouldShowNewBalance(transaction)) {
            binding.primaryStatus.newBalance.apply {
                text = getString(R.string.new_balance, transaction.displayBalance)
                visibility = VISIBLE
            }
        }
        binding.infoCard.detailsRecipientLabel.setText(if (transaction.transaction_type == HoverAction.RECEIVE) R.string.sender_label else R.string.recipient_label)
        binding.infoCard.detailsAmount.text = transaction.displayAmount
        binding.infoCard.detailsDate.text = humanFriendlyDateTime(transaction.updated_at)
        binding.infoCard.detailsServiceId.content.text = transaction.confirm_code
        binding.infoCard.detailsStaxUuid.content.text = transaction.uuid
        binding.infoCard.detailsStaxStatus.apply {
            text = transaction.fullStatus.getPlainTitle(requireContext())
            setCompoundDrawablesWithIntrinsicBounds(0, 0, transaction.fullStatus.getIcon(), 0)
        }
        binding.infoCard.detailsStaxReason.text = transaction.fullStatus.getReason()
        transaction.fee?.let { binding.infoCard.detailsFee.text = Utils.formatAmount(it) }
    }

    private fun setVisibleDetails(transaction: StaxTransaction) {
        binding.infoCard.reasonRow.visibility = if (transaction.isFailed) VISIBLE else GONE
        binding.infoCard.amountRow.visibility = if (transaction.isRecorded || transaction.transaction_type == HoverAction.BALANCE) GONE else VISIBLE
        binding.infoCard.recipientRow.visibility = if (transaction.isRecorded || transaction.transaction_type == HoverAction.BALANCE) GONE else VISIBLE
        binding.infoCard.recipAccountRow.visibility = if (transaction.isRecorded || transaction.transaction_type == HoverAction.BALANCE) GONE else VISIBLE
        binding.infoCard.serviceIdRow.visibility = if (transaction.isRecorded || transaction.confirm_code.isNullOrBlank()) GONE else VISIBLE
        binding.infoCard.feeRow.visibility = if (transaction.fee == null) GONE else VISIBLE
    }

    private fun updateDetailsRequiringAction(action: HoverAction?, transaction: StaxTransaction?) {
        if (action != null && transaction != null) {
            binding.infoCard.detailsStaxType.text = transaction.fullStatus.getDisplayType(requireContext(), action)
            binding.infoCard.detailsStaxAccount.text = action.from_institution_name
            binding.infoCard.detailsFeeLabel.text = getString(R.string.transaction_fee, action.from_institution_name)
        }
    }

    private fun showActionDetails(action: HoverAction) {
        if (!isFullScreen) {
            binding.transactionDetailsCard.setTitle(viewModel.transaction.value?.generateLongDescription(action, viewModel.contact.value, requireContext()))
        }
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

            binding.primaryStatus.statusText.text = textValue
            binding.primaryStatus.statusIcon.setImageResource(transaction.fullStatus.getIcon())
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
                if (transaction.isFailed) action?.let { UIHelper.loadPicasso(getString(R.string.root_url) + it.from_institution_logo, this) }
                else binding.secondaryStatus.statusIcon.visibility = GONE
            }
        }
    }

    private fun updateRecipient(contact: StaxContact?) {
        if (contact != null) {
            if (!isFullScreen && viewModel.action.value != null)
                binding.transactionDetailsCard.setTitle(viewModel.transaction.value?.generateLongDescription(viewModel.action.value, contact, requireContext()))
            binding.infoCard.detailsRecipient.setContact(contact)
        } else binding.infoCard.detailsRecipient.setTitle(getString(R.string.self_choice))
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