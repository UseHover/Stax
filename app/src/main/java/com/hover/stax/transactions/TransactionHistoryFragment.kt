package com.hover.stax.transactions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.hover.stax.R
import com.hover.stax.databinding.TransactionCardHistoryBinding
import com.hover.stax.utils.AnalyticsUtil
import com.hover.stax.utils.NavUtil
import com.hover.stax.utils.UIHelper

class TransactionHistoryFragment : Fragment(), TransactionHistoryAdapter.SelectListener {

	private var _binding: TransactionCardHistoryBinding? = null
	private val binding get() = _binding!!

	private var transactionsAdapter: TransactionHistoryAdapter? = null
	override fun onCreateView(inflater: LayoutInflater,
	                          container: ViewGroup?,
	                          savedInstanceState: Bundle?): View {
		AnalyticsUtil.logAnalyticsEvent(getString(R.string.visit_screen, getString(R.string.visit_transaction_history)), requireActivity())
		_binding = TransactionCardHistoryBinding.inflate(inflater, container, false)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		initRecyclerView()
	}

	private fun initRecyclerView() {
		binding.transactionsRecycler.apply {
			layoutManager = UIHelper.setMainLinearManagers(context)
			transactionsAdapter = TransactionHistoryAdapter(null, null, this@TransactionHistoryFragment)
			adapter = transactionsAdapter
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