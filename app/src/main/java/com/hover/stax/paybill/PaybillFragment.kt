package com.hover.stax.paybill

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.hover.sdk.actions.HoverAction
import com.hover.stax.R
import com.hover.stax.contacts.StaxContact
import com.hover.stax.databinding.FragmentPaybillBinding
import com.hover.stax.home.AbstractHoverCallerActivity
import com.hover.stax.transfers.AbstractFormFragment
import com.hover.stax.transfers.TransferViewModel
import com.hover.stax.utils.AnalyticsUtil
import com.hover.stax.utils.NavUtil
import com.hover.stax.utils.UIHelper
import com.hover.stax.utils.Utils
import com.hover.stax.views.AbstractStatefulInput
import com.hover.stax.views.StaxDialog
import org.koin.androidx.viewmodel.ext.android.getSharedViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import timber.log.Timber

class PaybillFragment : AbstractFormFragment(), PaybillIconsAdapter.IconSelectListener {

    private var _binding: FragmentPaybillBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: PaybillViewModel

    private val args: PaybillFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        abstractFormViewModel = getSharedViewModel<PaybillViewModel>()
        viewModel = abstractFormViewModel as PaybillViewModel

        _binding = FragmentPaybillBinding.inflate(inflater, container, false)
        channelsViewModel.setType(HoverAction.C2B)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        AnalyticsUtil.logAnalyticsEvent(getString(R.string.visit_screen, getString(R.string.visit_paybill)), requireActivity())
        init(binding.root)
    }

    override fun init(root: View) {
        super.init(binding.root)
        //TODO test updating of business
        if (args.updateBusiness) {
            binding.editCard.businessNoInput.setText(viewModel.businessNumber.value)
        }

        initListeners()
        startObservers(root)
        setTextWatchers()

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, backPressedCallback)
    }

    private fun initListeners() {
        setSaveBillCheckedChangeListener()
        setBusinessNoTouchListener()
        setContinueBtnClickListener()
    }

    override fun onContactSelected(requestCode: Int, contact: StaxContact) {}

    private fun setSaveBillCheckedChangeListener() = with(binding.saveBillLayout) {
        saveBill.setOnCheckedChangeListener { _, isChecked ->
            binding.saveBillLayout.saveBillCard.visibility =
                if (isChecked) View.VISIBLE else View.GONE
        }

        billIconLayout.iconLayout.setOnClickListener { showIconsChooser() }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setBusinessNoTouchListener() =
        binding.editCard.businessNoInput.editText.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                channelsViewModel.activeAccount.value?.id?.let {
                    NavUtil.navigate(findNavController(), PaybillFragmentDirections.actionPaybillFragmentToPaybillListFragment(it))
                } ?: Timber.e("Active account not set")
                true
            } else false
        }

    private fun setContinueBtnClickListener() = binding.fab.setOnClickListener {
        if (validates()) {
            if (viewModel.isEditing.value == true) {
                if (binding.saveBillLayout.saveBill.isChecked)
                    savePaybill()
                else
                    viewModel.setEditing(false)
            } else submitRequest()
        } else {
            UIHelper.flashMessage(requireActivity(), getString(R.string.toast_pleasefix))
        }
    }

    private fun savePaybill() {
        val selected = viewModel.selectedPaybill.value
        val hasChanges = when {
            selected == null -> false
            selected.name != binding.saveBillLayout.billNameInput.text -> true
            selected.logo != viewModel.iconDrawable.value -> true
            selected.recurringAmount == 0 && binding.saveBillLayout.amountCheckBox.isChecked -> true
            selected.recurringAmount != 0 && !binding.saveBillLayout.amountCheckBox.isChecked -> true
            else -> false
        }

        when {
            selected != null && selected.isSaved && hasChanges -> showUpdatePaybillConfirmation()
            selected != null && selected.isSaved && !hasChanges -> viewModel.setEditing(false)
            else -> {
                viewModel.savePaybill(
                    channelsViewModel.activeAccount.value,
                    binding.saveBillLayout.amountCheckBox.isChecked
                )
                UIHelper.flashMessage(requireActivity(), R.string.paybill_save_success) //TODO add to other language strings
                viewModel.setEditing(false)
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun startObservers(root: View) {
        super.startObservers(root)

        observeBill()
        observeAccountNo()
        observeAmount()
        observeNickname()
        observeIcon()
    }

    private fun observeBill() {
        viewModel.selectedPaybill.observe(viewLifecycleOwner) {
            it?.let {
                with(binding.editCard) {
                    businessNoInput.setText(it.businessNo)
                    accountNoInput.setText(it.accountNo ?: "")

                    if (it.recurringAmount != 0)
                        amountInput.setText(it.recurringAmount.toString())
                    else
                        amountInput.setText("")
                }

                updateSaveCardState(it)
            }
        }
    }

    private fun observeAmount() {
        viewModel.amount.observe(viewLifecycleOwner) {
            it?.let { binding.summaryCard.amountValue.text = Utils.formatAmount(it) }
        }
    }

    private fun observeAccountNo() {
        viewModel.accountNumber.observe(viewLifecycleOwner) {
            it?.let { binding.summaryCard.accountNo.text = it }
        }
    }

    private fun observeNickname() {
        viewModel.nickname.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) {
                binding.summaryCard.recipient.setTitle(it)
                binding.summaryCard.recipient.setSubtitle(viewModel.businessNumber.value)
            } else
                binding.summaryCard.recipient.setTitle(viewModel.businessNumber.value)
        }
    }

    private fun observeIcon() {
        viewModel.iconDrawable.observe(viewLifecycleOwner) {
            if (it != 0)
                binding.saveBillLayout.billIconLayout.billIcon.setImageDrawable(ContextCompat.getDrawable(requireContext(), it))
        }
    }

    private fun toggleMainContent(show: Boolean) {
        binding.saveBillLayout.cardSavePaybill.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun setTextWatchers() {
        with(binding.editCard) {
            businessNoInput.addTextChangedListener(businessNoWatcher)
            accountNoInput.addTextChangedListener(accountNoWatcher)
            amountInput.addTextChangedListener(amountWatcher)
        }

        binding.saveBillLayout.billNameInput.addTextChangedListener(nicknameWatcher)
    }

    private val amountWatcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
        override fun afterTextChanged(editable: Editable) {}
        override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            viewModel.setAmount(charSequence.toString().replace(",".toRegex(), ""))
        }
    }

    private val businessNoWatcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
        override fun afterTextChanged(editable: Editable) {}
        override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            viewModel.setBusinessNumber(charSequence.toString())
        }
    }

    private val accountNoWatcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
        override fun afterTextChanged(editable: Editable) {}
        override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            viewModel.setAccountNumber(charSequence.toString().replace(",".toRegex(), ""))
        }
    }

    private val nicknameWatcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
        override fun afterTextChanged(editable: Editable) {}
        override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            viewModel.setNickname(charSequence.toString())
        }
    }

    private fun validates(): Boolean {
        val businessNoError = viewModel.businessNoError()
        val accountNoError = viewModel.accountNoError()
        val amountError = viewModel.amountError()
        val nickNameError = viewModel.nameError()
        val saveBill = binding.saveBillLayout.saveBill.isChecked

        with(binding.editCard) {
            businessNoInput.setState(businessNoError,
                if (businessNoError == null) AbstractStatefulInput.SUCCESS else AbstractStatefulInput.ERROR)
            accountNoInput.setState(accountNoError,
                if (accountNoError == null) AbstractStatefulInput.SUCCESS else AbstractStatefulInput.ERROR)
            amountInput.setState(amountError,
                if (amountError == null) AbstractStatefulInput.SUCCESS else AbstractStatefulInput.ERROR)
        }

        if (saveBill) {
            binding.saveBillLayout.billNameInput.setState(nickNameError,
                if (nickNameError == null) AbstractStatefulInput.SUCCESS else AbstractStatefulInput.ERROR)
        }

        return businessNoError == null && accountNoError == null && amountError == null && (if (saveBill) nickNameError == null else true)
    }

    private fun showIconsChooser() = with(binding) {
        toggleMainContent(false)
        binding.fab.visibility = View.GONE

        with(paybillIconsLayout) {
            cardPaybillIcons.visibility = View.VISIBLE

            iconList.adapter = PaybillIconsAdapter(this@PaybillFragment)

            cardPaybillIcons.setOnClickIcon {
                cardPaybillIcons.visibility = View.GONE
                toggleMainContent(true)
            }
        }
    }

    private fun submitRequest() = with(channelsViewModel) {
        val actions = channelActions.value
        val channel = activeChannel.value
        val account = activeAccount.value

        val actionToRun = viewModel.selectedAction.value

        if (!actions.isNullOrEmpty() && channel != null && account != null)
            (requireActivity() as AbstractHoverCallerActivity).run(account, actionToRun?: actions.first(), viewModel.wrapExtras(), 0)
        else
            Timber.e("Request composition not complete; ${actions?.firstOrNull()}, $channel $account")
    }

    private fun resetViews() {
        binding.paybillIconsLayout.cardPaybillIcons.visibility = View.GONE
        binding.editCard.businessNoInput.binding.inputLayout.setEndIconDrawable(R.drawable.ic_chevron_right)
    }

    override fun onSelectIcon(id: Int) {
        viewModel.setIconDrawable(id)

        binding.paybillIconsLayout.cardPaybillIcons.visibility = View.GONE
        toggleMainContent(true)
    }

    private fun updateSaveCardState(paybill: Paybill) = with(binding.saveBillLayout) {
        if (paybill.isSaved) {
            saveBill.isChecked = true
            billNameInput.setText(paybill.name)
            amountCheckBox.isChecked = paybill.recurringAmount != 0
            if (amountCheckBox.isChecked) viewModel.setAmount(paybill.recurringAmount.toString())

            if (paybill.logo != 0) {
                billIconLayout.billIcon.setImageDrawable(ContextCompat.getDrawable(requireActivity(), paybill.logo))
                viewModel.setIconDrawable(paybill.logo)
            }
        } else {
            //helps maintain the correct state when checking if the saved paybill has changed
            saveBill.isChecked = false
            billNameInput.setText("")
            amountCheckBox.isChecked = false
            binding.saveBillLayout.cardSavePaybill.visibility = View.GONE
            viewModel.setIconDrawable(0)
        }
    }

    private fun showUpdatePaybillConfirmation() = viewModel.selectedPaybill.value?.let {
        dialog = StaxDialog(requireActivity())
            .setDialogTitle(getString(R.string.paybill_update_header))
            .setDialogMessage(getString(R.string.paybill_update_msg, it.name))
            .setNegButton(R.string.btn_cancel, null)
            .setPosButton(R.string.btn_update) { _ ->
                if (activity != null) {
                    viewModel.updatePaybill(it, binding.saveBillLayout.amountCheckBox.isChecked)
                    UIHelper.flashMessage(requireActivity(), R.string.paybill_update_success)
                    viewModel.setEditing(false)
                }
            }
        dialog!!.showIt()
    }

    private val backPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (viewModel.isEditing.value == false)
                viewModel.setEditing(true)
            else
                findNavController().popBackStack()
        }

    }
}
