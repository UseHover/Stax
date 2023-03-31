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
package com.hover.stax.accounts

import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.hover.stax.R
import com.hover.stax.databinding.FragmentAccountBinding
import com.hover.stax.domain.model.USSDAccount
import com.hover.stax.domain.model.USSD_TYPE
import com.hover.stax.domain.use_case.AccountDetail
import com.hover.stax.futureTransactions.FutureViewModel
import com.hover.stax.futureTransactions.RequestsAdapter
import com.hover.stax.futureTransactions.ScheduledAdapter
import com.hover.stax.hover.AbstractBalanceCheckerFragment
import com.hover.stax.requests.Request
import com.hover.stax.schedules.Schedule
import com.hover.stax.transactions.TransactionHistoryAdapter
import com.hover.stax.utils.AnalyticsUtil
import com.hover.stax.utils.DateUtils
import com.hover.stax.utils.NavUtil
import com.hover.stax.utils.UIHelper
import com.hover.stax.utils.Utils
import com.hover.stax.views.AbstractStatefulInput
import com.hover.stax.views.StaxDialog
import com.hover.stax.views.StaxTextInput
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class AccountDetailFragment :
    AbstractBalanceCheckerFragment(),
    TransactionHistoryAdapter.SelectListener,
    ScheduledAdapter.SelectListener,
    RequestsAdapter.SelectListener {

    private val viewModel: AccountDetailViewModel by sharedViewModel()
    private val futureViewModel: FutureViewModel by viewModel()

    private var transactionsAdapter: TransactionHistoryAdapter? = null
    private var requestsAdapter: RequestsAdapter? = null
    private var scheduledAdapter: ScheduledAdapter? = null

    private var _binding: FragmentAccountBinding? = null
    private val binding get() = _binding!!

    private var dialog: StaxDialog? = null

    private val args: AccountDetailFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        AnalyticsUtil.logAnalyticsEvent(getString(R.string.visit_screen, getString(R.string.visit_channel)), requireActivity())

        initRecyclerViews()
        setupObservers()
        setUpBalance()
        setUpManage()

        viewModel.setAccount(args.accountId, USSD_TYPE)
    }

    private fun setUpBalance() {
        binding.balanceCard.root.cardElevation = 0F
        binding.balanceCard.balanceChannelName.setTextColor(ContextCompat.getColor(requireActivity(), R.color.offWhite))
        binding.balanceCard.balanceAmount.setTextColor(ContextCompat.getColor(requireActivity(), R.color.offWhite))
        binding.balanceCard.balanceRefreshIcon.setOnClickListener {
            viewModel.account.value?.let { a ->
                onTapBalanceRefresh(a)
            }
        }
    }

    private fun setUpManage() {
        binding.manageCard.nicknameSaveBtn.setOnClickListener { updateNickname() }
        binding.manageCard.accountSaveBtn.setOnClickListener { updateAccountNumber() }
        binding.manageCard.nicknameInput.addTextChangedListener(nicknameWatcher)
        binding.manageCard.accountNumberInput.addTextChangedListener(accountWatcher)
    }

    private val nicknameWatcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
        override fun afterTextChanged(editable: Editable) {}
        override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            toggleButtonHighlight(binding.manageCard.nicknameInput, binding.manageCard.nicknameSaveBtn, charSequence.toString(), viewModel.account.value?.ussdAccount?.userAlias)
        }
    }

    private val accountWatcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
        override fun afterTextChanged(editable: Editable) {}
        override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            toggleButtonHighlight(binding.manageCard.accountNumberInput, binding.manageCard.accountSaveBtn, charSequence.toString(), viewModel.account.value?.ussdAccount?.accountNo)
        }
    }

    private fun toggleButtonHighlight(
        v: StaxTextInput,
        btn: AppCompatButton,
        newText: String,
        comparator: String?
    ) {
        if (newText.isNotEmpty() && comparator != null && newText != comparator)
            v.setState(null, AbstractStatefulInput.NONE)
        btn.backgroundTintList = ColorStateList.valueOf(
            if (newText.isNotEmpty() && comparator != null && newText != comparator)
                ContextCompat.getColor(requireActivity(), R.color.brightBlue)
            else ContextCompat.getColor(requireActivity(), R.color.buttonColor)
        )
    }

    private fun updateNickname() {
        validateInput(binding.manageCard.nicknameInput, viewModel.account.value?.ussdAccount?.userAlias, R.string.account_name_error, viewModel::updateAccountName)
    }

    private fun updateAccountNumber() {
        validateInput(binding.manageCard.accountNumberInput, viewModel.account.value?.ussdAccount?.accountNo, R.string.account_number_error, viewModel::updateAccountNumber)
    }

    private fun validateInput(
        v: StaxTextInput,
        comparison: String?,
        errorMsg: Int,
        successFun: (text: String) -> Unit
    ) {
        val msg = validates(v, comparison, errorMsg)
        if (msg == null)
            successFun(v.text)
        v.setState(
            msg
                ?: getString(R.string.label_saved),
            if (msg == null) AbstractStatefulInput.SUCCESS else AbstractStatefulInput.ERROR
        )
    }

    private fun validates(v: StaxTextInput, comparison: String?, errorMsg: Int): String? {
        return if (v.text.isEmpty() || v.text == comparison) getString(errorMsg)
        else null
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.account.collect {
                        it?.ussdAccount?.let { acct ->
                            binding.amountsCard.setTitle(acct.userAlias)
                            if (acct.userAlias != acct.institutionName)
                                binding.amountsCard.setSubtitle(acct.institutionName)
                            if (acct.latestBalance != null) {
                                binding.balanceCard.balanceAmount.text = acct.latestBalance
                                binding.balanceCard.balanceSubtitle.text =
                                    DateUtils.humanFriendlyDateTime(acct.latestBalanceTimestamp)
                            } else binding.balanceCard.balanceSubtitle.text =
                                getString(R.string.refresh_balance_desc)

                            binding.feesDescription.text =
                                getString(R.string.fees_label, acct.institutionName)
                            binding.detailsCard.officialName.text = acct.userAlias

                            binding.manageCard.nicknameInput.setText(acct.userAlias, false)
                            binding.manageCard.accountNumberInput.setText(acct.accountNo, false)
                            binding.manageCard.removeAcctBtn.setOnClickListener {
                                setUpRemoveAccount(
                                    acct
                                )
                            }

                            setUpFuture(acct.channelId)
                        }

                        it?.channel?.let { c ->
                            binding.detailsCard.shortcodeBtn.text =
                                getString(R.string.dial_btn, c.rootCode)
                            binding.detailsCard.shortcodeBtn.setOnClickListener { _ ->
                                Utils.dial(c.rootCode, requireContext())
                            }
                        }

                        it?.transactions?.let { ts ->
                            binding.historyCard.noHistory.visibility =
                                if (ts.isNullOrEmpty()) View.VISIBLE else View.GONE
                            transactionsAdapter!!.submitList(ts)
                        }
                    }
                }

//                launch {
//                    viewModel.spentThisMonth.collect {
//                    it.let { binding.detailsMoneyOut.text = Utils.formatAmount(it ?: 0.0) }
//                }}

//                launch {
//                    viewModel.feesThisYear.collect {
//                    it?.let { binding.detailsFees.text = Utils.formatAmount(it ?: 0.0) }
//                }}
            }
        }
    }

    private fun onTapBalanceRefresh(account: AccountDetail) {
        attemptCallHover(account.ussdAccount, account.balanceAction)
    }

    private fun setUpRemoveAccount(account: USSDAccount) {
        dialog = StaxDialog(requireActivity())
            .setDialogTitle(getString(R.string.removeaccount_dialoghead, account.userAlias))
            .setDialogMessage(R.string.removeaccount_msg)
            .setPosButton(R.string.btn_removeaccount) { removeAccount(account) }
            .setNegButton(R.string.btn_cancel, null)
            .isDestructive
        dialog!!.showIt()
    }

    private fun removeAccount(account: USSDAccount) {
        viewModel.removeAccount(account)
        NavHostFragment.findNavController(this).popBackStack()
        UIHelper.flashAndReportMessage(requireActivity(), resources.getString(R.string.toast_confirm_acctremoved))
    }

    private fun initRecyclerViews() {
        binding.historyCard.transactionsRecycler.apply {
            layoutManager = UIHelper.setMainLinearManagers(context)
            transactionsAdapter = TransactionHistoryAdapter(this@AccountDetailFragment)
            adapter = transactionsAdapter
        }

        binding.scheduledCard.scheduledRecyclerView.apply {
            layoutManager = UIHelper.setMainLinearManagers(context)
            scheduledAdapter = ScheduledAdapter(null, this@AccountDetailFragment)
            adapter = scheduledAdapter
        }

        binding.scheduledCard.requestsRecyclerView.apply {
            layoutManager = UIHelper.setMainLinearManagers(context)
            requestsAdapter = RequestsAdapter(null, this@AccountDetailFragment)
            adapter = requestsAdapter
        }
    }

    private fun setUpFuture(channelId: Int) {
        with(futureViewModel) {
            scheduledByChannel(channelId).observe(viewLifecycleOwner) {
                scheduledAdapter?.updateData(it)
                setFutureVisible(it, requests.value)
            }

            requestsByChannel(channelId).observe(viewLifecycleOwner) {
                requestsAdapter?.updateData(it)
                setFutureVisible(scheduled.value, it)
            }
        }
    }

    private fun setFutureVisible(schedules: List<Schedule>?, requests: List<Request>?) {
        val visible = !schedules.isNullOrEmpty() || !requests.isNullOrEmpty()
        binding.scheduledCard.root.visibility = if (visible) View.VISIBLE else View.GONE
    }

    override fun viewRequestDetail(id: Int) {
        NavUtil.navigate(findNavController(), AccountDetailFragmentDirections.actionAccountDetailsFragmentToRequestDetailsFragment(id))
    }

    override fun viewScheduledDetail(id: Int) {
        NavUtil.navigate(findNavController(), AccountDetailFragmentDirections.actionAccountDetailsFragmentToScheduleDetailsFragment(id))
    }

    override fun viewTransactionDetail(uuid: String?) {
        uuid?.let { NavUtil.showTransactionDetailsFragment(findNavController(), it) }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        if (dialog != null && dialog!!.isShowing) dialog!!.dismiss()

        _binding = null
    }
}