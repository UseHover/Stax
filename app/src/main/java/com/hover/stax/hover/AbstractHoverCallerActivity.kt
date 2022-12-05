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
package com.hover.stax.hover

import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.hover.sdk.actions.HoverAction
import com.hover.sdk.api.HoverParameters
import com.hover.sdk.transactions.TransactionContract
import com.hover.stax.R
import com.hover.stax.domain.model.Account
import com.hover.stax.home.NavHelper
import com.hover.stax.notifications.PushNotificationTopicsInterface
import com.hover.stax.schedules.Schedule
import com.hover.stax.utils.AnalyticsUtil
import com.hover.stax.utils.DateUtils
import com.hover.stax.utils.NavUtil
import com.hover.stax.utils.UIHelper
import com.hover.stax.views.StaxDialog
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber

const val SCHEDULE_REQUEST = 204
const val REQUEST_REQUEST = 301
const val BOUNTY_REQUEST = 207
const val FEE_REQUEST = 208

abstract class AbstractHoverCallerActivity : AppCompatActivity(), PushNotificationTopicsInterface {

    lateinit var navHelper: NavHelper

    fun runSession(account: Account, action: HoverAction) {
        runSession(account, action, null, account.id)
    }

    fun runSession(
        account: Account,
        action: HoverAction,
        extras: HashMap<String, String>?,
        requestCode: Int
    ) {
        Timber.e("Building sesh")
        val hsb = HoverSession.Builder(action, account, extras, this@AbstractHoverCallerActivity, requestCode)
        if (requestCode == FEE_REQUEST) hsb.stopAt("fee")
        try {
            hsb.build().runForResult(this)
        } catch (e: Exception) {
            runOnUiThread { UIHelper.flashAndReportMessage(this, getString(R.string.error_running_action)) }
            AnalyticsUtil.logErrorAndReportToFirebase(hsb.action.public_id, getString(R.string.error_running_action_log), e)
        }
    }

    private fun callHover(launcher: ActivityResultLauncher<HoverSession.Builder>, b: HoverSession.Builder, activity: Activity) {
        try {
            launcher.launch(b)
        } catch (e: Exception) {
            activity.runOnUiThread { UIHelper.flashAndReportMessage(activity, getString(R.string.error_running_action)) }
            AnalyticsUtil.logErrorAndReportToFirebase(b.action.public_id, getString(R.string.error_running_action_log), e)
        }
    }

    fun makeRegularCall(a: HoverAction, analytics: Int) {
        AnalyticsUtil.logAnalyticsEvent(getString(analytics), this)
        updatePushNotifGroupStatus(a)
        call(a.public_id)
    }

    private fun call(actionId: String) {
        val i = HoverParameters.Builder(this).request(actionId).setEnvironment(HoverParameters.MANUAL_ENV).buildIntent()
        startActivityForResult(i, BOUNTY_REQUEST)
    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        Timber.e("code: %s", requestCode)
//        Timber.e("received result. %s", data?.action)
//        Timber.e("uuid? %s", data?.extras?.getString("uuid"))
//        Timber.e("extras? %s", data?.extras)
//
//        when (requestCode) {
//            REQUEST_REQUEST -> showMessage(getString(R.string.toast_confirm_request))
//            BOUNTY_REQUEST -> showBountyDetails(data)
//            FEE_REQUEST -> Timber.e("Fee request")
//            else -> {
//                navToTransactionDetail(data)
//            }
//        }
//    }

    private fun generateScheduleMsg(resultCode: Int, data: Intent?): String {
        return if (resultCode == RESULT_OK && data != null)
            getString(R.string.toast_confirm_schedule, DateUtils.humanFriendlyDate(data.getLongExtra(Schedule.DATE_KEY, 0)))
        else ""
    }

    private fun showMessage(str: String) = UIHelper.showAndReportSnackBar(this, findViewById(R.id.fab), str)

    private fun showBountyDetails(data: Intent?) {
        Timber.i("Request code is bounty")
        navToTransactionDetail(data)
    }



    private fun navToTransactionDetail(data: Intent?) {
        if (data != null && data.extras != null && data.extras!!.getString("uuid") != null) {
            NavUtil.showTransactionDetailsFragment(
                findNavController(R.id.nav_host_fragment),
                data.extras!!.getString("uuid")!!
            )
        }
    }

    private fun updatePushNotifGroupStatus(a: HoverAction) {
        joinAllBountiesGroup(this)
        joinBountyCountryGroup(a.country_alpha2, this)
    }
}