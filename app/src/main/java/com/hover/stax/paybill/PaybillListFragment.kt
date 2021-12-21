package com.hover.stax.paybill

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.hover.stax.databinding.FragmentPaybillListBinding
import com.hover.stax.utils.Constants
import com.hover.stax.utils.UIHelper
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class PaybillListFragment : Fragment(), PaybillAdapter.ClickListener {

    private var _binding: FragmentPaybillListBinding? = null
    private val binding get() = _binding!!

    private val paybillViewModel: PaybillViewModel by sharedViewModel()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentPaybillListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.getInt(Constants.ACCOUNT_ID)?.let {
            paybillViewModel.getSavedPaybills(it)
            paybillViewModel.getPaybills(it)
        }

        startObservers()
        binding.newPaybill.newPaybillCard.setOnClickListener {  }
    }

    private fun startObservers() {
        with(paybillViewModel) {
            savedPaybills.observe(viewLifecycleOwner) {
                if (it.isNotEmpty())
                    showSavedPaybills(it)
                else
                    toggleSavedPaybills(false)
            }

            accountPaybills.observe(viewLifecycleOwner) {
                if (it.isNotEmpty())
                    showAccountPaybills(it)
                else
                    toggleAccountPaybills(false)
            }
        }
    }

    private fun showAccountPaybills(paybills: List<Paybill>) {
        toggleAccountPaybills(true)

        binding.popularList.apply {
            layoutManager = UIHelper.setMainLinearManagers(requireActivity())
            adapter = PaybillAdapter(paybills, this@PaybillListFragment)
        }
    }

    private fun showSavedPaybills(paybills: List<Paybill>) {
        toggleSavedPaybills(true)

        binding.savedList.apply {
            layoutManager = UIHelper.setMainLinearManagers(requireActivity())
            adapter = PaybillAdapter(paybills, this@PaybillListFragment)
        }
    }

    private fun toggleSavedPaybills(show: Boolean) {
        binding.savedHeader.visibility = if(show) View.VISIBLE else View.GONE
        binding.savedList.visibility = if(show) View.VISIBLE else View.GONE
    }

    private fun toggleAccountPaybills(show: Boolean) {
        binding.popularHeader.visibility = if(show) View.VISIBLE else View.GONE
        binding.popularList.visibility = if(show) View.VISIBLE else View.GONE
    }

    override fun onDeletePaybill(paybill: Paybill) {
        paybillViewModel.deletePaybill(paybill)
    }

    override fun onSelectPaybill(paybill: Paybill) = paybillViewModel.selectPaybill(paybill)

}