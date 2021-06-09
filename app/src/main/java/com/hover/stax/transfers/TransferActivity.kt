package com.hover.stax.transfers

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.Observer
import com.hover.sdk.actions.HoverAction
import com.hover.stax.R
import com.hover.stax.actions.ActionSelectViewModel
import com.hover.stax.channels.ChannelsViewModel
import com.hover.stax.contacts.StaxContact
import com.hover.stax.hover.HoverSession
import com.hover.stax.navigation.AbstractNavigationActivity
import com.hover.stax.pushNotification.PushNotificationTopicsInterface
import com.hover.stax.schedules.Schedule
import com.hover.stax.schedules.ScheduleDetailViewModel
import com.hover.stax.utils.Constants
import com.hover.stax.utils.Utils
import com.hover.stax.views.StaxDialog
import org.koin.androidx.viewmodel.ext.android.getViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class TransferActivity : AbstractNavigationActivity(), PushNotificationTopicsInterface {

    private val actionSelectViewModel: ActionSelectViewModel by viewModel()

    private val channelsViewModel: ChannelsViewModel by viewModel()
    private val transferViewModel: TransferViewModel by viewModel()
    private lateinit var scheduleViewModel: ScheduleDetailViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        intent.action?.let {
            transferViewModel.setTransactionType(it)
            channelsViewModel.type = it
        }

        checkIntent()
        setContentView(R.layout.activity_transfer)

        setUpNav()
    }

    private fun checkIntent() {
        when {
            intent.hasExtra(Schedule.SCHEDULE_ID) -> createFromSchedule(intent.getIntExtra(Schedule.SCHEDULE_ID, -1))
            intent.hasExtra(Constants.REQUEST_LINK) -> createFromRequest(intent.getStringExtra(Constants.REQUEST_LINK)!!)
            else -> Utils.logAnalyticsEvent(getString(R.string.visit_screen, intent.action), this)
        }
    }

    private fun createFromSchedule(scheduleId: Int) {
        scheduleViewModel = getViewModel()
        with(scheduleViewModel) {
            action.observe(this@TransferActivity, Observer { it?.let { actionSelectViewModel.setActiveAction(it) } })
            schedule.observe(this@TransferActivity, Observer { it?.let { transferViewModel.view(it) } })
            setSchedule(scheduleId)
        }

        Utils.logAnalyticsEvent(getString(R.string.clicked_schedule_notification), this)
    }

    private fun createFromRequest(link: String) {
        transferViewModel.decrypt(link)
        observeRequest()
        Utils.logAnalyticsEvent(getString(R.string.clicked_request_link), this)
    }

    private fun observeRequest() {
        val alertDialog = StaxDialog(this).setDialogMessage(R.string.loading_link_dialoghead).showIt()
        transferViewModel.request.observe(this@TransferActivity, Observer { it?.let { alertDialog?.dismiss() } })
    }

    fun submit() = actionSelectViewModel.activeAction.value?.let { makeHoverCall(it) }

    private fun makeHoverCall(action: HoverAction) {
        Utils.logAnalyticsEvent(getString(R.string.finish_transfer, TransactionType.type), this)
        updatePushNotifGroupStatus()

        transferViewModel.checkSchedule()
        makeCall(action)
    }

    private fun makeCall(action: HoverAction) {
        val hsb = HoverSession.Builder(action, channelsViewModel.activeChannel.value, this, Constants.TRANSFER_REQUEST)
            .extra(HoverAction.AMOUNT_KEY, transferViewModel.amount.value)
            .extra(HoverAction.NOTE_KEY, transferViewModel.note.value)

        transferViewModel.contact.value?.let { addRecipientInfo(hsb) }
        hsb.run()
    }

    private fun addRecipientInfo(hsb: HoverSession.Builder) {
        hsb.extra(HoverAction.ACCOUNT_KEY, transferViewModel.contact.value!!.phoneNumber)
            .extra(
                HoverAction.PHONE_KEY, transferViewModel.contact.value!!
                    .getNumberFormatForInput(
                        actionSelectViewModel.activeAction.value!!,
                        channelsViewModel.activeChannel.value!!
                    )
            )
    }

    private fun updatePushNotifGroupStatus() {
        joinAnyTransactionNotifGroup(this)
        stopReceivingNoActivityTopicNotifGroup(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == Constants.TRANSFER_REQUEST)
            returnResult(requestCode, resultCode, data)
    }

    private fun returnResult(type: Int, result: Int, data: Intent?){
        val i = data?.let { Intent(it) } ?: Intent()
        transferViewModel.contact.value?.let { i.putExtra(StaxContact.LOOKUP_KEY, it.lookupKey) }
        i.action = if(type == Constants.SCHEDULE_REQUEST) Constants.SCHEDULED else Constants.TRANSFERED
        setResult(result, i)
    }

    override fun onBackPressed() = if(transferViewModel.isEditing.value == false) transferViewModel.setEditing(true) else super.onBackPressed()

}