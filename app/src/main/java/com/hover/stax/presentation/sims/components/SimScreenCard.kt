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
package com.hover.stax.presentation.sims.components

import android.content.Context
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hover.sdk.actions.HoverAction
import com.hover.stax.R
import com.hover.stax.domain.model.USSDAccount
import com.hover.stax.domain.use_case.SimWithAccount
import com.hover.stax.presentation.components.DisabledButton
import com.hover.stax.presentation.components.PrimaryButton
import com.hover.stax.presentation.components.SecondaryButton
import com.hover.stax.presentation.components.StaxCard
import com.hover.stax.presentation.components.SimTitle
import com.hover.stax.ui.theme.TextGrey
import com.hover.stax.utils.DateUtils
import com.hover.stax.utils.Utils

@Composable
internal fun SimScreenCard(
    simWithAccount: SimWithAccount,
    refreshBalance: (SimWithAccount) -> Unit,
    buyAirtime: (USSDAccount) -> Unit
) {
    StaxCard {
        val context = LocalContext.current

        SimItemTopRow(simWithAccount, refreshBalance)
        if (simWithAccount.account.channelId != -1) {
            val notYetChecked = stringResource(id = R.string.not_yet_checked)
            Text(
                text = simWithAccount.account.latestBalance ?: notYetChecked,
                color = TextGrey,
                style = MaterialTheme.typography.body1
            )

            if (simWithAccount.account.latestBalance != null) {
                Spacer(modifier = Modifier.height(13.dp))
                Text(
                    text = stringResource(
                        id = R.string.as_of,
                        DateUtils.humanFriendlyDateTime(simWithAccount.account.latestBalanceTimestamp)
                    ),
                    color = TextGrey,
                    style = MaterialTheme.typography.body1
                )
            }
        } else {
            Text(
                text = stringResource(
                    id = R.string.unsupported_sim_info
                ),
                color = TextGrey,
                style = MaterialTheme.typography.body2
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        if (simWithAccount.account.channelId == -1) {
            SecondaryButton(
                stringResource(id = R.string.email_support), null,
                onClick = { emailStax(simWithAccount, context) }
            )
        } else {
            val bonus = getBonus(simWithAccount.airtimeActions)
            SecondaryButton(
                getAirtimeButtonLabel(bonus), icon = getAirtimeButtonIcon(bonus),
                onClick = { buyAirtime(simWithAccount.account) }
            )
        }
    }
}

private fun getBonus(actions: List<HoverAction?>): Int {
    var bonus = 0
    actions.let { action ->
        bonus = action.firstOrNull { (it?.bonus_percent ?: 0) > 0 }?.bonus_percent ?: 0
    }
    return bonus
}

@Composable
private fun getAirtimeButtonLabel(bonus: Int): String {
    var label = stringResource(id = R.string.nav_airtime)
    if (bonus > 0) {
        label = stringResource(R.string.buy_airitme_bonus, Utils.formatPercent(bonus))
    }
    return label
}

private fun getAirtimeButtonIcon(bonus: Int?): Int? {
    var icon: Int? = null
    if (bonus != null) { icon = R.drawable.ic_bonus }
    return icon
}

private fun emailStax(simWithAccount: SimWithAccount, context: Context) {
    val emailBody = context.getString(
        R.string.sim_card_support_request_emailBody,
        simWithAccount.sim.osReportedHni ?: "Null",
        simWithAccount.sim.operatorName ?: simWithAccount.account.userAlias,
        simWithAccount.sim.networkOperator ?: "Null",
        simWithAccount.sim.countryIso ?: "Null"
    )

    Utils.openEmail(context.getString(R.string.sim_card_support_request_emailSubject), context, emailBody)
}

@Composable
fun SimItemTopRow(
    simWithAccount: SimWithAccount,
    refreshBalance: (SimWithAccount) -> Unit
) {
    SimTitle(simWithAccount.sim, simWithAccount.account.institutionName, simWithAccount.account.logoUrl, content = {
        SimAction(simWithAccount, refreshBalance)
    })
}

@Composable
fun SimAction(simWithAccount: SimWithAccount, refreshBalance: (SimWithAccount) -> Unit) {
    if (simWithAccount.balanceAction != null) {
        PrimaryButton(
            stringResource(id = R.string.check_balance_capitalized), null,
            onClick = { refreshBalance(simWithAccount) }
        )
    } else {
        DisabledButton(stringResource(id = R.string.unsupported), null) { }
    }
}

// @Composable
// @Preview
// private fun SimItemsPreview() {
// 	StaxTheme {
// 		Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
// 			Scaffold(topBar = {
// 				TopBar(title = R.string.nav_home, isInternetConnected = false, {}, {})
// 			}, content = { innerPadding ->
// 				Column(modifier = Modifier.padding(innerPadding)) {
// 					SimItem(
// 						simIndex = 2,
// 						secondaryClickItem = { },
// 						balanceTapListener = null
// 					)
//
// 					SimItem(
// 						simIndex = 1,
// 						account = Account.generateDummy("MTN Nigeria"),
// 						bonus = 1,
// 						secondaryClickItem = { },
// 						balanceTapListener = null
// 					)
//
// 					SimItem(
// 						simIndex = -1,
// 						account = Account.generateDummy("Airtel"),
// 						bonus = 1,
// 						secondaryClickItem = { },
// 						balanceTapListener = null
// 					)
// 				}
// 			})
// 		}
// 	}
// }