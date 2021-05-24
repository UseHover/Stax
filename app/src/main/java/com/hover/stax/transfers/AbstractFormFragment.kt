package com.hover.stax.transfers

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.provider.ContactsContract
import android.view.View
import android.widget.LinearLayout
import androidx.annotation.CallSuper
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.hover.sdk.actions.HoverAction
import com.hover.sdk.permissions.PermissionHelper
import com.hover.stax.R
import com.hover.stax.channels.Channel
import com.hover.stax.channels.ChannelDropdown
import com.hover.stax.channels.ChannelDropdownViewModel
import com.hover.stax.contacts.StaxContact
import com.hover.stax.permissions.PermissionUtils
import com.hover.stax.transfers.TransactionType.Companion.type
import com.hover.stax.utils.Constants
import com.hover.stax.utils.UIHelper
import com.hover.stax.utils.Utils
import com.hover.stax.views.StaxCardView
import timber.log.Timber


abstract class AbstractFormFragment : Fragment() {

    lateinit var abstractFormViewModel: AbstractFormViewModel
    lateinit var channelDropdownViewModel: ChannelDropdownViewModel

    lateinit var editCard: StaxCardView
    lateinit var summaryCard: StaxCardView
    lateinit var channelDropdown: ChannelDropdown
    lateinit var fab: ExtendedFloatingActionButton

    private lateinit var noWorryText: LinearLayout

    @CallSuper
    open fun init(root: View) {
        editCard = root.findViewById(R.id.editCard)
        noWorryText = root.findViewById(R.id.noworry_text)
        summaryCard = root.findViewById(R.id.summaryCard)
        fab = root.findViewById(R.id.fab)
        channelDropdown = root.findViewById(R.id.channel_dropdown)
    }

    open fun startObservers(root: View) {
        channelDropdown.setListener(channelDropdownViewModel);
        channelDropdown.setObservers(channelDropdownViewModel, viewLifecycleOwner);
        setupActionDropdownObservers(channelDropdownViewModel, viewLifecycleOwner);
        abstractFormViewModel.isEditing.observe(viewLifecycleOwner, Observer(this::showEdit));
    }

    private fun setupActionDropdownObservers(viewModel: ChannelDropdownViewModel, lifecycleOwner: LifecycleOwner) {
        viewModel.activeChannel.observe(lifecycleOwner, Observer { channel: Channel -> Timber.i("Got new active channel: $channel ${channel.countryAlpha2}") })
        viewModel.channelActions.observe(lifecycleOwner, Observer { actions: List<HoverAction?> -> Timber.i("Got new actions: %s", actions.size) })
    }

    open fun showEdit(isEditing: Boolean) {
        channelDropdownViewModel.setChannelSelected(channelDropdown.highlighted)
        editCard.visibility = if (isEditing) View.VISIBLE else View.GONE
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
            staxContact.phoneNumber?.let {
                Utils.logAnalyticsEvent(getString(R.string.contact_select_success), requireContext());
                onContactSelected(requestCode, staxContact);
            } ?: run {
                Utils.logAnalyticsEvent(getString(R.string.contact_select_error), requireContext());
                UIHelper.flashMessage(requireContext(), getString(R.string.toast_error_contactselect));
            }
        }
    }

    abstract fun onContactSelected(requestCode: Int, contact: StaxContact?)
}