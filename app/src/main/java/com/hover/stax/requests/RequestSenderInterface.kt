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
package com.hover.stax.requests

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.hover.stax.R
import com.hover.stax.contacts.StaxContact
import com.hover.stax.domain.model.USSDAccount
import com.hover.stax.utils.AnalyticsUtil.logAnalyticsEvent
import com.hover.stax.utils.UIHelper.flashAndReportMessage
import com.hover.stax.utils.Utils.copyToClipboard

const val REQUEST_LINK = "request_link"
const val SMS = 303

interface RequestSenderInterface : SmsSentObserver.SmsSentListener {

    fun sendSms(requestViewModel: NewRequestViewModel, activity: Activity) {
        requestViewModel.saveRequest()
        SmsSentObserver(this, listOf(requestViewModel.requestee.value), Handler(), requestViewModel.getApplication()).start()
        sendSms(requestViewModel.formulatedRequest.value, listOf(requestViewModel.requestee.value), activity)
    }

    fun sendWhatsapp(requestViewModel: NewRequestViewModel, activity: Activity) {
        requestViewModel.saveRequest()
        sendWhatsapp(requestViewModel.formulatedRequest.value, listOf(requestViewModel.requestee.value), requestViewModel.activeAccount.value, activity)
    }

    fun copyShareLink(view: View, requestViewModel: NewRequestViewModel, activity: Activity) {
        requestViewModel.saveRequest()
        copyShareLink(requestViewModel.formulatedRequest.value, view.findViewById(R.id.copylink_share_selection), activity)
    }

    override fun onSmsSendEvent(sent: Boolean) {
        // TODO: show message, end fragment
    }

    fun sendSms(r: Request?, requestees: List<StaxContact?>?, a: Activity) {
        if (r == null || requestees == null) {
            showError(a)
            return
        }

        val sendIntent = Intent().apply {
            action = Intent.ACTION_VIEW
            data = Uri.parse("smsto:" + r.generateRecipientString(requestees.filterNotNull()))
            putExtra(Intent.EXTRA_TEXT, r.generateMessage(a))
            putExtra("sms_body", r.generateMessage(a))
        }

        logAnalyticsEvent(a.getString(R.string.clicked_send_sms_request), a)
        a.startActivityForResult(Intent.createChooser(sendIntent, "Request"), SMS)
    }

    fun sendWhatsapp(r: Request?, requestees: List<StaxContact?>?, account: USSDAccount?, a: Activity) {
        if (r == null || requestees == null) {
            showError(a)
            return
        }
        logAnalyticsEvent(a.getString(R.string.clicked_send_whatsapp_request), a)
        if (requestees.size == 1) sendWhatsAppToSingleContact(r, requestees.filterNotNull(), account, a) else sendWhatsAppToMultipleContacts(r.generateMessage(a), a)
    }

    fun sendWhatsAppToSingleContact(
        r: Request,
        requestees: List<StaxContact>,
        account: USSDAccount?,
        a: Activity
    ) {
        val sendIntent = Intent()
        sendIntent.action = Intent.ACTION_VIEW
        val whatsapp = "https://api.whatsapp.com/send?phone=" + r.generateWhatsappRecipientString(requestees, account) + "&text=" + r.generateMessage(a)
        sendIntent.data = Uri.parse(whatsapp)
        try {
            a.startActivityForResult(sendIntent, SMS)
        } catch (ignored: ActivityNotFoundException) {
        }
    }

    fun sendWhatsAppToMultipleContacts(message: String?, a: Activity) {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, message)
            type = "text/plain"
            setPackage("com.whatsapp")
        }

        try {
            a.startActivity(sendIntent)
        } catch (ignored: ActivityNotFoundException) {
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    fun copyShareLink(r: Request?, copyBtn: TextView, c: Context) {
        when {
            r == null -> showError(c)
            copyToClipboard(r.generateMessage(c), c) -> {
                logAnalyticsEvent(c.getString(R.string.clicked_copylink_request), c)
                copyBtn.isActivated = true
                copyBtn.setCompoundDrawablesWithIntrinsicBounds(null, ContextCompat.getDrawable(c, R.drawable.img_check), null, null)
                copyBtn.text = c.getString(R.string.link_copied_label)
            }
            else -> {
                copyBtn.isActivated = false
                copyBtn.setCompoundDrawablesWithIntrinsicBounds(null, ContextCompat.getDrawable(c, R.drawable.img_copy), null, null)
                copyBtn.text = c.getString(R.string.copyLink_label)
            }
        }
    }

    fun showError(c: Context) {
        flashAndReportMessage(c, c.getString(R.string.loading_human))
    }
}