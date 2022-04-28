package com.hover.stax.paybill

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.hover.sdk.actions.HoverAction
import com.hover.stax.R
import com.hover.stax.contacts.StaxContact
import com.hover.stax.databinding.FragmentPaybillBinding
import com.hover.stax.home.AbstractHoverCallerActivity
import com.hover.stax.transfers.AbstractFormFragment
import com.hover.stax.utils.AnalyticsUtil
import com.hover.stax.utils.UIHelper
import com.hover.stax.utils.Utils
import com.hover.stax.views.AbstractStatefulInput
import com.hover.stax.views.StaxDialog
import com.hover.stax.views.StaxTextInput
import okhttp3.internal.notify
import org.koin.androidx.viewmodel.ext.android.getSharedViewModel
import timber.log.Timber

class PaybillFragment : AbstractFormFragment(), PaybillIconsAdapter.IconSelectListener {

    private var _binding: FragmentPaybillBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: PaybillViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        abstractFormViewModel = getSharedViewModel<PaybillViewModel>()
        viewModel = abstractFormViewModel as PaybillViewModel

        _binding = FragmentPaybillBinding.inflate(inflater, container, false)
        accountsViewModel.setType(HoverAction.C2B)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        AnalyticsUtil.logAnalyticsEvent(getString(R.string.visit_screen, getString(R.string.visit_paybill)), requireActivity())
        init(binding.root)
    }

    override fun init(root: View) {
        super.init(root)
        binding.paybillIconsLayout.cardPaybillIcons.visibility = View.GONE
        binding.editCard.businessNoInput.binding?.inputLayout?.setEndIconDrawable(R.drawable.ic_chevron_right)

        initView()
        initListeners()
        startObservers(root)
    }

    private fun initView() {
        binding.editCard.businessNoInput.setMutlipartText(viewModel.selectedPaybill.value?.name, viewModel.selectedPaybill.value?.businessNo)
        binding.editCard.accountNoInput.setText(viewModel.accountNumber.value)
        binding.editCard.amountInput.setText(viewModel.amount.value)
        binding.saveBillLayout.saveBill.isChecked = viewModel.saveBill.value!!
        binding.saveBillLayout.billNameInput.setText(viewModel.nickname.value)
        binding.saveBillLayout.saveAmount.isChecked = viewModel.saveAmount.value!!
//        binding.saveBillLayout.billIconLayout.billIcon.setImageDrawable(viewModel.iconDrawable.value?.let {
//            ContextCompat.getDrawable(requireContext(), it)
//        })
        binding.summaryCard.accountValue.setTitle(accountsViewModel.activeAccount.value?.name)
        binding.summaryCard.recipient.setContent(actionSelectViewModel.activeAction.value?.to_institution_name, viewModel.selectedPaybill.value?.businessNo)
    }

    private fun initListeners() {
        setTextWatchers()
        setBusinessNoTouchListener()
        setSaveListeners()
        setContinueBtnClickListener()
    }

    override fun onContactSelected(requestCode: Int, contact: StaxContact) {}

    private fun setSaveListeners() = with(binding.saveBillLayout) {
        saveBill.setOnCheckedChangeListener { _, isChecked -> viewModel.setSave(isChecked) }
        saveAmount.setOnCheckedChangeListener { _, isChecked -> viewModel.setSaveAmount(isChecked) }

        billIconLayout.iconLayout.setOnClickListener { showIconsChooser() }
    }

    private fun setBusinessNoTouchListener() =
        binding.editCard.businessNoInput.setOnClickListener {
            findNavController().navigate(R.id.paybillListFragment)
        }

    private fun setContinueBtnClickListener() = binding.fab.setOnClickListener {
        if (validates()) {
            if (viewModel.isEditing.value == true) {
                if (viewModel.saveBill.value!!)
                    savePaybill()
                else
                    viewModel.setEditing(false)
            } else startSession()
        } else {
            UIHelper.flashMessage(requireActivity(), getString(R.string.toast_pleasefix))
        }
    }

    private fun savePaybill() {
        val hasEditedSaved = viewModel.hasEditedSaved()
        Timber.e("updating existing? %s", hasEditedSaved)
        when {
            hasEditedSaved -> showUpdatePaybillConfirmation()
            viewModel.selectedPaybill.value?.isSaved == true -> viewModel.setEditing(false)
            else -> {
                viewModel.savePaybill(accountsViewModel.activeAccount.value, actionSelectViewModel.activeAction.value)
                UIHelper.flashMessage(requireActivity(), R.string.paybill_save_success)
            }
        }
    }

    override fun startObservers(root: View) {
        super.startObservers(root)

        observePayWith()
        observeActions()
        observeBill()
        observeAccountNo()
        observeAmount()
        observeName()
        observeIcon()
    }

    private fun observePayWith() {
        accountsViewModel.activeAccount.observe(viewLifecycleOwner) { account ->
            account?.let { binding.summaryCard.accountValue.setTitle(it.toString()) }
            viewModel.getSavedPaybills(account.id)
        }
    }

    private fun observeActions() {
        accountsViewModel.channelActions.observe(viewLifecycleOwner) { actions ->
            if (accountsViewModel.errorCheck() != null)
                payWithDropdown.setState(accountsViewModel.errorCheck(), AbstractStatefulInput.ERROR)
            else
                payWithDropdown.setState(null, AbstractStatefulInput.SUCCESS)
        }
        actionSelectViewModel.activeAction.observe(viewLifecycleOwner) { it?.let { binding.summaryCard.recipient.setTitle(it.to_institution_name) }}
    }

    private fun observeBill() {
        viewModel.selectedPaybill.observe(viewLifecycleOwner) {
            it?.let {
                binding.editCard.businessNoInput.setMutlipartText(it.name, it.businessNo)
                binding.summaryCard.recipient.setContent(actionSelectViewModel.activeAction.value?.to_institution_name, it.businessNo)

                // These prevent blank value from overwriting for unknown reasons
                binding.editCard.accountNoInput.setText(viewModel.accountNumber.value)
                binding.editCard.amountInput.setText(viewModel.amount.value)
                binding.saveBillLayout.billNameInput.setText(viewModel.nickname.value)
                binding.saveBillLayout.saveBill.isChecked = it.isSaved
                binding.saveBillLayout.saveAmount.isChecked = it.recurringAmount != 0
                if (it.logo != 0)
                    binding.saveBillLayout.billIconLayout.billIcon.setImageDrawable(ContextCompat.getDrawable(requireContext(), it.logo))
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
            it?.let { binding.summaryCard.accountNo.setContent(viewModel.nickname.value, it) }
        }
    }

    private fun observeName() {
        viewModel.nickname.observe(viewLifecycleOwner) {
            it?.let { binding.summaryCard.accountNo.setContent(it, viewModel.accountNumber.value) }
        }
    }

    private fun observeIcon() {
        viewModel.iconDrawable.observe(viewLifecycleOwner) {
            if (it != 0)
                binding.saveBillLayout.billIconLayout.billIcon.setImageDrawable(ContextCompat.getDrawable(requireContext(), it))
        }
    }

    override fun showEdit(isEditing: Boolean) {
        super.showEdit(isEditing)
        binding.saveBillLayout.cardSavePaybill.visibility = if (isEditing) View.VISIBLE else View.GONE
    }

    private fun setTextWatchers() {
        watchTextInput(binding.editCard.accountNoInput, accountNoWatcher) { viewModel.accountNoError() }
        watchTextInput(binding.editCard.amountInput, amountWatcher) { viewModel.amountError() }
        watchTextInput(binding.saveBillLayout.billNameInput, nicknameWatcher) { viewModel.nameError() }
    }

    private fun watchTextInput(input: StaxTextInput, watcher: TextWatcher, errorMsg: () -> String?) {
        input.addTextChangedListener(watcher)
        input.setOnFocusChangeListener { _, hasFocus -> setInputState(hasFocus, input, errorMsg()) }
    }

    private val amountWatcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
        override fun afterTextChanged(editable: Editable) {}
        override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            if (charSequence.isEmpty()) return
            viewModel.setAmount(charSequence.toString().replace(",".toRegex(), ""))
        }
    }

    private val accountNoWatcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
        override fun afterTextChanged(editable: Editable) {}
        override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            if (charSequence.isEmpty()) return
            viewModel.setAccountNumber(charSequence.toString().replace(",".toRegex(), ""))
        }
    }

    private val nicknameWatcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
        override fun afterTextChanged(editable: Editable) {}
        override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            if (charSequence.isEmpty()) return
            viewModel.setNickname(charSequence.toString())
        }
    }

    private fun validates(): Boolean {
        val payWithError = accountsViewModel.errorCheck()
        val businessNoError = viewModel.businessNoError()
        val accountNoError = viewModel.accountNoError()
        val amountError = viewModel.amountError()
        val nickNameError = viewModel.nameError()

        with(binding.editCard) {
            businessNoInput.setState(businessNoError,
                if (businessNoError == null) AbstractStatefulInput.SUCCESS else AbstractStatefulInput.ERROR)
            accountNoInput.setState(accountNoError,
                if (accountNoError == null) AbstractStatefulInput.SUCCESS else AbstractStatefulInput.ERROR)
            amountInput.setState(amountError,
                if (amountError == null) AbstractStatefulInput.SUCCESS else AbstractStatefulInput.ERROR)
        }
        binding.saveBillLayout.billNameInput.setState(nickNameError,
            if (nickNameError == null && viewModel.saveBill.value!!) AbstractStatefulInput.SUCCESS else if (nickNameError != null) AbstractStatefulInput.ERROR else AbstractStatefulInput.NONE)

        return payWithError == null && businessNoError == null && accountNoError == null && amountError == null && nickNameError == null
    }

    private fun showIconsChooser() = with(binding) {
        binding.saveBillLayout.cardSavePaybill.visibility = View.GONE
        binding.fab.visibility = View.GONE

        with(paybillIconsLayout) {
            cardPaybillIcons.visibility = View.VISIBLE
            iconList.adapter = PaybillIconsAdapter(this@PaybillFragment)
        }
    }

    private fun startSession() = with(accountsViewModel) {
        val actions = channelActions.value
        val account = activeAccount.value
        val actionToRun = actionSelectViewModel.activeAction.value

        if (!actions.isNullOrEmpty() && account != null)
            (requireActivity() as AbstractHoverCallerActivity).run(account, actionToRun?: actions.first(), viewModel.wrapExtras(), 0)
        else
            Timber.e("Request composition not complete; ${actions?.firstOrNull()}, $account")
    }

    override fun onSelectIcon(id: Int) {
        viewModel.setIconDrawable(id)
        binding.paybillIconsLayout.cardPaybillIcons.visibility = View.GONE
        binding.saveBillLayout.cardSavePaybill.visibility = View.VISIBLE
        binding.fab.visibility = View.VISIBLE
    }

    private fun showUpdatePaybillConfirmation() = viewModel.selectedPaybill.value?.let {
        dialog = StaxDialog(requireActivity())
            .setDialogTitle(getString(R.string.paybill_update_header))
            .setDialogMessage(getString(R.string.paybill_update_msg, it.name))
            .setNegButton(R.string.btn_cancel, null)
            .setPosButton(R.string.btn_update) { _ ->
                if (activity != null) {
                    viewModel.updatePaybill(it)
                    UIHelper.flashMessage(requireActivity(), R.string.paybill_update_success)
                    viewModel.setEditing(false)
                }
            }
        dialog!!.showIt()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        binding.editCard.amountInput.onDetachedFromWindow()
        binding.editCard.accountNoInput.onDetachedFromWindow()
        binding.saveBillLayout.billNameInput.onDetachedFromWindow()
    }
}
