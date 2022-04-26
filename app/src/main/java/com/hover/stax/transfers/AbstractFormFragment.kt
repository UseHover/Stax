package com.hover.stax.transfers

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.provider.ContactsContract
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.CallSuper
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import com.hover.sdk.actions.HoverAction
import com.hover.sdk.permissions.PermissionHelper
import com.hover.stax.R
import com.hover.stax.accounts.AccountDropdown
import com.hover.stax.channels.ChannelsViewModel
import com.hover.stax.contacts.StaxContact
import com.hover.stax.permissions.PermissionUtils
import com.hover.stax.transfers.TransactionType.Companion.type
import com.hover.stax.utils.AnalyticsUtil
import com.hover.stax.utils.Constants
import com.hover.stax.utils.NavUtil
import com.hover.stax.utils.UIHelper
import com.hover.stax.views.StaxCardView
import com.hover.stax.views.StaxDialog
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import timber.log.Timber


abstract class AbstractFormFragment : Fragment() {

    lateinit var abstractFormViewModel: AbstractFormViewModel
    val channelsViewModel: ChannelsViewModel by sharedViewModel()

    var editCard: StaxCardView? = null
    private var editRequestCard: LinearLayout? = null

    private lateinit var summaryCard: StaxCardView
    lateinit var payWithDropdown: AccountDropdown
    lateinit var fab: Button

    private lateinit var noWorryText: TextView

    var dialog: StaxDialog? = null

    @CallSuper
    open fun init(root: View) {
        editCard = root.findViewById(R.id.editCard)
        editRequestCard = root.findViewById(R.id.editRequestCard)
        noWorryText = root.findViewById(R.id.noWorryText)
        summaryCard = root.findViewById(R.id.summaryCard)
        fab = root.findViewById(R.id.fab)
        payWithDropdown = root.findViewById(R.id.payWithDropdown)
    }

    @CallSuper
    open fun startObservers(root: View) {
        summaryCard.setOnClickIcon { abstractFormViewModel.setEditing(true) }
        payWithDropdown.setListener(channelsViewModel)
        payWithDropdown.setObservers(channelsViewModel, viewLifecycleOwner)
        setupActionDropdownObservers()
        abstractFormViewModel.isEditing.observe(viewLifecycleOwner, Observer(this::showEdit))
    }

    private fun setupActionDropdownObservers() {
        channelsViewModel.activeChannel.observe(viewLifecycleOwner) { Timber.e("Got new active channel ${this.javaClass.simpleName}: $it ${it?.countryAlpha2}") }
        channelsViewModel.channelActions.observe(viewLifecycleOwner) { Timber.e("Got new actions ${this.javaClass.simpleName}: %s", it?.size) }
    }


    open fun showEdit(isEditing: Boolean) {
        editCard?.visibility = if (isEditing) View.VISIBLE else View.GONE
        editRequestCard?.visibility = if (isEditing) View.VISIBLE else View.GONE

        noWorryText.visibility = if (isEditing) View.VISIBLE else View.GONE
        summaryCard.visibility = if (isEditing) View.GONE else View.VISIBLE
        fab.text = chooseFabText(isEditing)
    }

    fun chooseFabText(isEditing: Boolean): String {
        return if (isEditing) getString(R.string.btn_continue)
            else if (type == HoverAction.AIRTIME) getString(R.string.fab_airtimenow)
            else if (type == HoverAction.C2B) getString(R.string.fab_airtimenow)
            else getString(R.string.fab_transfernow)
    }

    open fun contactPicker(requestCode: Int, c: Context) {
        AnalyticsUtil.logAnalyticsEvent(getString(R.string.try_contact_select), c)

        if (PermissionUtils.hasContactPermission(c))
            startContactIntent(requestCode)
        else
            requestPermissions(arrayOf(Manifest.permission.READ_CONTACTS), requestCode)
    }

    private fun startContactIntent(requestCode: Int) {
        val contactPickerIntent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
        startActivityForResult(contactPickerIntent, requestCode)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (PermissionHelper(requireContext()).permissionsGranted(grantResults)) {
            AnalyticsUtil.logAnalyticsEvent(getString(R.string.contact_perm_success), requireContext())
            startContactIntent(requestCode)
        } else {
            AnalyticsUtil.logAnalyticsEvent(getString(R.string.contact_perm_denied), requireContext())
            UIHelper.flashMessage(requireContext(), getString(R.string.toast_error_contactperm))
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode != Constants.ADD_SERVICE && resultCode == Activity.RESULT_OK) {
            val staxContact = StaxContact(data, requireContext())
            staxContact.accountNumber?.let {
                AnalyticsUtil.logAnalyticsEvent(getString(R.string.contact_select_success), requireContext())
                onContactSelected(requestCode, staxContact)
            } ?: run {
                AnalyticsUtil.logAnalyticsEvent(getString(R.string.contact_select_error), requireContext())
                UIHelper.flashMessage(requireContext(), getString(R.string.toast_error_contactselect))
            }
        }
    }

    abstract fun onContactSelected(requestCode: Int, contact: StaxContact)

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
}