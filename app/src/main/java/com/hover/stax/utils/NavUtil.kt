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
package com.hover.stax.utils

import androidx.navigation.NavController
import androidx.navigation.NavDirections
import com.hover.stax.MainNavigationDirections

object NavUtil {

    fun navigate(navController: NavController, navDirections: NavDirections) = with(navController) {
        currentDestination?.getAction(navDirections.actionId)?.let { navigate(navDirections) }
    }

    fun showTransactionDetailsFragment(
        navController: NavController,
        uuid: String,
        isNewTransaction: Boolean? = false
    ) {
        navigate(navController, MainNavigationDirections.actionGlobalTxnDetailsFragment(uuid, isNewTransaction!!))
    }

    fun navigateTransfer(
        navController: NavController,
        type: String,
        accountId: String? = null,
        amount: String? = null,
        contactId: String? = null
    ) {
        val transferDirection = MainNavigationDirections.actionGlobalTransferFragment(type)
        accountId?.let { transferDirection.accountId = it }
        amount?.let { transferDirection.amount = it }
        contactId?.let { transferDirection.contactId = it }
        navigate(navController, transferDirection)
    }
}