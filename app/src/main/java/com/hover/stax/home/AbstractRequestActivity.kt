package com.hover.stax.home

import android.content.Intent
import android.os.Handler
import android.view.View
import com.hover.stax.R
import com.hover.stax.actions.ActionSelectViewModel
import com.hover.stax.requests.NewRequestViewModel
import com.hover.stax.requests.RequestSenderInterface
import com.hover.stax.requests.SmsSentObserver
import com.hover.stax.schedules.ScheduleDetailViewModel
import com.hover.stax.transfers.TransferViewModel
import com.hover.stax.utils.AnalyticsUtil
import com.hover.stax.utils.Constants
import org.koin.androidx.viewmodel.ext.android.viewModel

abstract class AbstractRequestActivity : AbstractHoverCallerActivity(), RequestSenderInterface, SmsSentObserver.SmsSentListener {

    private val requestViewModel: NewRequestViewModel by viewModel()
    private val scheduleViewModel: ScheduleDetailViewModel by viewModel()
    private val actionSelectViewModel: ActionSelectViewModel by viewModel()
    private val transferViewModel: TransferViewModel by viewModel()

    fun createFromSchedule(scheduleId: Int, isRequestType: Boolean) {
        with(scheduleViewModel) {
            if (isRequestType) {
                schedule.observe(this@AbstractRequestActivity) { it?.let { requestViewModel.setSchedule(it) } }
                AnalyticsUtil.logAnalyticsEvent(getString(com.hover.stax.R.string.clicked_schedule_notification), this@AbstractRequestActivity)
            } else {
                action.observe(this@AbstractRequestActivity) { it?.let { actionSelectViewModel.setActiveAction(it) } }
                schedule.observe(this@AbstractRequestActivity) { it?.let { transferViewModel.view(it) } }
            }

            setSchedule(scheduleId)
        }

        AnalyticsUtil.logAnalyticsEvent(getString(R.string.clicked_schedule_notification), this)
    }

    fun sendSms() {
        requestViewModel.saveRequest()
        SmsSentObserver(this, listOf(requestViewModel.requestee.value), Handler(), this).start()
        sendSms(requestViewModel.formulatedRequest.value, listOf(requestViewModel.requestee.value), this)
    }

    fun sendWhatsapp() {
        requestViewModel.saveRequest()
        sendWhatsapp(requestViewModel.formulatedRequest.value, listOf(requestViewModel.requestee.value), requestViewModel.activeChannel.value, this)
    }

    fun copyShareLink(view: View) {
        requestViewModel.saveRequest()
        copyShareLink(requestViewModel.formulatedRequest.value, view.findViewById(R.id.copylink_share_selection), this)
    }

    override fun onSmsSendEvent(sent: Boolean) {
        if (sent) onFinished(Constants.SMS)
    }

    private fun onFinished(type: Int) = setResult(RESULT_OK, createSuccessIntent(type))

    private fun createSuccessIntent(type: Int): Intent =
            Intent().apply { action = if (type == Constants.SCHEDULE_REQUEST) Constants.SCHEDULED else Constants.TRANSFERRED }

    fun cancel() = setResult(RESULT_CANCELED)
}