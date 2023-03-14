/*
 * Copyright 2023 Stax
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

    override fun createIntent(context: Context, input: HoverSession.Builder): Intent {
        builder = input
        updatePushNotifGroupStatus(context)
        logStart(context, input)
        return input.build()
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Intent? {
        return intent
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