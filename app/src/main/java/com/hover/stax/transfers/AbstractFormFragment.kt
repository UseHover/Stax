package com.hover.stax.transfers

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.CallSuper
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import com.hover.sdk.actions.HoverAction
import com.hover.sdk.permissions.PermissionHelper
import com.hover.stax.R
import com.hover.stax.accounts.AccountDropdown
import com.hover.stax.actions.ActionSelectViewModel
import com.hover.stax.accounts.AccountsViewModel
import com.hover.stax.contacts.StaxContact
import com.hover.stax.permissions.PermissionUtils
import com.hover.stax.transfers.TransactionType.Companion.type
import com.hover.stax.utils.AnalyticsUtil
import com.hover.stax.utils.NavUtil
import com.hover.stax.utils.UIHelper
import com.hover.stax.views.AbstractStatefulInput
import com.hover.stax.views.StaxCardView
import com.hover.stax.views.StaxDialog
import com.hover.stax.views.StaxTextInput
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import timber.log.Timber

abstract class AbstractFormFragment : Fragment() {

    lateinit var abstractFormViewModel: AbstractFormViewModel
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
        abstractFormViewModel.reset()
    }

    @CallSuper
    open fun init(root: View) {
        editCard = root.findViewById(R.id.editCard)
        noWorryText = root.findViewById(R.id.noWorryText)
        summaryCard = root.findViewById(R.id.summaryCard)
        fab = root.findViewById(R.id.fab)
        payWithDropdown = root.findViewById(R.id.payWithDropdown)
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, backPressedCallback)
    }

    @CallSuper
    open fun startObservers(root: View) {
        summaryCard?.setOnClickIcon { abstractFormViewModel.setEditing(true) }
        payWithDropdown.setListener(accountsViewModel)
        payWithDropdown.setObservers(accountsViewModel, viewLifecycleOwner)
        setupActionDropdownObservers()
        abstractFormViewModel.isEditing.observe(viewLifecycleOwner, Observer(this::showEdit))
    }

    private fun setupActionDropdownObservers() {
        accountsViewModel.activeAccount.observe(viewLifecycleOwner) { Timber.v("Got new active account ${this.javaClass.simpleName}: $it") }
        accountsViewModel.channelActions.observe(viewLifecycleOwner) { Timber.v("Got new actions ${this.javaClass.simpleName}: %s", it?.size) }
        actionSelectViewModel.activeAction.observe(viewLifecycleOwner) { Timber.v("Got new active action ${this.javaClass.simpleName}: $it ${it?.public_id}") }
    }

    @CallSuper
    open fun showEdit(isEditing: Boolean) {
        editCard?.visibility = if (isEditing) View.VISIBLE else View.GONE
        summaryCard?.visibility = if (isEditing) View.GONE else View.VISIBLE
        noWorryText.visibility = if (isEditing) View.VISIBLE else View.GONE
        fab.text = chooseFabText(isEditing)
    }

    fun setInputState(hasFocus: Boolean, input: StaxTextInput, errors: String?) {
        if (!hasFocus)
            input.setState(errors, if (errors == null) AbstractStatefulInput.SUCCESS else AbstractStatefulInput.ERROR)
        else
            input.setState(null, AbstractStatefulInput.NONE)
    }

    private fun chooseFabText(isEditing: Boolean): String {
        return if (isEditing) getString(R.string.btn_continue)
            else if (type == HoverAction.AIRTIME) getString(R.string.fab_airtimenow)
            else if (type == HoverAction.C2B) getString(R.string.fab_airtimenow)
            else getString(R.string.fab_transfernow)
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
        } else { showError(R.string.toast_error_contactperm, R.string.contact_perm_denied) }
    }

    private val contactPickerLauncher = registerForActivityResult(ActivityResultContracts.PickContact()) { data ->
        val staxContact = StaxContact(data, requireActivity())
        staxContact.accountNumber?.let {
            log(getString(R.string.contact_select_success))
            onContactSelected(staxContact)
        } ?: run { showError(R.string.toast_error_contactselect, R.string.contact_select_error) }
    }

    private fun showError(userMsg: Int, logMsg: Int) {
        log(getString(logMsg))
        UIHelper.flashMessage(requireContext(), getString(userMsg))
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
            Timber.e("Caught back press. isediting: %s", abstractFormViewModel.isEditing.value)
            if (abstractFormViewModel.isEditing.value == false)
                abstractFormViewModel.setEditing(true)
            else
                findNavController().popBackStack()
        }
    }

    override fun onDestroy() {
        abstractFormViewModel.reset()
        actionSelectViewModel.activeAction.value = null
        super.onDestroy()
    }

    private fun log(event: String) = AnalyticsUtil.logAnalyticsEvent(event, requireContext())
}