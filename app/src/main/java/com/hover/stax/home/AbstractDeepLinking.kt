package com.hover.stax.home

import android.content.Intent
import android.os.Bundle
import com.hover.sdk.actions.HoverAction
import com.hover.stax.R

abstract class AbstractDeepLinking : AbstractAppReviewActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkForDeepLinking()
    }

    private fun checkForDeepLinking() {
        if (intent.action != null && intent.action == Intent.ACTION_VIEW && intent.data != null) {
            val route = intent.data.toString()

            when {
                route.contains(getString(R.string.deeplink_sendmoney)) ->
                    navigateToTransferFragment(getNavController(), HoverAction.P2P)
                route.contains(getString(R.string.deeplink_airtime)) ->
                    navigateToTransferFragment(getNavController(), HoverAction.AIRTIME)
                route.contains(getString(R.string.deeplink_linkaccount)) ->
                    navigateToChannelsListFragment(getNavController(), true)
                route.contains(getString(R.string.deeplink_balance)) || route.contains(getString(R.string.deeplink_history)) ->
                    navigateToBalanceFragment(getNavController())
                route.contains(getString(R.string.deeplink_settings)) ->
                    navigateToSettingsFragment(getNavController())
                route.contains(getString(R.string.deeplink_reviews)) ->
                    launchStaxReview()
                route.contains(getString(R.string.deeplink_financial_tips)) ->
                    intent.data?.getQueryParameter("id")?.let { navigateToWellnessFragment(getNavController(), it) }
            }

            intent.data = null
        }
    }
}