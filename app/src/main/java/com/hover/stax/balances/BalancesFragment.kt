package com.hover.stax.balances

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.hover.stax.MainNavigationDirections
import com.hover.stax.R
import com.hover.stax.accounts.Account
import com.hover.stax.accounts.AccountsViewModel
import com.hover.stax.accounts.DUMMY
import com.hover.stax.databinding.FragmentBalanceBinding
import com.hover.stax.home.MainActivity
import com.hover.stax.utils.AnalyticsUtil
import com.hover.stax.utils.UIHelper
import com.hover.stax.views.staxcardstack.StaxCardStackView
import org.koin.androidx.viewmodel.ext.android.sharedViewModel


class BalancesFragment : Fragment() {

    private lateinit var addAccountBtn: CardView
    private lateinit var balanceTitle: TextView
    private lateinit var balanceStack: StaxCardStackView
    private lateinit var balancesRecyclerView: RecyclerView

    private var _binding: FragmentBalanceBinding? = null
    private val binding get() = _binding!!

    private val accountsViewModel: AccountsViewModel by sharedViewModel()
    private val balancesViewModel: BalancesViewModel by sharedViewModel()
    private lateinit var cardStackAdapter: BalanceCardStackAdapter

    private var showAddSecondAccount = false

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
        balancesViewModel.showBalances.observe(viewLifecycleOwner) { showBalanceCards(it) }
        setUpBalanceHeader()
        setUpBalanceList()
        setUpHiddenStack()

        val observer = Observer<List<Account>> { t -> updateAccounts(ArrayList(t)) }
        accountsViewModel.accounts.observe(viewLifecycleOwner, observer)
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
        balancesRecyclerView = binding.homeCardBalances.balancesRecyclerView.also {
            it.layoutManager = UIHelper.setMainLinearManagers(context)
            it.setHasFixedSize(true)
            it.visibility = View.GONE
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
        if (showAddSecondAccount) addAccountBtn.visibility = if (show) View.VISIBLE else View.GONE

        if (show) binding.homeCardBalances.balancesMl.transitionToEnd() else binding.homeCardBalances.balancesMl.transitionToStart()

        balanceStack.visibility = if (show) View.GONE else View.VISIBLE
//        balanceStack.setOverlapGaps(if (show) STACK_OVERLAY_GAP else 0)
    }

    private fun updateAccounts(accounts: ArrayList<Account>) {
        accounts.let {
            addDummyAccountsIfRequired(accounts)
            cardStackAdapter.updateData(accounts.reversed())
            updateBalanceCardStackHeight(accounts.size)
        }
        val balancesAdapter = BalanceAdapter(accounts, activity as MainActivity)
        balancesRecyclerView.adapter = balancesAdapter
    }

    private fun updateBalanceCardStackHeight(numOfItems: Int) {
        val params = balanceStack.layoutParams
        params.height = 20 * numOfItems
        balanceStack.layoutParams = params
    }

    private fun addDummyAccountsIfRequired(accounts: ArrayList<Account>?) {
        showAddSecondAccount = !accounts.isNullOrEmpty() && accounts.none { it.id == DUMMY } && accounts.size > 1
        accounts?.let {
            if (it.isEmpty()) {
                accounts.add(Account(getString(R.string.your_main_account), GREEN_BG).dummy())
                accounts.add(Account(getString(R.string.your_other_account), BLUE_BG).dummy())
            }
            if (it.size == 1) {
                accounts.add(Account(getString(R.string.your_other_account), BLUE_BG).dummy())
            }
        }
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