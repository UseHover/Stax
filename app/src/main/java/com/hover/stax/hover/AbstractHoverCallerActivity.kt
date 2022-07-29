package com.hover.stax.hover

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.hover.sdk.actions.HoverAction
import com.hover.sdk.api.HoverParameters
import com.hover.sdk.transactions.TransactionContract
import com.hover.stax.R
import com.hover.stax.domain.model.Account
import com.hover.stax.presentation.home.BalancesViewModel
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
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

const val SCHEDULED = "SCHEDULED"

const val TRANSFER_REQUEST = 203
const val SCHEDULE_REQUEST = 204
const val REQUEST_REQUEST = 301
const val FETCH_ACCOUNT_REQUEST = 205
const val BOUNTY_REQUEST = 207
const val FEE_REQUEST = 208

abstract class AbstractHoverCallerActivity : AppCompatActivity(), PushNotificationTopicsInterface {

    private val balancesViewModel: BalancesViewModel by viewModel()

    lateinit var navHelper: NavHelper

    private fun runAction(hsb: HoverSession.Builder) = try {
        hsb.run()
        updatePushNotifGroupStatus()
    } catch (e: Exception) {
        runOnUiThread { UIHelper.flashAndReportMessage(this, getString(R.string.error_running_action)) }
        createLog(hsb, "Failed Actions")
    }

    fun runSession(account: Account, action: HoverAction) {
        runSession(account, action, null, account.id) // Constants.REQUEST_REQUEST
    }

    fun runSession(account: Account, action: HoverAction, extras: HashMap<String, String>?, requestCode: Int) {
        val hsb = HoverSession.Builder(action, account, this@AbstractHoverCallerActivity, requestCode)
        if (!extras.isNullOrEmpty()) hsb.extras(extras)
        runAction(hsb)
        createLog(hsb, getString(R.string.finish_transfer, action.transaction_type))
    }

    private fun createLog(hsb: HoverSession.Builder, event: String) {
        val data = JSONObject()
        try {
            data.put("actionId", hsb.action.id)
        } catch (ignored: JSONException) {
        }
        AnalyticsUtil.logAnalyticsEvent(event, data, this)
        Timber.e(event)
    }

    private fun getRequestCode(transactionType: String): Int {
        return if (transactionType == HoverAction.FETCH_ACCOUNTS) FETCH_ACCOUNT_REQUEST
        else TRANSFER_REQUEST
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Timber.e("code: %s", requestCode)
        Timber.e("received result. %s", data?.action)
        Timber.e("uuid? %s", data?.extras?.getString("uuid"))

        when (requestCode) {
            REQUEST_REQUEST -> showMessage(getString(R.string.toast_confirm_request))
            BOUNTY_REQUEST -> showBountyDetails(data)
            FEE_REQUEST -> showFeeDetails(data)
            else -> {
                navToTransactionDetail(data)
            }
        }
    }

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

    private fun showFeeDetails(data: Intent?) {
        Timber.e("Request code is fee")
        if (data != null) {
            val dialog = StaxDialog(this)
                .setDialogTitle(getString(R.string.fee_fetched_header))
                .setDialogMessage(getFee(data))
                .setPosButton(R.string.got_it) { }
            dialog.showIt()
        }
    }

    private fun getFee(data: Intent): String {
        if (data.hasExtra(TransactionContract.COLUMN_PARSED_VARIABLES)) {
            val parsedVariables = data.getSerializableExtra(TransactionContract.COLUMN_PARSED_VARIABLES) as HashMap<String, String>

            if (parsedVariables.containsKey("fee") && parsedVariables["fee"] != null) {
                return parsedVariables["fee"]!!
            }
        }
        return "No fee information found"
    }

    private fun navToTransactionDetail(data: Intent?) {
        if (data != null && data.extras != null && data.extras!!.getString("uuid") != null) {
            NavUtil.showTransactionDetailsFragment(
                findNavController(R.id.nav_host_fragment),
                data.extras!!.getString("uuid")!!
            )
        }
    }

    private fun updatePushNotifGroupStatus() {
        joinTransactionGroup(this)
        leaveNoUsageGroup(this)
    }

    private fun updatePushNotifGroupStatus(a: HoverAction) {
        joinAllBountiesGroup(this)
        joinBountyCountryGroup(a.country_alpha2.uppercase(), this)
    }
}