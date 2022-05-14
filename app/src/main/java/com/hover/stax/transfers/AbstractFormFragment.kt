package com.hover.stax.transfers

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.CallSuper
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import com.hover.sdk.actions.HoverAction
import com.hover.stax.R
import com.hover.stax.accounts.Account
import com.hover.stax.accounts.AccountDropdown
import com.hover.stax.channels.Channel
import com.hover.stax.channels.ChannelsViewModel
import com.hover.stax.contacts.StaxContact
import com.hover.stax.home.MainActivity
import com.hover.stax.permissions.PermissionUtils
import com.hover.stax.transfers.TransactionType.Companion.type
import com.hover.stax.utils.AnalyticsUtil
import com.hover.stax.utils.NavUtil
import com.hover.stax.utils.UIHelper
import com.hover.stax.views.StaxCardView
import com.hover.stax.views.StaxDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import timber.log.Timber


abstract class AbstractFormFragment : Fragment(), AccountDropdown.AccountFetchListener {

    lateinit var abstractFormViewModel: AbstractFormViewModel
    val channelsViewModel: ChannelsViewModel by sharedViewModel()

    var editCard: StaxCardView? = null
    private var editRequestCard: LinearLayout? = null

    private lateinit var summaryCard: StaxCardView
    lateinit var accountDropdown: AccountDropdown
    lateinit var fab: Button

    private lateinit var noWorryText: TextView

    var dialog: StaxDialog? = null

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            log(getString(R.string.contact_perm_success))
            contactPickerLauncher.launch(null)
        } else {
            log(getString(R.string.contact_perm_denied))
            UIHelper.flashMessage(requireContext(), getString(R.string.toast_error_contactperm))
        }
    }

    private val contactPickerLauncher = registerForActivityResult(ActivityResultContracts.PickContact()) { data ->
        val staxContact = StaxContact(data, requireActivity())
        staxContact.accountNumber?.let {
            log(getString(R.string.contact_select_success))
            onContactSelected(staxContact)
        } ?: run {
            log(getString(R.string.contact_select_error))
            UIHelper.flashMessage(requireContext(), getString(R.string.toast_error_contactselect))
        }
    }

    @CallSuper
    open fun init(root: View) {
        editCard = root.findViewById(R.id.editCard)
        editRequestCard = root.findViewById(R.id.editRequestCard)
        noWorryText = root.findViewById(R.id.noWorryText)
        summaryCard = root.findViewById(R.id.summaryCard)
        fab = root.findViewById(R.id.fab)
        accountDropdown = root.findViewById(R.id.accountDropdown)
    }

    open fun startObservers(root: View) {
        accountDropdown.setListener(channelsViewModel)
        accountDropdown.setObservers(channelsViewModel, viewLifecycleOwner)
        setupActionDropdownObservers()
        abstractFormViewModel.isEditing.observe(viewLifecycleOwner, Observer(this::showEdit))
    }

    private fun setupActionDropdownObservers() {
        val activeChannelObserver = object : Observer<Channel?> {
            override fun onChanged(t: Channel?) {
                Timber.e("Got new active channel ${this.javaClass.simpleName}, ${t?.countryAlpha2}")
            }
        }

        val actionsObserver = object : Observer<List<HoverAction>> {
            override fun onChanged(t: List<HoverAction>?) {
                Timber.e("Got new actions ${this.javaClass.simpleName}: %s", t?.size)
            }
        }

        channelsViewModel.activeChannel.observe(viewLifecycleOwner, activeChannelObserver)
        channelsViewModel.channelActions.observe(viewLifecycleOwner, actionsObserver)
    }


    open fun showEdit(isEditing: Boolean) {
        Timber.e("Is editing : $isEditing")

        editCard?.visibility = if (isEditing) View.VISIBLE else View.GONE
        editRequestCard?.visibility = if (isEditing) View.VISIBLE else View.GONE

        noWorryText.visibility = if (isEditing) View.VISIBLE else View.GONE
        summaryCard.visibility = if (isEditing) View.GONE else View.VISIBLE
        fab.text = if (isEditing) getString(R.string.btn_continue) else if (type == HoverAction.AIRTIME) getString(R.string.fab_airtimenow) else getString(R.string.fab_transfernow)
    }

    open fun contactPicker(c: Context) {
        AnalyticsUtil.logAnalyticsEvent(getString(R.string.try_contact_select), c)

        if (PermissionUtils.hasContactPermission(c))
            contactPickerLauncher.launch(null)
        else
            requestPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
    }

    abstract fun onContactSelected(contact: StaxContact)

    @SuppressLint("ClickableViewAccessibility")
    fun setDropdownTouchListener(navDirections: NavDirections) {
        accountDropdown.autoCompleteTextView.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN)
                NavUtil.navigate(findNavController(), navDirections)
            true
        }
    }

    override fun fetchAccounts(account: Account) {
        if(dialog == null) {
            dialog = StaxDialog(requireActivity())
                .setDialogTitle(getString(R.string.incomplete_account_setup_header))
                .setDialogMessage(getString(R.string.incomplete_account_setup_desc, account.alias))
                .setPosButton(R.string.check_balance_title) { runBalanceCheck(account.channelId) }
                .setNegButton(R.string.btn_cancel, null)
            dialog!!.showIt()
        }
    }

    private fun runBalanceCheck(channelId: Int) = lifecycleScope.launch(Dispatchers.IO) {
        channelsViewModel.getChannel(channelId)?.let { channel ->
            val action = channelsViewModel.getFetchAccountAction(channelId)
            channelsViewModel.setActiveChannel(channel)

            if (action != null)
                (activity as? MainActivity)?.makeCall(action, channel)
            else
                UIHelper.flashMessage(requireActivity(), getString(R.string.action_run_error))
        }
    }

    private fun log(event: String) = AnalyticsUtil.logAnalyticsEvent(event, requireContext())

}