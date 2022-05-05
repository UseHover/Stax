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
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.hover.sdk.actions.HoverAction
import com.hover.stax.R
import com.hover.stax.balances.BalanceAdapter
import com.hover.stax.balances.BalancesViewModel
import com.hover.stax.databinding.FragmentAccountBinding
import com.hover.stax.futureTransactions.FutureViewModel
import com.hover.stax.futureTransactions.RequestsAdapter
import com.hover.stax.futureTransactions.ScheduledAdapter
import com.hover.stax.hover.AbstractHoverCallerActivity
import com.hover.stax.requests.Request
import com.hover.stax.schedules.Schedule
import com.hover.stax.transactions.TransactionHistoryAdapter
import com.hover.stax.utils.*
import com.hover.stax.views.AbstractStatefulInput
import com.hover.stax.views.StaxDialog
import com.hover.stax.views.StaxTextInput
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel


class AccountDetailFragment : Fragment(), TransactionHistoryAdapter.SelectListener, ScheduledAdapter.SelectListener,
    RequestsAdapter.SelectListener, BalanceAdapter.BalanceListener {

    private val viewModel: AccountDetailViewModel by sharedViewModel()
    private val balancesViewModel: BalancesViewModel by sharedViewModel()
    private val futureViewModel: FutureViewModel by viewModel()

    private var transactionsAdapter: TransactionHistoryAdapter? = null
    private var requestsAdapter: RequestsAdapter? = null
    private var scheduledAdapter: ScheduledAdapter? = null

    private var _binding: FragmentAccountBinding? = null
    private val binding get() = _binding!!

    private var dialog: StaxDialog? = null

    private val args: AccountDetailFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
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

        viewModel.setAccount(args.accountId)
    }

    private fun setUpBalance() {
        binding.balanceCard.root.cardElevation = 0F
        binding.balanceCard.balanceChannelName.setTextColor(ContextCompat.getColor(requireActivity(), R.color.offWhite))
        binding.balanceCard.balanceAmount.setTextColor(ContextCompat.getColor(requireActivity(), R.color.offWhite))
        binding.balanceCard.balanceRefreshIcon.setOnClickListener { onTapRefresh(viewModel.account.value) }
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
            toggleButtonHighlight(binding.manageCard.nicknameInput, binding.manageCard.nicknameSaveBtn, charSequence.toString(), viewModel.account.value?.alias)
        }
    }

    private val accountWatcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
        override fun afterTextChanged(editable: Editable) {}
        override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            toggleButtonHighlight(binding.manageCard.accountNumberInput, binding.manageCard.accountSaveBtn, charSequence.toString(), viewModel.account.value?.accountNo)
        }
    }

    private fun toggleButtonHighlight(v: StaxTextInput, btn: AppCompatButton, newText: String, comparator: String?) {
        if (newText.isNotEmpty() && comparator != null && newText != comparator)
            v.setState(null, AbstractStatefulInput.NONE)
        btn.backgroundTintList = ColorStateList.valueOf(
            if (newText.isNotEmpty() && comparator != null && newText != comparator)
                ContextCompat.getColor(requireActivity(), R.color.brightBlue)
            else ContextCompat.getColor(requireActivity(), R.color.buttonColor)
        )
    }

    private fun updateNickname() {
        validateInput(binding.manageCard.nicknameInput, viewModel.account.value?.alias, R.string.account_name_error, viewModel::updateAccountName)
    }

    private fun updateAccountNumber() {
        validateInput(binding.manageCard.accountNumberInput, viewModel.account.value?.accountNo, R.string.account_number_error, viewModel::updateAccountNumber)
    }

    private fun validateInput(v: StaxTextInput, comparison: String?, errorMsg: Int, successFun: (text: String) -> Unit) {
        val msg = validates(v, comparison, errorMsg)
        if (msg == null)
            successFun(v.text)
        v.setState(
            msg
                ?: getString(R.string.label_saved), if (msg == null) AbstractStatefulInput.SUCCESS else AbstractStatefulInput.ERROR
        )
    }

    private fun validates(v: StaxTextInput, comparison: String?, errorMsg: Int): String? {
        return if (v.text.isEmpty() || v.text == comparison) getString(errorMsg)
        else null
    }

    private fun setupObservers() {
        observeBalanceCheck()

        with(viewModel) {
            account.observe(viewLifecycleOwner) {
                it?.let { acct ->
                    binding.amountsCard.setTitle(acct.alias)
                    if (acct.latestBalance != null) {
                        binding.balanceCard.balanceAmount.text = acct.latestBalance
                        binding.balanceCard.balanceSubtitle.text = DateUtils.humanFriendlyDateTime(acct.latestBalanceTimestamp)
                    } else binding.balanceCard.balanceSubtitle.text = getString(R.string.refresh_balance_desc)

                    binding.feesDescription.text = getString(R.string.fees_label, acct.name)
                    binding.detailsCard.officialName.text = if(acct.name == PLACEHOLDER) acct.alias else acct.name

                    binding.manageCard.nicknameInput.setText(acct.alias, false)
                    binding.manageCard.accountNumberInput.setText(acct.accountNo, false)
                    binding.manageCard.removeAcctBtn.setOnClickListener { setUpRemoveAccount(acct) }

                    setUpFuture(acct.channelId)
                }
            }

            channel.observe(viewLifecycleOwner) { c ->
                if (account.value != null && account.value!!.alias != c.name)
                    binding.amountsCard.setSubtitle(c.name)
                binding.detailsCard.shortcodeBtn.text = getString(R.string.dial_btn, c.rootCode)
                binding.detailsCard.shortcodeBtn.setOnClickListener { Utils.dial(c.rootCode, requireContext()) }
            }

            transactions.observe(viewLifecycleOwner) {
                binding.historyCard.noHistory.visibility = if (it.isNullOrEmpty()) View.VISIBLE else View.GONE
                transactionsAdapter!!.updateData(it, viewModel.actions.value)
            }

            actions.observe(viewLifecycleOwner) { transactionsAdapter!!.updateData(viewModel.transactions.value, it) }

            spentThisMonth.observe(viewLifecycleOwner) {
                binding.detailsMoneyOut.text = Utils.formatAmount(it ?: 0.0)
            }

            feesThisYear.observe(viewLifecycleOwner) {
                binding.detailsFees.text = Utils.formatAmount(it ?: 0.0)
            }
        }
    }

    private fun observeBalanceCheck() {
        balancesViewModel.balanceAction.observe(viewLifecycleOwner) {
            attemptCallHover(viewModel.account.value, it)
        }
        viewModel.account.observe(viewLifecycleOwner) {
            attemptCallHover(it, balancesViewModel.balanceAction.value)
        }
    }

    override fun onTapRefresh(account: Account?) {
        account?.let {
            AnalyticsUtil.logAnalyticsEvent(getString(R.string.refresh_balance_single), requireContext())
            balancesViewModel.requestBalance(account)
        }
    }

    override fun onTapDetail(accountId: Int) { }

    private fun attemptCallHover(account: Account?, action: HoverAction?) {
        action?.let { account?.let { callHover(account, action) } }
    }

    private fun callHover(account: Account, action: HoverAction) {
        balancesViewModel.requestBalance(null)
        (requireActivity() as AbstractHoverCallerActivity).run(account, action)
    }

    private fun setUpRemoveAccount(account: Account) {
        dialog = StaxDialog(requireActivity())
                .setDialogTitle(getString(R.string.removeaccount_dialoghead, account.alias))
                .setDialogMessage(R.string.removeaccount_msg)
                .setPosButton(R.string.btn_removeaccount) { removeAccount(account) }
                .setNegButton(R.string.btn_cancel, null)
                .isDestructive
        dialog!!.showIt()
    }

    private fun removeAccount(account: Account) {
        viewModel.removeAccount(account)
        NavHostFragment.findNavController(this).popBackStack()
        UIHelper.flashMessage(requireActivity(), resources.getString(R.string.toast_confirm_acctremoved))
    }

    private fun initRecyclerViews() {
        binding.historyCard.transactionsRecycler.apply {
            layoutManager = UIHelper.setMainLinearManagers(context)
            transactionsAdapter = TransactionHistoryAdapter(null, null, this@AccountDetailFragment)
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

    override fun viewTransactionDetail(uuid: String?)  {
        uuid?.let { NavUtil.showTransactionDetailsFragment(findNavController(), it) }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        if (dialog != null && dialog!!.isShowing) dialog!!.dismiss()

        _binding = null
    }
}