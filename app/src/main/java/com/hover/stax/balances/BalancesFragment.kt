package com.hover.stax.balances

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.hover.sdk.actions.HoverAction
import com.hover.stax.MainNavigationDirections
import com.hover.stax.R
import com.hover.stax.accounts.Account
import com.hover.stax.accounts.AccountsViewModel
import com.hover.stax.accounts.DUMMY
import com.hover.stax.addChannels.ChannelsViewModel
import com.hover.stax.databinding.FragmentBalanceBinding
import com.hover.stax.home.HomeFragmentDirections
import com.hover.stax.home.MainActivity
import com.hover.stax.hover.AbstractHoverCallerActivity
import com.hover.stax.utils.AnalyticsUtil
import com.hover.stax.utils.UIHelper
import com.hover.stax.utils.collectLatestLifecycleFlow
import com.hover.stax.views.StaxDialog
import com.hover.stax.views.staxcardstack.StaxCardStackView
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel


class BalancesFragment : Fragment(), BalanceAdapter.BalanceListener {

    private lateinit var addAccountBtn: CardView
    private lateinit var balanceTitle: TextView
    private lateinit var balanceStack: StaxCardStackView
    private lateinit var balancesRecyclerView: RecyclerView

    private var _binding: FragmentBalanceBinding? = null
    private val binding get() = _binding!!

    private val accountsViewModel: AccountsViewModel by sharedViewModel()
    private val balancesViewModel: BalancesViewModel by sharedViewModel()
    private val channelsViewModel: ChannelsViewModel by sharedViewModel()
    private lateinit var cardStackAdapter: BalanceCardStackAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentBalanceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpBalances()
        setUpLinkNewAccount()
    }

    private fun setUpBalances() {
        setUpBalanceHeader()
        setUpBalanceList()
        setUpHiddenStack()

        balancesViewModel.showBalances.observe(viewLifecycleOwner) { showBalanceCards(it) }

        collectLatestLifecycleFlow(accountsViewModel.accounts) {
            updateAccounts(ArrayList(it))
        }

        collectLatestLifecycleFlow(balancesViewModel.balanceAction) {
            attemptCallHover(balancesViewModel.userRequestedBalanceAccount.value, it)
        }

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                channelsViewModel.accountCallback.collect {
                    Toast.makeText(requireActivity(), "Account ${it.name} event has been received", Toast.LENGTH_SHORT).show()
                    askToCheckBalance(it)
                }
            }
        }
    }

    private fun attemptCallHover(account: Account?, action: HoverAction?) {
        action?.let { account?.let { callHover(account, action) } }
    }

    private fun callHover(account: Account, action: HoverAction) {
        (requireActivity() as AbstractHoverCallerActivity).runSession(account, action)
    }

    private fun setUpLinkNewAccount() {
        addAccountBtn = binding.newAccountLink
        addAccountBtn.setOnClickListener {
            (requireActivity() as MainActivity).checkPermissionsAndNavigate(MainNavigationDirections.actionGlobalAddChannelsFragment())
        }
    }

    private fun setUpBalanceHeader() {
        balanceTitle = binding.homeCardBalances.balanceHeaderTitleId.also {
            it.setCompoundDrawablesRelativeWithIntrinsicBounds(
                if (balancesViewModel.showBalances.value == true) R.drawable.ic_visibility_on else R.drawable.ic_visibility_off,
                0,
                0,
                0
            )
            it.setOnClickListener { balancesViewModel.setBalanceState(!balancesViewModel.showBalances.value!!) }
        }
    }

    private fun setUpBalanceList() {
        balancesRecyclerView = binding.homeCardBalances.balancesRecyclerView.apply {
            layoutManager = UIHelper.setMainLinearManagers(context)
            setHasFixedSize(true)
            visibility = View.GONE
        }
    }

    private fun setUpHiddenStack() {
        cardStackAdapter = BalanceCardStackAdapter(requireActivity())
        balanceStack = binding.stackBalanceCards
        balanceStack.apply {
            setAdapter(cardStackAdapter)
            setOverlapGaps(STACK_OVERLAY_GAP)
            rotationX = ROTATE_UPSIDE_DOWN
        }
    }

    private fun showBalanceCards(show: Boolean) {
        AnalyticsUtil.logAnalyticsEvent(getString(if (balancesViewModel.showBalances.value != true) R.string.show_balances else R.string.hide_balances), requireActivity())
        balanceTitle.setCompoundDrawablesRelativeWithIntrinsicBounds(
            if (show) R.drawable.ic_visibility_on else R.drawable.ic_visibility_off, 0, 0, 0
        )

        showAddAccount(accountsViewModel.accounts.value, show)
        if (show) binding.homeCardBalances.balancesMl.transitionToEnd() else binding.homeCardBalances.balancesMl.transitionToStart()

        balanceStack.visibility = if (show) View.GONE else View.VISIBLE
    }

    private fun updateAccounts(accounts: ArrayList<Account>) {
        accounts.let {
            addDummyAccountsIfRequired(accounts)
            cardStackAdapter.updateData(accounts.reversed())
            updateBalanceCardStackHeight(accounts.size)
            showAddAccount(accounts, balancesViewModel.showBalances.value!!)
        }
        val balancesAdapter = BalanceAdapter(accounts, this)
        balancesRecyclerView.adapter = balancesAdapter
        showBalanceCards(balancesViewModel.showBalances.value!!)
    }

    private fun askToCheckBalance(account: Account) {
        val dialog = StaxDialog(requireActivity())
            .setDialogTitle(R.string.check_balance_title)
            .setDialogMessage(R.string.check_balance_desc)
            .setNegButton(R.string.later, null)
            .setPosButton(R.string.check_balance_title) { onTapRefresh(account) }
        dialog.showIt()
    }

    private fun updateBalanceCardStackHeight(numOfItems: Int) {
        val params = balanceStack.layoutParams
        params.height = 20 * numOfItems
        balanceStack.layoutParams = params
    }

    private fun showAddAccount(accounts: List<Account>?, show: Boolean) {
        addAccountBtn.visibility = if (!accounts.isNullOrEmpty() && accounts.size > 1 && show) View.VISIBLE else View.GONE
    }

    private fun addDummyAccountsIfRequired(accounts: ArrayList<Account>?) {
        accounts?.let {
            if (it.isEmpty()) {
                accounts.add(Account(getString(R.string.your_main_account), GREEN_BG).dummy())
                accounts.add(Account(getString(R.string.your_other_account), BLUE_BG).dummy())
            }
            if (it.size == 1)
                accounts.add(Account(getString(R.string.your_other_account), BLUE_BG).dummy())
        }
    }

    override fun onTapRefresh(account: Account?) {
        if (account == null || account.id == DUMMY)
            (requireActivity() as MainActivity).checkPermissionsAndNavigate(HomeFragmentDirections.actionNavigationHomeToNavigationLinkAccount())
        else {
            AnalyticsUtil.logAnalyticsEvent(getString(R.string.refresh_balance_single), requireContext())
            balancesViewModel.requestBalance(account)
        }
    }

    override fun onTapDetail(accountId: Int) {
        if (accountId == DUMMY)
            (requireActivity() as MainActivity).checkPermissionsAndNavigate(HomeFragmentDirections.actionNavigationHomeToNavigationLinkAccount())
        else
            findNavController().navigate(HomeFragmentDirections.actionNavigationHomeToAccountDetailsFragment(accountId))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val GREEN_BG = "#46E6CC"
        const val BLUE_BG = "#04CCFC"

        const val STACK_OVERLAY_GAP = 10
        const val ROTATE_UPSIDE_DOWN = 180f
        const val BALANCE_VISIBILITY_KEY: String = "BALANCE_VISIBLE"
    }
}