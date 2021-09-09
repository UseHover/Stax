package com.hover.stax.transfers

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.provider.ContactsContract
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import androidx.annotation.CallSuper
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.hover.sdk.actions.HoverAction
import com.hover.sdk.permissions.PermissionHelper
import com.hover.stax.R
import com.hover.stax.account.AccountDropDown
import com.hover.stax.channels.Channel
import com.hover.stax.channels.ChannelDropdown
import com.hover.stax.channels.ChannelsViewModel
import com.hover.stax.contacts.StaxContact
import com.hover.stax.permissions.PermissionUtils
import com.hover.stax.transfers.TransactionType.Companion.type
import com.hover.stax.utils.Constants
import com.hover.stax.utils.UIHelper
import com.hover.stax.utils.Utils
import com.hover.stax.views.StaxCardView
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import timber.log.Timber


abstract class AbstractFormFragment : Fragment() {

    lateinit var abstractFormViewModel: AbstractFormViewModel
    val channelsViewModel: ChannelsViewModel by sharedViewModel()

    var editCard: StaxCardView? = null
    private var editRequestCard: LinearLayout? = null

    private lateinit var summaryCard: StaxCardView
    var channelDropdown: ChannelDropdown? = null
    lateinit var accountDropdown: AccountDropDown
    lateinit var fab: Button

    private lateinit var noWorryText: LinearLayout


    @CallSuper
    open fun init(root: View) {
        editCard = root.findViewById(R.id.editCard)
        editRequestCard = root.findViewById(R.id.editRequestCard)
        noWorryText = root.findViewById(R.id.noworry_text)
        summaryCard = root.findViewById(R.id.summaryCard)
        fab = root.findViewById(R.id.fab)
        channelDropdown = root.findViewById(R.id.channel_dropdown)
        accountDropdown = root.findViewById(R.id.accountDropdown)
    }

    open fun startObservers(root: View) {
        accountDropdown.setListener(channelsViewModel)
        channelDropdown?.setListener(channelsViewModel)
        accountDropdown.setObservers(channelsViewModel, viewLifecycleOwner)
        channelDropdown?.setObservers(channelsViewModel, viewLifecycleOwner)
        setupActionDropdownObservers(channelsViewModel, viewLifecycleOwner)
        abstractFormViewModel.isEditing.observe(viewLifecycleOwner, Observer(this::showEdit))
    }

    private fun setupActionDropdownObservers(viewModel: ChannelsViewModel, lifecycleOwner: LifecycleOwner) {
        viewModel.activeChannel.observe(lifecycleOwner, { channel: Channel -> Timber.i("Got new active channel: $channel ${channel.countryAlpha2}") })
        viewModel.channelActions.observe(lifecycleOwner, { actions: List<HoverAction?> -> Timber.i("Got new actions: %s", actions.size) })
    }

    open fun showEdit(isEditing: Boolean) {
        channelDropdown?.highlighted?.let { channelsViewModel.setChannelsSelected(listOf(it)) }

        editCard?.visibility = if (isEditing) View.VISIBLE else View.GONE
        editRequestCard?.visibility = if(isEditing) View.VISIBLE else View.GONE

        noWorryText.visibility = if (isEditing) View.VISIBLE else View.GONE
        summaryCard.visibility = if (isEditing) View.GONE else View.VISIBLE
        fab.text = if (isEditing) getString(R.string.btn_continue) else if (type == HoverAction.AIRTIME) getString(R.string.fab_airtimenow) else getString(R.string.fab_transfernow)
    }

    open fun contactPicker(requestCode: Int, c: Context) {
        Utils.logAnalyticsEvent(getString(R.string.try_contact_select), c)

        if (PermissionUtils.hasContactPermission(c))
            startContactIntent(requestCode);
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
            Utils.logAnalyticsEvent(getString(R.string.contact_perm_success), requireContext())
            startContactIntent(requestCode)
        } else {
            Utils.logAnalyticsEvent(getString(R.string.contact_perm_denied), requireContext());
            UIHelper.flashMessage(requireContext(), getString(R.string.toast_error_contactperm));
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode != Constants.ADD_SERVICE && resultCode == Activity.RESULT_OK) {
            val staxContact = StaxContact(data, requireContext())
            staxContact.accountNumber?.let {
                Utils.logAnalyticsEvent(getString(R.string.contact_select_success), requireContext())
                onContactSelected(requestCode, staxContact);
            } ?: run {
                Utils.logAnalyticsEvent(getString(R.string.contact_select_error), requireContext());
                UIHelper.flashMessage(requireContext(), getString(R.string.toast_error_contactselect));
            }
        }
    }

    abstract fun onContactSelected(requestCode: Int, contact: StaxContact)
}