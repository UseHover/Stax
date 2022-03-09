package com.hover.stax.home

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.hover.stax.R
import com.hover.stax.utils.Constants.NAV_AIRTIME
import com.hover.stax.utils.Constants.NAV_BALANCE
import com.hover.stax.utils.Constants.NAV_LINK_ACCOUNT
import com.hover.stax.utils.Constants.NAV_SETTINGS
import com.hover.stax.utils.Constants.NAV_TRANSFER
import timber.log.Timber

internal object StaxDeepLinking {
    fun navigateIfRequired(activity: AppCompatActivity) {
        with(activity) {
            val staxNavigation = StaxNavigation(this, true)

            if (intent.action != null && intent.action == Intent.ACTION_VIEW && intent.data != null) {
                val route = intent.data.toString()

                when {
                    route.contains(getString(R.string.deeplink_sendmoney)) ->
                        staxNavigation.checkPermissionsAndNavigate(NAV_TRANSFER)
                    route.contains(getString(R.string.deeplink_airtime)) ->
                        staxNavigation.checkPermissionsAndNavigate(NAV_AIRTIME)
                    route.contains(getString(R.string.deeplink_linkaccount)) ->
                        staxNavigation.checkPermissionsAndNavigate(NAV_LINK_ACCOUNT)
                    route.contains(getString(R.string.deeplink_balance)) || route.contains(getString(R.string.deeplink_history)) ->
                        staxNavigation.checkPermissionsAndNavigate(NAV_BALANCE)
                    route.contains(getString(R.string.deeplink_settings)) ->
                        staxNavigation.checkPermissionsAndNavigate(NAV_SETTINGS)
                    route.contains(getString(R.string.deeplink_reviews)) -> {
                        StaxAppReview.launchStaxReview(this)
                    } route.contains(getString(R.string.deeplink_financial_tips)) ->
                        intent.data?.getQueryParameter("id")?.let { staxNavigation.navigateWellness(it) }
                }
            }
        }
    }
}