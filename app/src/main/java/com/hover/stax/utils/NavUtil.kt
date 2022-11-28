package com.hover.stax.utils

import androidx.navigation.NavController
import androidx.navigation.NavDirections
import com.hover.stax.MainNavigationDirections

object NavUtil {

    fun navigate(navController: NavController, navDirections: NavDirections) = with(navController) {
        currentDestination?.getAction(navDirections.actionId)?.let { navigate(navDirections) }
    }

    fun showTransactionDetailsFragment(navController: NavController, uuid: String, isNewTransaction: Boolean? = false) {
        navigate(navController, MainNavigationDirections.actionGlobalTxnDetailsFragment(uuid, isNewTransaction!!))
    }

    fun navigateTransfer(navController: NavController, type: String, accountId: String? = null, amount: String? = null, contactId: String? = null) {
        val transferDirection = MainNavigationDirections.actionGlobalTransferFragment(type)
        accountId?.let { transferDirection.accountId = it }
        amount?.let { transferDirection.amount = it }
        contactId?.let { transferDirection.contactId = it }
        navigate(navController, transferDirection)
    }
}