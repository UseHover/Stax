package com.hover.stax.home

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.hover.sdk.actions.HoverAction
import com.hover.stax.MainNavigationDirections
import com.hover.stax.R

internal object StaxDeepLinking {
    fun navigateIfRequired(activity: AppCompatActivity) {
        with(activity) {
            val navHelper = NavHelper(this, true)

            if (intent.action != null && intent.action == Intent.ACTION_VIEW && intent.data != null) {
                val route = intent.data.toString()

                when {
                    route.contains(getString(R.string.deeplink_sendmoney)) ->
                        navHelper.checkPermissionsAndNavigate(MainNavigationDirections.actionGlobalTransferFragment(HoverAction.P2P))
                    route.contains(getString(R.string.deeplink_airtime)) ->
                        navHelper.checkPermissionsAndNavigate(MainNavigationDirections.actionGlobalTransferFragment(HoverAction.AIRTIME))
                    route.contains(getString(R.string.deeplink_linkaccount)) ->
                        navHelper.checkPermissionsAndNavigate(MainNavigationDirections.actionGlobalAddChannelsFragment(true))
                    route.contains(getString(R.string.deeplink_balance)) || route.contains(getString(R.string.deeplink_history)) ->
                        navHelper.checkPermissionsAndNavigate(MainNavigationDirections.actionGlobalNavigationBalance())
                    route.contains(getString(R.string.deeplink_settings)) ->
                        navHelper.checkPermissionsAndNavigate(MainNavigationDirections.actionGlobalNavigationSettings())
                    route.contains(getString(R.string.deeplink_reviews)) -> {
                        StaxAppReview.launchStaxReview(this)
                    }
                    route.contains(getString(R.string.deeplink_financial_tips)) ->
                        intent.data?.getQueryParameter("id")?.let { MainNavigationDirections.actionGlobalWellnessFragment(it) }
                }
            }
        }
    }
}