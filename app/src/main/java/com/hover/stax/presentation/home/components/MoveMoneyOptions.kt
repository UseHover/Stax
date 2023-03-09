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
package com.hover.stax.presentation.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hover.stax.R
import com.hover.stax.domain.model.USSD_TYPE
import com.hover.stax.domain.use_case.ActionableAccount
import com.hover.stax.presentation.home.HomeClickFunctions

@Composable
fun MoveMoneyOptions(
    homeClickFunctions: HomeClickFunctions?,
    accounts: List<ActionableAccount>
) {
    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier
            .padding(horizontal = 13.dp, vertical = 26.dp)
            .fillMaxWidth()
    ) {
        homeClickFunctions?.onSendMoneyClicked?.let {
            HomeScreenActionButton(
                onItemClick = it,
                drawable = R.drawable.ic_send_money,
                stringRes = R.string.cta_transfer
            )
        }
        homeClickFunctions?.onBuyAirtimeClicked?.let {
            HomeScreenActionButton(
                onItemClick = it,
                drawable = R.drawable.ic_system_upate_24,
                stringRes = R.string.cta_airtime
            )
        }
        if (showKeFeatures(accounts)) {
            homeClickFunctions?.onBuyGoodsClicked?.let {
                HomeScreenActionButton(
                    onItemClick = it,
                    drawable = R.drawable.ic_shopping_cart,
                    stringRes = R.string.cta_merchant
                )
            }
            homeClickFunctions?.onPayBillClicked?.let {
                HomeScreenActionButton(
                    onItemClick = it,
                    drawable = R.drawable.ic_utility,
                    stringRes = R.string.cta_paybill_linebreak
                )
            }
        }
        homeClickFunctions?.onRequestMoneyClicked?.let {
            HomeScreenActionButton(
                onItemClick = it,
                drawable = R.drawable.ic_baseline_people_24,
                stringRes = R.string.cta_request
            )
        }
    }
}

private fun showKeFeatures(accounts: List<ActionableAccount>): Boolean =
    accounts.any {
        it.account.type == USSD_TYPE && it.ussdAccount?.countryAlpha2.contentEquals("KE", ignoreCase = true)
    }