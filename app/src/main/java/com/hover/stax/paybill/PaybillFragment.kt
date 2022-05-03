package com.hover.stax.paybill

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputEditText
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
        initView()
        startObservers(root)
    }

    private fun initView() {
        initListeners()
        setUpIcons()
        binding.editCard.businessNoInput.binding?.root?.setEndIconDrawable(R.drawable.ic_chevron_right)
    }

    private fun initListeners() {
        setInputListeners()
        setBusinessNoTouchListener()
        setContinueBtnClickListener()
    }

    private fun setInputListeners() = with(binding.saveCard) {
        setInputListener(binding.editCard.accountNoInput, { s -> viewModel.setAccountNumber(s) }, { viewModel.accountNoError() })
        setInputListener(binding.editCard.amountInput, { s -> viewModel.setAmount(s) }, { viewModel.amountError() })
        setInputListener(billNameInput, { s -> viewModel.setNickname(s) }, { viewModel.nameError() })
        saveBill.setOnCheckedChangeListener { _, isChecked -> viewModel.setSave(isChecked) }
        saveAmount.setOnCheckedChangeListener { _, isChecked -> viewModel.setSaveAmount(isChecked) }
        billIcon.setOnClickListener { toggleIconChooser(true) }
    }

    private fun setInputListener(input: StaxTextInput, setFun: (String) -> Unit, errorMsg: () -> String?) {
        input.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) setFun((v as TextInputEditText).text.toString())
            setInputState(hasFocus, input, errorMsg())
        }
    }

    override fun onContactSelected(requestCode: Int, contact: StaxContact) {}

    private fun setBusinessNoTouchListener() =
        binding.editCard.businessNoInput.setOnClickListener {
            findNavController().navigate(R.id.paybillListFragment)
        }

    private fun setContinueBtnClickListener() = binding.fab.setOnClickListener {
        if (validates())
            performFabAction()
        else
            UIHelper.flashMessage(requireActivity(), getString(R.string.toast_pleasefix))
    }

    private fun setUpIcons() = with(binding.paybillIconsLayout) {
        iconList.adapter = PaybillIconsAdapter(this@PaybillFragment)
        root.setOnClickIcon { toggleIconChooser(false) }
        cardPaybillIcons.visibility = View.GONE
    }

    private fun toggleIconChooser(show: Boolean) = with(binding) {
        binding.saveCard.root.visibility = if (show) View.GONE else View.VISIBLE
        binding.fab.visibility = if (show) View.GONE else View.VISIBLE
        binding.paybillIconsLayout.cardPaybillIcons.visibility = if (show) View.VISIBLE else View.GONE
    }

    override fun onSelectIcon(id: Int) {
        viewModel.setIconDrawable(id)
        binding.paybillIconsLayout.cardPaybillIcons.visibility = View.GONE
        binding.saveCard.root.visibility = View.VISIBLE
        binding.fab.visibility = View.VISIBLE
    }

    private fun performFabAction() {
        if (viewModel.isEditing.value == true) goToConfirm()
        else startSession()
    }

    private fun goToConfirm() {
        if (viewModel.saveBill.value!!) savePaybill()
        else viewModel.setEditing(false)
    }

    private fun savePaybill() {
        when {
            viewModel.hasEditedSaved() -> showUpdatePaybillConfirmation()
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
        observeSave()
    }

    private fun observePayWith() {
        accountsViewModel.activeAccount.observe(viewLifecycleOwner) { account ->
            account?.let { binding.summaryCard.accountValue.setTitle(it.toString()) }
            viewModel.getSavedPaybills(account.id)
        }
    }

    private fun observeActions() {
        accountsViewModel.channelActions.observe(viewLifecycleOwner) {
            if (accountsViewModel.errorCheck() != null)
                payWithDropdown.setState(accountsViewModel.errorCheck(), AbstractStatefulInput.ERROR)
            else
                payWithDropdown.setState(null, AbstractStatefulInput.SUCCESS)
        }
        actionSelectViewModel.activeAction.observe(viewLifecycleOwner) { it?.let {
            binding.summaryCard.recipient.setTitle(it.to_institution_name) }}
    }

    private fun observeBill() {
        viewModel.selectedPaybill.observe(viewLifecycleOwner) {
            actionSelectViewModel.setActiveAction(it?.actionId) }

        viewModel.businessNumber.observe(viewLifecycleOwner) {
            updateBiz(viewModel.businessName.value, it) }

        viewModel.businessName.observe(viewLifecycleOwner) {
            updateBiz(it, viewModel.businessNumber.value) }
    }

    private fun updateBiz(name: String?, no: String?) {
        binding.editCard.businessNoInput.setMutlipartText(name, no)
        binding.summaryCard.recipient.setContent(name, no)
    }

    private fun observeAmount() {
        viewModel.amount.observe(viewLifecycleOwner) {
            binding.editCard.amountInput.setText(it)
            binding.summaryCard.amountValue.text = Utils.formatAmount(it)
        }
    }

    private fun observeAccountNo() {
        viewModel.accountNumber.observe(viewLifecycleOwner) {
            binding.editCard.accountNoInput.setText(it)
            binding.summaryCard.accountNo.setText(it)
        }
    }

    private fun observeSave() {
        viewModel.saveBill.observe(viewLifecycleOwner) {
            binding.saveCard.ch
            binding.saveCard.saveDetails.visibility = if (it == true) View.VISIBLE else View.GONE
        }
        observeIcon()
        observeName()
    }

    private fun observeName() {
        viewModel.nickname.observe(viewLifecycleOwner) {
            binding.saveCard.billNameInput.setText(it)
            if (it != null) binding.summaryCard.nameValue.text = it
            binding.summaryCard.nameLabel.visibility = if (it == null) View.GONE else View.VISIBLE
            binding.summaryCard.nameValue.visibility = if (it == null) View.GONE else View.VISIBLE
        }
    }

    private fun observeIcon() {
        viewModel.iconDrawable.observe(viewLifecycleOwner) {
            if (it != 0)
                binding.saveCard.billIcon.setImageDrawable(
                    ContextCompat.getDrawable(requireContext(), it))
        }
    }

    override fun showEdit(isEditing: Boolean) {
        super.showEdit(isEditing)
        binding.saveCard.root.visibility = if (isEditing) View.VISIBLE else View.GONE
    }

    private fun validates(): Boolean {
        viewModel.setAccountNumber(binding.editCard.accountNoInput.text)
        viewModel.setAmount(binding.editCard.amountInput.text)
        viewModel.setNickname(binding.saveCard.billNameInput.text)

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
        binding.saveCard.billNameInput.setState(nickNameError,
            if (nickNameError == null && viewModel.saveBill.value!!) AbstractStatefulInput.SUCCESS else if (nickNameError != null) AbstractStatefulInput.ERROR else AbstractStatefulInput.NONE)

        return payWithError == null && businessNoError == null && accountNoError == null && amountError == null && nickNameError == null
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
}
