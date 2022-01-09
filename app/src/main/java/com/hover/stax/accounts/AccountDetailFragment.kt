package com.hover.stax.accounts

import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatButton

import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.hover.stax.R
import com.hover.stax.databinding.FragmentAccountBinding
import com.hover.stax.futureTransactions.FutureViewModel
import com.hover.stax.futureTransactions.RequestsAdapter
import com.hover.stax.futureTransactions.ScheduledAdapter
import com.hover.stax.home.MainActivity
import com.hover.stax.navigation.NavigationInterface
import com.hover.stax.requests.Request
import com.hover.stax.schedules.Schedule
import com.hover.stax.transactions.TransactionHistoryAdapter
import com.hover.stax.utils.*
import com.hover.stax.views.AbstractStatefulInput
import com.hover.stax.views.StaxDialog
import com.hover.stax.views.StaxTextInputLayout
import org.koin.androidx.viewmodel.ext.android.viewModel


class AccountDetailFragment : Fragment(), TransactionHistoryAdapter.SelectListener, ScheduledAdapter.SelectListener,
        RequestsAdapter.SelectListener, NavigationInterface {

    private val viewModel: AccountDetailViewModel by viewModel()
    private val futureViewModel: FutureViewModel by viewModel()

    private var transactionsAdapter: TransactionHistoryAdapter? = null
    private var requestsAdapter: RequestsAdapter? = null
    private var scheduledAdapter: ScheduledAdapter? = null

    private var _binding: FragmentAccountBinding? = null
    private val binding get() = _binding!!

    private var dialog: StaxDialog? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        AnalyticsUtil.logAnalyticsEvent(getString(R.string.visit_screen, getString(R.string.visit_channel)), requireActivity());

        initRecyclerViews()
        setupObservers()
        setUpBalance()
        setUpManage()

        arguments?.let { viewModel.setAccount(it.getInt(Constants.ACCOUNT_ID)) }
    }

    private fun setUpBalance() {
        binding.balanceCard.root.cardElevation = 0F
        binding.balanceCard.balanceAmount.text = " - "
        binding.balanceCard.balanceChannelName.setTextColor(resources.getColor(R.color.offWhite))
        binding.balanceCard.balanceAmount.setTextColor(resources.getColor(R.color.offWhite))
        binding.balanceCard.balanceRefreshIcon.setOnClickListener { onRefresh() }
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

    private fun toggleButtonHighlight(v: StaxTextInputLayout, btn: AppCompatButton, newText: String, comparator: String?) {
        if (newText.isNotEmpty() && comparator != null && newText != comparator)
            v.setState(null, AbstractStatefulInput.NONE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            btn.backgroundTintList = ColorStateList.valueOf(
                    if (newText.isNotEmpty() && comparator != null && newText != comparator)
                        resources.getColor(R.color.brightBlue)
                    else resources.getColor(R.color.buttonColor)
            )
        }
    }

    private fun updateNickname() {
        validateInput(binding.manageCard.nicknameInput, viewModel.account.value?.alias, R.string.account_name_error, viewModel::updateAccountName)
    }

    private fun updateAccountNumber() {
        validateInput(binding.manageCard.accountNumberInput, viewModel.account.value?.accountNo, R.string.account_number_error, viewModel::updateAccountNumber)
    }

    private fun validateInput(v: StaxTextInputLayout, comparison: String?, errorMsg: Int, successFun: (text: String) -> Unit) {
        val msg = validates(v, comparison, errorMsg)
        if (msg == null)
            successFun(v.text)
        v.setState(msg
                ?: getString(R.string.label_saved), if (msg == null) AbstractStatefulInput.SUCCESS else AbstractStatefulInput.ERROR)
    }

    private fun validates(v: StaxTextInputLayout, comparison: String?, errorMsg: Int): String? {
        return if (v.text.isEmpty() || v.text == comparison) getString(errorMsg)
        else null
    }

    private fun setupObservers() {
        with(viewModel) {
            account.observe(viewLifecycleOwner) {
                it?.let { acct ->
                    binding.detailsCard.setTitle(acct.alias)
                    if (acct.latestBalance != null) {
                        binding.balanceCard.balanceAmount.text = acct.latestBalance
                        binding.balanceCard.balanceSubtitle.text = DateUtils.humanFriendlyDateTime(acct.latestBalanceTimestamp)
                    } else binding.balanceCard.balanceSubtitle.text = getString(R.string.refresh_balance_desc)

                    binding.feesDescription.text = getString(R.string.fees_label, acct.name)
                    binding.officialName.text = acct.name

                    binding.manageCard.nicknameInput.setText(acct.alias, false)
                    binding.manageCard.accountNumberInput.setText(acct.accountNo, false)
                    binding.manageCard.removeAcctBtn.setOnClickListener { setUpRemoveAccount(acct) }

                    setUpFuture(acct.channelId)
                }
            }

            channel.observe(viewLifecycleOwner) { c ->
                if (account.value != null && account.value!!.alias != c.name)
                    binding.detailsCard.setSubtitle(c.name)
                binding.shortcodeCard.dialShortcode.text = getString(R.string.dial_btn, c.rootCode)
                binding.shortcodeCard.dialShortcode.setOnClickListener { Utils.dial(c.rootCode, requireContext()) }
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

    private fun setUpRemoveAccount(account: Account) {
        dialog = StaxDialog(requireActivity())
                .setDialogTitle(getString(R.string.removepin_dialoghead, account.alias))
                .setDialogMessage(R.string.removepins_dialogmes)
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

    private fun onRefresh() = viewModel.account.value?.let { (activity as MainActivity).onTapRefresh(it.id) }

    override fun viewRequestDetail(id: Int) =
            findNavController().navigate(R.id.action_accountDetailsFragment_to_requestDetailsFragment, bundleOf("id" to id))

    override fun viewScheduledDetail(id: Int) =
            findNavController().navigate(R.id.action_accountDetailsFragment_to_scheduleDetailsFragment, bundleOf("id" to id))

    override fun viewTransactionDetail(uuid: String?) = navigateToTransactionDetailsFragment(uuid, childFragmentManager, true)

    override fun onDestroyView() {
        super.onDestroyView()

        if (dialog != null && dialog!!.isShowing) dialog!!.dismiss()

        _binding = null
    }
}