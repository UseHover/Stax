package com.hover.stax.utils

import androidx.navigation.NavController
import androidx.navigation.NavDirections

object NavUtil {
    fun navigate(navController: NavController, navDirections: NavDirections) = with(navController) {
        currentDestination?.getAction(navDirections.actionId)?.let { navigate(navDirections) }
    }
}