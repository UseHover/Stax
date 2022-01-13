package com.hover.stax.paybill

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.hover.sdk.actions.HoverAction
import com.hover.stax.R
import com.hover.stax.databinding.FragmentPaybillListBinding
import com.hover.stax.utils.Constants
import com.hover.stax.utils.UIHelper
import com.hover.stax.views.StaxDialog
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class PaybillListFragment : Fragment(), PaybillAdapter.ClickListener, PaybillActionsAdapter.PaybillActionsClickListener {

    private var _binding: FragmentPaybillListBinding? = null
    private val binding get() = _binding!!

    private val paybillViewModel: PaybillViewModel by sharedViewModel()

    private var dialog: StaxDialog? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPaybillListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.contentLayout.setOnClickIcon { findNavController().popBackStack() }

        arguments?.getInt(Constants.ACCOUNT_ID)?.let {
            paybillViewModel.getSavedPaybills(it)
            paybillViewModel.getPopularPaybills(it)
        }

        startObservers()

        binding.newPaybill.newPaybillCard.setOnClickListener {
            PaybillNumberDialog().show(childFragmentManager, PaybillNumberDialog::class.java.simpleName)
        }
    }

    private fun startObservers() {
        with(paybillViewModel) {
            savedPaybills.observe(viewLifecycleOwner) {
                if (it.isNotEmpty())
                    showSavedPaybills(it)
                else
                    toggleSavedPaybills(false)
            }

            popularPaybills.observe(viewLifecycleOwner) {
                if (it.isNotEmpty())
                    showPopularPaybills(it)
                else
                    togglePopularPaybills(false)
            }
        }
    }

    private fun showPopularPaybills(paybills: List<HoverAction>) {
        togglePopularPaybills(true)

        binding.popularList.apply {
            layoutManager = UIHelper.setMainLinearManagers(requireActivity())
            adapter = PaybillActionsAdapter(paybills, this@PaybillListFragment)
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
        binding.savedHeader.visibility = if (show) View.VISIBLE else View.GONE
        binding.savedList.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun togglePopularPaybills(show: Boolean) {
        binding.popularHeader.visibility = if (show) View.VISIBLE else View.GONE
        binding.popularList.visibility = if (show) View.VISIBLE else View.GONE
    }

    override fun onDeletePaybill(paybill: Paybill) {
        dialog = StaxDialog(requireActivity())
                .setDialogTitle(getString(R.string.paybill_delete_header))
                .setDialogMessage(getString(R.string.paybill_delete_msg, paybill.name))
                .setNegButton(R.string.btn_cancel, null)
                .setPosButton(R.string.btn_delete) { if (activity != null) paybillViewModel.deletePaybill(paybill) }
        dialog!!.showIt()
    }

    override fun onSelectPaybill(paybill: Paybill) {
        paybillViewModel.selectPaybill(paybill)
        findNavController().popBackStack()
    }

    override fun onSelectPaybill(action: HoverAction) {
        paybillViewModel.selectPaybill(action)
        findNavController().popBackStack()
    }

    override fun onPause() {
        super.onPause()

        if (dialog != null && dialog!!.isShowing) dialog!!.dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }

}