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
package com.hover.stax.transfers

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.CallSuper
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import com.hover.sdk.actions.HoverAction
import com.hover.stax.R
import com.hover.stax.accounts.AccountDropdown
import com.hover.stax.accounts.AccountsViewModel
import com.hover.stax.actions.ActionSelectViewModel
import com.hover.stax.contacts.StaxContact
import com.hover.stax.domain.model.Account
import com.hover.stax.hover.HoverSession
import com.hover.stax.hover.TransactionContract
import com.hover.stax.permissions.PermissionUtils
import com.hover.stax.presentation.home.BalancesViewModel
import com.hover.stax.utils.AnalyticsUtil
import com.hover.stax.utils.NavUtil
import com.hover.stax.utils.UIHelper
import com.hover.stax.utils.collectLifecycleFlow
import com.hover.stax.views.AbstractStatefulInput
import com.hover.stax.views.StaxCardView
import com.hover.stax.views.StaxDialog
import com.hover.stax.views.StaxTextInput
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import timber.log.Timber

abstract class AbstractFormFragment : Fragment() {

    lateinit var abstractFormViewModel: AbstractFormViewModel
    private val balancesViewModel: BalancesViewModel by sharedViewModel()
    val accountsViewModel: AccountsViewModel by sharedViewModel()
    val actionSelectViewModel: ActionSelectViewModel by sharedViewModel()

    var editCard: View? = null
    var summaryCard: StaxCardView? = null
    lateinit var payWithDropdown: AccountDropdown
    lateinit var fab: Button

    private lateinit var noWorryText: TextView

    var dialog: StaxDialog? = null

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        resetVMs()
    }

    @CallSuper
    open fun init(root: View) {
        editCard = root.findViewById(R.id.editCard)
        noWorryText = root.findViewById(R.id.noWorryText)
        summaryCard = root.findViewById(R.id.summaryCard)
        fab = root.findViewById(R.id.fab)
        fab.setOnClickListener { fabClicked() }
        payWithDropdown = root.findViewById(R.id.payWithDropdown)
        payWithDropdown.setListener(accountsViewModel)
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, backPressedCallback)
    }

    @CallSuper
    open fun startObservers(root: View) {
        summaryCard?.setOnClickIcon { abstractFormViewModel.setEditing(true) }
        payWithDropdown.setObservers(accountsViewModel, viewLifecycleOwner)
        abstractFormViewModel.isEditing.observe(viewLifecycleOwner, Observer(this::showEdit))

        collectLifecycleFlow(balancesViewModel.balanceAction) {
            callHover(checkBalance, generateSessionBuilder(it))
        }
    }

    private fun generateSessionBuilder(action: HoverAction): HoverSession.Builder {
        return HoverSession.Builder(action, accountsViewModel.activeAccount.value!!, null, requireActivity())
    }

    private val checkBalance = registerForActivityResult(TransactionContract()) { data: Intent? ->
        if (data != null && data.extras != null && data.extras!!.getString("uuid") != null) {
            NavUtil.showTransactionDetailsFragment(findNavController(), data.extras!!.getString("uuid")!!)
        }
    }

    protected fun callHover(launcher: ActivityResultLauncher<HoverSession.Builder>, b: HoverSession.Builder) {
        try {
            launcher.launch(b)
        } catch (e: Exception) {
            requireActivity().runOnUiThread { UIHelper.flashAndReportMessage(requireContext(), getString(R.string.error_running_action)) }
            AnalyticsUtil.logErrorAndReportToFirebase(b.action.public_id, getString(R.string.error_running_action_log), e)
        }
    }

    protected fun goToDeets(data: Intent?) {
        if (data != null && data.extras != null && data.extras!!.getString("uuid") != null) {
            NavUtil.showTransactionDetailsFragment(findNavController(), data.extras!!.getString("uuid")!!)
        }
    }

    @CallSuper
    open fun showEdit(isEditing: Boolean) {
        editCard?.visibility = if (isEditing) View.VISIBLE else View.GONE
        summaryCard?.visibility = if (isEditing) View.GONE else View.VISIBLE
        noWorryText.visibility = if (isEditing) View.VISIBLE else View.GONE
        fab.text = chooseFabText(isEditing)
    }

    private fun fabClicked() {
//        if (accountsViewModel.activeAccount.value != null && !accountsViewModel.isValidAccount())
//            askToCheckBalance(accountsViewModel.activeAccount.value!!)
//        else
        if (validates()) {
            if (abstractFormViewModel.isEditing.value == true) {
                onFinishForm()
            } else {
                onSubmitForm()
            }
        } else UIHelper.flashAndReportMessage(requireActivity(), getString(R.string.toast_pleasefix))
    }

    abstract fun validates(): Boolean

    abstract fun onFinishForm()

    abstract fun onSubmitForm()

    private fun askToCheckBalance(account: Account) {
        val dialog = StaxDialog(layoutInflater)
            .setDialogTitle(R.string.finish_adding_title)
            .setDialogMessage(getString(R.string.finish_adding_desc, account.userAlias))
            .setNegButton(R.string.btn_cancel, null)
            .setPosButton(R.string.connect_cta) { balancesViewModel.requestAction(account, HoverAction.BALANCE) }
        dialog.showIt()
    }

    fun setInputState(hasFocus: Boolean, input: StaxTextInput, errors: String?) {
        if (!hasFocus)
            input.setState(errors, if (errors == null) AbstractStatefulInput.SUCCESS else AbstractStatefulInput.ERROR)
        else
            input.setState(null, AbstractStatefulInput.NONE)
    }

    private fun chooseFabText(isEditing: Boolean): String {
        return when {
            isEditing -> getString(R.string.btn_continue)
            accountsViewModel.getActionType() == HoverAction.AIRTIME -> getString(R.string.fab_airtimenow)
            accountsViewModel.getActionType() == HoverAction.BUY_DATA -> getString(R.string.fab_datanow)
            accountsViewModel.getActionType() == HoverAction.P2P ||
                accountsViewModel.getActionType() == HoverAction.MERCHANT ||
                accountsViewModel.getActionType() == HoverAction.BILL -> getString(R.string.fab_transfernow)
            else -> getString(R.string.fab_submit)
        }
    }

    open fun startContactPicker(c: Context) {
        AnalyticsUtil.logAnalyticsEvent(getString(R.string.try_contact_select), c)

        if (PermissionUtils.hasContactPermission(c))
            contactPickerLauncher.launch(null)
        else
            requestPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
    }

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            log(getString(R.string.contact_perm_success))
            contactPickerLauncher.launch(null)
        } else {
            showError(R.string.toast_error_contactperm, R.string.contact_perm_denied)
        }
    }

    private val contactPickerLauncher = registerForActivityResult(ActivityResultContracts.PickContact()) { data ->
        val staxContact = data?.let { StaxContact(data, requireActivity()) }
        staxContact?.id?.let {
            log(getString(R.string.contact_select_success))
            onContactSelected(staxContact)
        } ?: run { showError(R.string.toast_error_contactselect, R.string.contact_select_error) }
    }

    private fun showError(userMsg: Int, logMsg: Int) {
        log(getString(logMsg))
        UIHelper.flashAndReportMessage(requireContext(), getString(userMsg))
    }

    abstract fun onContactSelected(contact: StaxContact)

    override fun onPause() {
        super.onPause()
        abstractFormViewModel.setEditing(true)
    }

    @SuppressLint("ClickableViewAccessibility")
    fun setDropdownTouchListener(navDirections: NavDirections) {
        payWithDropdown.autoCompleteTextView.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN)
                NavUtil.navigate(findNavController(), navDirections)
            true
        }
    }

    private val backPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            Timber.e("Caught back press. is editing: %s", abstractFormViewModel.isEditing.value)
            if (abstractFormViewModel.isEditing.value == false)
                abstractFormViewModel.setEditing(true)
            else
                findNavController().popBackStack()
        }
    }

    private fun resetVMs() {
        abstractFormViewModel.reset()
        accountsViewModel.reset()
        actionSelectViewModel.activeAction.value = null
    }

    override fun onDestroy() {
        resetVMs()
        super.onDestroy()
    }

    private fun log(event: String) = AnalyticsUtil.logAnalyticsEvent(event, requireContext())
}