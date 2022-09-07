package com.hover.stax.transactions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.fragment.app.Fragment
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import com.hover.stax.R
import com.hover.stax.databinding.TransactionCardHistoryBinding
import com.hover.stax.presentation.home.components.TopBar
import com.hover.stax.ui.theme.StaxTheme
import com.hover.stax.utils.AnalyticsUtil
import com.hover.stax.utils.NavUtil
import com.hover.stax.utils.UIHelper
import com.hover.stax.utils.network.NetworkMonitor
import org.koin.androidx.viewmodel.ext.android.viewModel

class TransactionHistoryFragment : Fragment(), TransactionHistoryAdapter.SelectListener {

    private var _binding: TransactionCardHistoryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TransactionHistoryViewModel by viewModel()
    private var transactionsAdapter: TransactionHistoryAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        AnalyticsUtil.logAnalyticsEvent(getString(R.string.visit_screen, getString(R.string.visit_transaction_history)), requireActivity())
        _binding = TransactionCardHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initToolbar()

        initRecyclerView()
        observeTransactionActionPair()
    }

    private fun initToolbar() {
        binding.toolbar.setContent {
            StaxTheme { Toolbar() }
        }
    }

    @Composable
    private fun Toolbar() {
        val hasNetwork by NetworkMonitor.StateLiveData.get().observeAsState(initial = false)
        TopBar(
            title = R.string.nav_history, isInternetConnected = hasNetwork,
            { navigate(TransactionHistoryFragmentDirections.actionGlobalNavigationSettings()) },
            { navigate(TransactionHistoryFragmentDirections.actionGlobalRewardsFragment()) }
        )
    }

    private fun navigate(navDirections: NavDirections) = NavUtil.navigate(findNavController(), navDirections)

    private fun initRecyclerView() {
        binding.transactionsRecycler.apply {
            layoutManager = UIHelper.setMainLinearManagers(context)
            transactionsAdapter = TransactionHistoryAdapter(this@TransactionHistoryFragment)
            adapter = transactionsAdapter
        }
    }

    private fun observeTransactionActionPair() {
        viewModel.transactionHistoryItem.observe(viewLifecycleOwner) {
            binding.noHistory.visibility = if (it.isNullOrEmpty()) View.VISIBLE else View.GONE
            transactionsAdapter!!.submitList(it)
        }
    }

    override fun viewTransactionDetail(uuid: String?) {
        uuid?.let { NavUtil.showTransactionDetailsFragment(findNavController(), it) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}