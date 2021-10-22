package com.hover.stax.settings

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.hover.stax.R
import com.hover.stax.account.Account
import com.hover.stax.databinding.FragmentManageStaxBinding
import com.hover.stax.navigation.NavigationInterface
import com.hover.stax.utils.UIHelper
import com.hover.stax.utils.Utils
import org.koin.androidx.viewmodel.ext.android.viewModel

class ManageStaxFragment : Fragment(), NavigationInterface {
    private var _binding: FragmentManageStaxBinding? = null
    private val binding get() = _binding!!

    private var accountAdapter: ArrayAdapter<Account>? = null
    private val viewModel: PinsViewModel by viewModel()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentManageStaxBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Utils.logAnalyticsEvent(getString(R.string.visit_screen, getString(R.string.visit_security)), requireActivity())
        setUpAccounts(viewModel);
    }

    private fun setUpAccounts(viewModel: PinsViewModel) {
        accountAdapter = ArrayAdapter(requireActivity(), R.layout.stax_spinner_item)
        viewModel.accounts.observe(viewLifecycleOwner) {
            showAccounts(it)

            if (!it.isNullOrEmpty() && it.size > 1)
                createDefaultSelector(it)
            else
                binding.cardAccounts.defaultAccountEntry.visibility = View.GONE
        }
    }

    private fun showAccounts(accounts: List<Account>) {
        val lv = binding.cardAccounts.accountsList
        accountAdapter!!.clear()
        accountAdapter!!.addAll(accounts)
        lv.adapter = accountAdapter
        lv.setOnItemClickListener { _, _, position, _ -> navigateToPinUpdateFragment(accounts[position].id, this@ManageStaxFragment) }
        UIHelper.fixListViewHeight(lv)
    }

    private fun createDefaultSelector(accounts: List<Account>) {
        val spinner = binding.cardAccounts.defaultAccountSpinner
        binding.cardAccounts.defaultAccountEntry.visibility = View.VISIBLE
        spinner.setAdapter(accountAdapter)
        spinner.setText(accounts.first { it.isDefault }.alias, false);
        spinner.onItemClickListener = AdapterView.OnItemClickListener { _, _, pos: Int, _ ->
            if (pos != 0) viewModel.setDefaultAccount(accounts[pos])
        }
    }
}