package com.hover.stax.home

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavController
import com.hover.sdk.actions.HoverAction
import com.hover.sdk.api.Hover
import com.hover.stax.R
import com.hover.stax.channels.AddChannelsFragment
import com.hover.stax.financialTips.FinancialTipsFragment
import com.hover.stax.transactions.TransactionDetailsFragment
import com.hover.stax.utils.Constants
import com.hover.stax.utils.UIHelper
import timber.log.Timber

interface NavigationInterface {
    fun navigate(navController: NavController, toWhere: Int, activity: Activity) {
        when (toWhere) {
            Constants.NAV_TRANSFER -> navigateToTransferFragment(navController, HoverAction.P2P)
            Constants.NAV_AIRTIME -> navigateToTransferFragment(navController, HoverAction.AIRTIME)
            Constants.NAV_REQUEST -> navigateToRequestFragment(navController)
            Constants.NAV_HOME -> navigateToHomeFragment(navController)
            Constants.NAV_BALANCE -> navigateToBalanceFragment(navController)
            Constants.NAV_SETTINGS -> navigateToSettingsFragment(navController)
            Constants.NAV_LINK_ACCOUNT -> navigateToChannelsListFragment(navController, true)
            Constants.NAV_EMAIL_CLIENT -> openSupportEmailClient(activity)
            Constants.NAV_USSD_LIB -> navigateToUSSDLib(navController)
            Constants.NAV_PAYBILL -> navController.navigate(R.id.action_navigation_home_to_paybillFragment)
            else -> {}
        }
    }

    fun navigateToUSSDLib(navController: NavController) {
        navController.navigate(R.id.libraryFragment)
    }

    fun navigateToRequestFragment(navController: NavController) {
        navController.navigate(R.id.navigation_request)
    }

    fun navigateToHomeFragment(navController: NavController) {
        navController.navigate(R.id.navigation_home)
    }

    fun navigateToSettingsFragment(navController: NavController) {
        navController.navigate(R.id.navigation_settings)
    }

    fun navigateToBalanceFragment(navController: NavController) {
        navController.navigate(R.id.navigation_balance)
    }

    fun navigateToChannelsListFragment(navController: NavController, forceReturnData: Boolean) {
        val bundle = Bundle()
        bundle.putBoolean(AddChannelsFragment.FORCE_RETURN_DATA, forceReturnData)
        navController.navigate(R.id.navigation_linkAccount, bundle)
    }

    fun navigateToTransferFragment(navController: NavController, actionType: String?) {
        val bundle = Bundle()
        bundle.putString(Constants.TRANSACTION_TYPE, actionType)
        navController.navigate(R.id.action_navigation_home_to_navigation_transfer, bundle)
    }

    fun navigateToTransactionDetailsFragment(uuid: String?, manager: FragmentManager?, isFullScreen: Boolean?) {
        val frag = TransactionDetailsFragment.newInstance(uuid!!, isFullScreen!!)
        frag.show(manager!!, "dialogFrag")
    }

    fun navigateToWellnessFragment(navController: NavController, id: String?) {
        val bundle = Bundle()
        bundle.putString(FinancialTipsFragment.TIP_ID, id)
        navController.navigate(R.id.action_navigation_home_to_wellnessFragment, bundle)
    }

    fun openSupportEmailClient(activity: Activity) {
        val intent = Intent(Intent.ACTION_VIEW)
        val recipientEmail = activity.getString(R.string.stax_support_email)
        val subject = activity.getString(R.string.stax_emailing_subject, Hover.getDeviceId(activity.baseContext))
        val data = Uri.parse("mailto:$recipientEmail ?subject=$subject")
        intent.data = data
        try {
            activity.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Timber.e("Activity not found")
            UIHelper.flashMessage(activity, activity.getString(R.string.email_client_not_found))
        }
    }
}