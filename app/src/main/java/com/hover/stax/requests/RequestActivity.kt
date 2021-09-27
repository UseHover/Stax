package com.hover.stax.requests

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.hover.sdk.actions.HoverAction
import com.hover.sdk.permissions.PermissionHelper
import com.hover.stax.R
import com.hover.stax.channels.Channel
import com.hover.stax.databinding.ActivityRequestBinding
import com.hover.stax.hover.HoverSession
import com.hover.stax.navigation.AbstractNavigationActivity
import com.hover.stax.schedules.Schedule
import com.hover.stax.schedules.ScheduleDetailViewModel
import com.hover.stax.utils.Constants
import com.hover.stax.utils.UIHelper
import com.hover.stax.utils.Utils
import com.hover.stax.views.StaxDialog
import org.koin.androidx.viewmodel.ext.android.viewModel

class RequestActivity : AbstractNavigationActivity(), RequestSenderInterface, SmsSentObserver.SmsSentListener {

    private val requestViewModel: NewRequestViewModel by viewModel()
    private val scheduleViewModel: ScheduleDetailViewModel by viewModel()

    private var dialog: AlertDialog? = null

    private lateinit var binding: ActivityRequestBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRequestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setUpNav()

        checkIntent()
    }

    private fun checkIntent(){
        if(intent.hasExtra(Schedule.SCHEDULE_ID))
            createFromSchedule(intent.getIntExtra(Schedule.SCHEDULE_ID, -1))
        else
            Utils.logAnalyticsEvent(getString(R.string.visit_screen, getString(R.string.visit_new_request)), this)
    }

    private fun createFromSchedule(scheduleId: Int) {
        with(scheduleViewModel) {
            schedule.observe(this@RequestActivity, { it?.let { requestViewModel.setSchedule(it) } })
            setSchedule(scheduleId)
        }
        Utils.logAnalyticsEvent(getString(R.string.clicked_schedule_notification), this)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == Constants.SMS && PermissionHelper(this).permissionsGranted(grantResults)){
            Utils.logAnalyticsEvent(getString(R.string.perms_sms_granted), this)
            sendSms()
        }else if(requestCode == Constants.SMS){
            Utils.logAnalyticsEvent(getString(R.string.perms_sms_denied), this)
            UIHelper.flashMessage(this, getString(R.string.toast_error_smsperm))
        }
    }

    fun sendSms(){
        requestViewModel.saveRequest()
        SmsSentObserver(this, requestViewModel.requestees.value, Handler(), this).start()
        sendSms(requestViewModel.formulatedRequest.value, requestViewModel.requestees.value, this)
    }

    fun sendWhatsapp(){
        requestViewModel.saveRequest()
        sendWhatsapp(requestViewModel.formulatedRequest.value, requestViewModel.requestees.value, requestViewModel.activeChannel.value, this)
    }

    fun copyShareLink(view: View) {
        requestViewModel.saveRequest()
        copyShareLink(requestViewModel.formulatedRequest.value, view.findViewById(R.id.copylink_share_selection), this)
    }

    override fun onSmsSendEvent(sent: Boolean) {
        if(sent) onFinished(Constants.SMS)
    }

    private fun onFinished(type: Int){
        setResult(RESULT_OK, createSuccessIntent(type))
        finish()
    }

    private fun createSuccessIntent(type: Int): Intent =
        Intent().apply { action = if(type == Constants.SCHEDULE_REQUEST) Constants.SCHEDULED else Constants.TRANSFERRED }

    private fun cancel(){
        setResult(RESULT_CANCELED)
        finish()
    }

    override fun onStop() {
        super.onStop()
        dialog?.dismiss()
    }

    override fun onBackPressed() {
        when {
            !requestViewModel.isEditing.value!! && requestViewModel.formulatedRequest.value == null -> requestViewModel.setEditing(true)
            !requestViewModel.isEditing.value!! && requestViewModel.formulatedRequest.value == null -> askAreYouSure()
            else -> super.onBackPressed()
        }
    }

    private fun askAreYouSure(){
        dialog = StaxDialog(this)
            .setDialogTitle(R.string.reqsave_head)
            .setDialogMessage(R.string.reqsave_msg)
            .setPosButton(R.string.btn_save) { saveUnsent() }
            .setNegButton(R.string.btn_dontsave) { cancel() }
            .showIt()

    }

    private fun saveUnsent(){
        requestViewModel.saveRequest()
        Utils.logAnalyticsEvent(getString(R.string.saved_unsent_request), this)
        super.onBackPressed()
    }

    fun makeCall(action: HoverAction, channel: Channel){
        val hsb = HoverSession.Builder(action, channel , this, Constants.REQUEST_REQUEST)
        hsb.run()
    }
}