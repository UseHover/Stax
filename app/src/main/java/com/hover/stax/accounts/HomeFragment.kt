package com.hover.stax.accounts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.hover.sdk.actions.HoverAction
import com.hover.stax.R
import com.hover.stax.channels.ChannelsAdapter
import com.hover.stax.databinding.FragmentAccountsBinding
import com.hover.stax.transfers.TransactionType
import com.hover.stax.utils.UIHelper
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class HomeFragment : Fragment(), AccountsAdapter.SelectListener {

    private var _binding: FragmentAccountsBinding? = null
    private val binding get() = _binding!!

    private val accountsViewModel: AccountsViewModel by sharedViewModel()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAccountsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val selectAdapter = AccountsAdapter(ArrayList(), this)

        binding.accountsRecycler.apply {
            layoutManager = UIHelper.setMainLinearManagers(requireActivity())
            adapter = selectAdapter
        }

        binding.accountListCard.setOnClickIcon { findNavController().popBackStack() }

        accountsViewModel.accounts.observe(viewLifecycleOwner) {
            if (it.isNotEmpty())
                showAccountsList(it)
        }
    }

    override fun accountSelected(account: Account) {
        accountsViewModel.setActiveAccount(account)
    }

    private fun showAccountsList(accounts: List<Account>) {
        binding.accountListCard.visibility = View.GONE

        with(binding.accountsCard) {
            accountSelectCard.apply {
                visibility = View.VISIBLE
                setOnClickListener { this.visibility = View.GONE; binding.accountListCard.visibility = View.VISIBLE }
            }
            accountsInfo.text = getString(R.string.account_select_header, accountsViewModel.activeAccount.value?.name, getTransactionType())
            accountsRecycler.adapter = AccountsAdapter(accounts, this@HomeFragment)
        }
    }

    private fun getTransactionType(): String = when (TransactionType.type) {
        HoverAction.AIRTIME -> getString(R.string.cta_airtime)
        HoverAction.P2P -> getString(R.string.cta_transfer)
        HoverAction.C2B -> getString(R.string.cta_paybill)
        else -> getString(R.string.cta_transfer)
    }
}