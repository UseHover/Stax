/*
 * Copyright 2022 Stax
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hover.stax.paybill

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputEditText
import com.hover.sdk.actions.HoverAction
import com.hover.stax.R
import com.hover.stax.contacts.StaxContact
import com.hover.stax.databinding.FragmentPaybillBinding
import com.hover.stax.hover.AbstractHoverCallerActivity
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

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        abstractFormViewModel = getSharedViewModel<PaybillViewModel>()
        viewModel = abstractFormViewModel as PaybillViewModel
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPaybillBinding.inflate(inflater, container, false)
        accountsViewModel.setType(HoverAction.BILL)
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
    }

    private fun setInputListeners() = with(binding.saveCard) {
        setInputListener(binding.editCard.accountNoInput, { s -> viewModel.setAccountNumber(s) }, { viewModel.accountNoError() })
        setInputListener(binding.editCard.amountInput, { s -> viewModel.setAmount(s) }, { viewModel.amountError() })
        setInputListener(billNameInput, { s -> viewModel.setNickname(s) }, { viewModel.nameError() })
        saveBill.setOnCheckedChangeListener { _, isChecked -> viewModel.setSave(isChecked) }
        saveAmount.setOnCheckedChangeListener { _, isChecked -> viewModel.setSaveAmount(isChecked) }
        billIcon.setOnClickListener { toggleIconChooser(true) }
    }

    private fun setInputListener(
        input: StaxTextInput,
        setFun: (String) -> Unit,
        errorMsg: () -> String?
    ) {
        input.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) setFun((v as TextInputEditText).text.toString())
            setInputState(hasFocus, input, errorMsg())
        }
    }

    override fun onContactSelected(contact: StaxContact) {}

    private fun setBusinessNoTouchListener() =
        binding.editCard.businessNoInput.setOnClickListener {
            findNavController().navigate(R.id.paybillListFragment)
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

    override fun onFinishForm() {
        if (viewModel.saveBill.value!!) savePaybill()
        else viewModel.setEditing(false)
    }

    private fun savePaybill() {
        when {
            viewModel.hasEditedSaved() -> showUpdatePaybillConfirmation()
            viewModel.selectedPaybill.value?.isSaved == true -> viewModel.setEditing(false)
            else -> {
                viewModel.savePaybill(accountsViewModel.activeAccount.value, actionSelectViewModel.activeAction.value)
                UIHelper.flashAndReportMessage(requireActivity(), R.string.paybill_save_success)
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
            account?.let {
                binding.summaryCard.accountValue.setTitle(it.toString())
                viewModel.getSavedPaybills(account.id)
            }
        }
    }

    private fun observeActions() {
        accountsViewModel.institutionActions.observe(viewLifecycleOwner) {
            val err = accountsViewModel.errorCheck()
            payWithDropdown.setState(err, if (err == null) AbstractStatefulInput.SUCCESS else AbstractStatefulInput.ERROR)
        }

        actionSelectViewModel.activeAction.observe(viewLifecycleOwner) {
            it?.let {
                binding.summaryCard.recipient.setTitle(it.to_institution_name)
            }
        }
    }

    private fun observeBill() {
        viewModel.selectedPaybill.observe(viewLifecycleOwner) {
            actionSelectViewModel.setActiveAction(it?.actionId)
            binding.saveCard.saveBill.isChecked = it?.isSaved == true
            binding.saveCard.saveAmount.isChecked = it?.recurringAmount != 0
        }

        viewModel.businessNumber.observe(viewLifecycleOwner) {
            updateBiz(viewModel.businessName.value, it)
        }

        viewModel.businessName.observe(viewLifecycleOwner) {
            updateBiz(it, viewModel.businessNumber.value)
        }
    }

    private fun updateBiz(name: String?, no: String?) {
        binding.editCard.businessNoInput.setMultipartText(name, no)
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
            binding.summaryCard.accountNo.text = it
        }
    }

    private fun observeSave() {
        viewModel.saveBill.observe(viewLifecycleOwner) {
            binding.saveCard.saveDetails.visibility = if (it == true) View.VISIBLE else View.GONE
        }
        observeIcon()
        observeName()
    }

    private fun observeName() {
        viewModel.nickname.observe(viewLifecycleOwner) {
            binding.saveCard.billNameInput.setText(it)
            if (!it.isNullOrEmpty()) binding.summaryCard.nameValue.text = it
            binding.summaryCard.nameLabel.visibility = if (it.isNullOrEmpty()) View.GONE else View.VISIBLE
            binding.summaryCard.nameValue.visibility = if (it.isNullOrEmpty()) View.GONE else View.VISIBLE
        }
    }

    private fun observeIcon() {
        viewModel.iconDrawable.observe(viewLifecycleOwner) {
            if (it != 0)
                binding.saveCard.billIcon.setImageDrawable(
                    ContextCompat.getDrawable(requireContext(), it)
                )
        }
    }

    override fun showEdit(isEditing: Boolean) {
        super.showEdit(isEditing)
        binding.saveCard.root.visibility = if (isEditing) View.VISIBLE else View.GONE
    }

    override fun validates(): Boolean {
        viewModel.setAccountNumber(binding.editCard.accountNoInput.text)
        viewModel.setAmount(binding.editCard.amountInput.text)
        viewModel.setNickname(binding.saveCard.billNameInput.text)

        val payWithError = accountsViewModel.errorCheck()
        binding.editCard.payWithDropdown.setState(
            payWithError,
            if (payWithError == null) AbstractStatefulInput.SUCCESS else AbstractStatefulInput.ERROR
        )

        val businessNoError = viewModel.businessNoError()
        val accountNoError = viewModel.accountNoError()
        val amountError = viewModel.amountError()
        val nickNameError = viewModel.nameError()

        with(binding.editCard) {
            businessNoInput.setState(
                businessNoError,
                if (businessNoError == null) AbstractStatefulInput.SUCCESS else AbstractStatefulInput.ERROR
            )
            accountNoInput.setState(
                accountNoError,
                if (accountNoError == null) AbstractStatefulInput.SUCCESS else AbstractStatefulInput.ERROR
            )
            amountInput.setState(
                amountError,
                if (amountError == null) AbstractStatefulInput.SUCCESS else AbstractStatefulInput.ERROR
            )
        }
        binding.saveCard.billNameInput.setState(
            nickNameError,
            if (nickNameError == null && viewModel.saveBill.value!!) AbstractStatefulInput.SUCCESS else if (nickNameError != null) AbstractStatefulInput.ERROR else AbstractStatefulInput.NONE
        )

        return payWithError == null && businessNoError == null && accountNoError == null && amountError == null && nickNameError == null
    }

    override fun onSubmitForm() {
        with(accountsViewModel) {
            val actions = institutionActions.value
            val account = activeAccount.value
            val activeAction = actionSelectViewModel.activeAction.value

            val actionToRun = activeAction ?: actions?.firstOrNull { it.from_institution_id == it.to_institution_id }

            if (!actions.isNullOrEmpty() && account != null)
                (requireActivity() as AbstractHoverCallerActivity).runSession(account, actionToRun ?: actions.first(), viewModel.wrapExtras(), 0)
            else
                Timber.e("Request composition not complete; ${actions?.firstOrNull()}, $account")

            findNavController().popBackStack()
        }
    }

    private fun showUpdatePaybillConfirmation() = viewModel.selectedPaybill.value?.let {
        dialog = StaxDialog(requireActivity())
            .setDialogTitle(getString(R.string.paybill_update_header))
            .setDialogMessage(getString(R.string.paybill_update_msg, it.name))
            .setNegButton(R.string.btn_cancel, null)
            .setPosButton(R.string.btn_update) { _ ->
                if (activity != null) {
                    viewModel.updatePaybill(it)
                    UIHelper.flashAndReportMessage(requireActivity(), R.string.paybill_update_success)
                    viewModel.setEditing(false)
                }
            }
        dialog!!.showIt()
    }
}