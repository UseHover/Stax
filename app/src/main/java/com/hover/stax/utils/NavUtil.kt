package com.hover.stax.utils

import androidx.fragment.app.FragmentManager
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import com.hover.stax.transactions.TransactionDetailsFragment

object NavUtil {

    fun navigate(navController: NavController, navDirections: NavDirections) = with(navController) {
        currentDestination?.getAction(navDirections.actionId)?.let { navigate(navDirections) }
    }

    fun showTransactionDetailsFragment(uuid: String?, manager: FragmentManager?, isFullScreen: Boolean?) {
        val frag = TransactionDetailsFragment.newInstance(uuid!!, isFullScreen!!)
        frag.show(manager!!, "dialogFrag")
    }
}