package com.hover.stax.utils

import androidx.navigation.NavController
import androidx.navigation.NavDirections
import com.hover.stax.MainNavigationDirections

object NavUtil {

    fun navigate(navController: NavController, navDirections: NavDirections) = with(navController) {
        currentDestination?.getAction(navDirections.actionId)?.let { navigate(navDirections) }
    }

    fun showTransactionDetailsFragment(navController: NavController, uuid: String) {
        navigate(navController, MainNavigationDirections.actionGlobalTxnDetailsFragment(uuid))
    }
}