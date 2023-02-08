package com.hover.stax.hover

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.hover.stax.R

import com.hover.stax.notifications.PushNotificationTopicsInterface
import com.hover.stax.utils.AnalyticsUtil
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber

class TransactionContract : ActivityResultContract<HoverSession.Builder, Intent?>(), PushNotificationTopicsInterface {

	private var builder: HoverSession.Builder? = null

	override fun createIntent(context: Context, b: HoverSession.Builder): Intent {
		builder = b
		updatePushNotifGroupStatus(context)
		logStart(context, b)
		return b.build()
	}

	override fun parseResult(resultCode: Int, result: Intent?) : Intent? {
		return result
	}

	private fun updatePushNotifGroupStatus(c: Context) {
		joinTransactionGroup(c)
		leaveNoUsageGroup(c)
	}

	private fun logStart(context: Context, hsb: HoverSession.Builder) {
		val msg = if (hsb.stopVar != null) {
			context.getString(R.string.checking_var, hsb.action.transaction_type, hsb.stopVar)
		} else { context.getString(R.string.starting_transaction, hsb.action.transaction_type) }
		val data = JSONObject()
		try {
			data.put("actionId", hsb.action.id)
		} catch (ignored: JSONException) {
		}
		AnalyticsUtil.logAnalyticsEvent(msg, data, context)
		AnalyticsUtil.logAnalyticsEvent(hsb.activity.getString(R.string.start_load_screen), context)
		Timber.e(msg)
	}
}