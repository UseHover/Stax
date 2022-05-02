package com.hover.stax.requests

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.view.View
import android.widget.TextView
import com.hover.stax.R
import com.hover.stax.accounts.Account
import com.hover.stax.contacts.StaxContact
import com.hover.stax.utils.AnalyticsUtil.logAnalyticsEvent
import com.hover.stax.utils.UIHelper.flashMessage
import com.hover.stax.utils.Utils.copyToClipboard

const val REQUEST_LINK = "request_link"
const val SMS = 303

interface RequestSenderInterface : SmsSentObserver.SmsSentListener {

    fun sendSms(requestViewModel: NewRequestViewModel) {
        requestViewModel.saveRequest()
        SmsSentObserver(this, listOf(requestViewModel.requestee.value), Handler(), requestViewModel.getApplication()).start()
        sendSms(requestViewModel.formulatedRequest.value, listOf(requestViewModel.requestee.value), requestViewModel.getApplication())
    }

    fun sendWhatsapp(requestViewModel: NewRequestViewModel) {
        requestViewModel.saveRequest()
        sendWhatsapp(requestViewModel.formulatedRequest.value, listOf(requestViewModel.requestee.value), requestViewModel.activeAccount.value, requestViewModel.getApplication())
    }

    fun copyShareLink(view: View, requestViewModel: NewRequestViewModel) {
        requestViewModel.saveRequest()
        copyShareLink(requestViewModel.formulatedRequest.value, view.findViewById(R.id.copylink_share_selection), requestViewModel.getApplication())
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
            data = Uri.parse("smsto:" + r.generateRecipientString(requestees))
            putExtra(Intent.EXTRA_TEXT, r.generateMessage(a))
            putExtra("sms_body", r.generateMessage(a))
        }

        logAnalyticsEvent(a.getString(R.string.clicked_send_sms_request), a)
        a.startActivityForResult(Intent.createChooser(sendIntent, "Request"), SMS)
    }

    fun sendWhatsapp(r: Request?, requestees: List<StaxContact?>?, account: Account?, a: Activity) {
        if (r == null || requestees == null) {
            showError(a)
            return
        }
        logAnalyticsEvent(a.getString(R.string.clicked_send_whatsapp_request), a)
        if (requestees.size == 1) sendWhatsAppToSingleContact(r, requestees, account, a) else sendWhatsAppToMultipleContacts(r.generateMessage(a), a)
    }

    fun sendWhatsAppToSingleContact(r: Request, requestees: List<StaxContact?>?, account: Account?, a: Activity) {
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
    fun copyShareLink(r: Request?, copyBtn: TextView, a: Activity) {
        when {
            r == null -> showError(a)
            copyToClipboard(r.generateMessage(a), a) -> {
                logAnalyticsEvent(a.getString(R.string.clicked_copylink_request), a)
                copyBtn.isActivated = true
                copyBtn.setCompoundDrawablesWithIntrinsicBounds(null, a.resources.getDrawable(R.drawable.img_check), null, null)
                copyBtn.text = a.getString(R.string.link_copied_label)
            }
            else -> {
                copyBtn.isActivated = false
                copyBtn.setCompoundDrawablesWithIntrinsicBounds(null, a.resources.getDrawable(R.drawable.img_copy), null, null)
                copyBtn.text = a.getString(R.string.copyLink_label)
            }
        }
    }

    fun showError(a: Activity) {
        flashMessage(a, a.getString(R.string.loading_link_dialoghead))
    }
}