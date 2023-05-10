/*
 * Copyright 2022 Stax
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hover.stax.paybill

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.hover.sdk.actions.HoverAction
import com.hover.stax.R
import com.hover.stax.accounts.AccountsViewModel
import com.hover.stax.actions.ActionSelectViewModel
import com.hover.stax.database.models.BUSINESS_NO
import com.hover.stax.database.models.Paybill
import com.hover.stax.databinding.FragmentPaybillListBinding
import com.hover.stax.core.AnalyticsUtil
import com.hover.stax.utils.NavUtil
import com.hover.stax.utils.UIHelper
import com.hover.stax.views.StaxDialog
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class PaybillListFragment : Fragment(), PaybillAdapter.ClickListener, PaybillActionsAdapter.PaybillActionsClickListener {

    private var _binding: FragmentPaybillListBinding? = null
    private val binding get() = _binding!!

    private val accountsViewModel: AccountsViewModel by sharedViewModel()
    private val actionSelectViewModel: ActionSelectViewModel by sharedViewModel()
    private val paybillViewModel: PaybillViewModel by sharedViewModel()

    private var dialog: StaxDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPaybillListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        com.hover.stax.core.AnalyticsUtil.logAnalyticsEvent(getString(R.string.visit_screen, getString(R.string.visit_paybill_list)), requireActivity())
        updatePaybills(paybillViewModel.savedPaybills.value)
        updateActions(accountsViewModel.institutionActions.value)
        startListeners()
        startObservers()
    }

    private fun startListeners() {
        binding.contentLayout.setOnClickIcon { NavUtil.navigate(findNavController(), PaybillListFragmentDirections.actionPaybillListFragmentToPaybillFragment()) }

        binding.newPaybill.newPaybillCard.setOnClickListener {
            PaybillNumberDialog().show(childFragmentManager, PaybillNumberDialog::class.java.simpleName)
        }
    }

    private fun startObservers() {
        paybillViewModel.savedPaybills.observe(viewLifecycleOwner) { updatePaybills(it) }
        accountsViewModel.institutionActions.observe(viewLifecycleOwner) { updateActions(it) }

        actionSelectViewModel.activeAction.observe(viewLifecycleOwner) {
            it?.let { paybillViewModel.selectPaybill(it) }
        }
    }

    private fun showPopular(actions: List<HoverAction>) {
        togglePopularPaybills(true)

        binding.popularList.apply {
            layoutManager = UIHelper.setMainLinearManagers(requireActivity())
            adapter = PaybillActionsAdapter(actions.filter { it.getVarValue(BUSINESS_NO) != BUSINESS_NO }, this@PaybillListFragment)
        }
    }

    private fun updateActions(actions: List<HoverAction>?) {
        if (actions.isNullOrEmpty())
            togglePopularPaybills(false)
        else
            showPopular(actions)
    }

    private fun updatePaybills(paybills: List<Paybill>?) {
        if (paybills.isNullOrEmpty())
            toggleSavedPaybills(false)
        else {
            toggleSavedPaybills(true)

            binding.savedList.apply {
                layoutManager = UIHelper.setMainLinearManagers(requireActivity())
                adapter = PaybillAdapter(paybills, this@PaybillListFragment)
            }
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
            .setPosButton(R.string.btn_delete) {
                if (activity != null) {
                    paybillViewModel.deletePaybill(paybill)
                    UIHelper.flashAndReportMessage(requireActivity(), R.string.paybill_delete_success)
                }
            }
        dialog!!.showIt()
    }

    override fun onSelectPaybill(paybill: Paybill) {
        paybillViewModel.selectPaybill(paybill)
        actionSelectViewModel.setActiveAction(paybill.actionId)
        findNavController().popBackStack()
    }

    override fun onSelectPaybill(action: HoverAction) {
        paybillViewModel.selectPaybill(action)
        actionSelectViewModel.setActiveAction(action)
        requireActivity().supportFragmentManager.popBackStack()
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