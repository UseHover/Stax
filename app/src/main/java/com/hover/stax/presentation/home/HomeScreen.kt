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
package com.hover.stax.presentation.home

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.hover.stax.domain.model.FinancialTip
import com.hover.stax.presentation.home.components.*
import com.hover.stax.ui.theme.StaxTheme
import org.koin.androidx.compose.getViewModel

data class HomeClickFunctions(
    val onSendMoneyClicked: () -> Unit,
    val onBuyAirtimeClicked: () -> Unit,
    val onBuyGoodsClicked: () -> Unit,
    val onPayBillClicked: () -> Unit,
    val onRequestMoneyClicked: () -> Unit,
    val onClickedTC: () -> Unit,
    val onClickedSettingsIcon: () -> Unit,
    val onClickedRewards: () -> Unit
)

interface FinancialTipClickInterface {
    fun onTipClicked(tipId: String?)
}

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun HomeScreen(
    homeClickFunctions: HomeClickFunctions?,
    homeViewModel: HomeViewModel = getViewModel(),
    balancesViewModel: BalancesViewModel = getViewModel(),
    navTo: (dest: Int) -> Unit,
    navController: NavHostController = rememberNavController()
) {
    val accounts by balancesViewModel.accounts.collectAsState()

    StaxTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Scaffold(topBar = { HomeTopBar(0, navTo) }) {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    if (accounts.isEmpty()) {
                        EmptyBalance {
                            homeViewModel.requestAddAccount()
                        }
                    } else { BonusAd(homeViewModel, navController) }

                    MoveMoneyOptions(homeClickFunctions, accounts)

                    BalancesList(accounts = accounts,
                        { homeViewModel.requestAddAccount() },
                        { homeViewModel.requestGoToAccount(it) },
                        { balancesViewModel.requestBalance(it) } )

                    Spacer(Modifier.height(34.dp))

                    FinancialTipScreen(homeViewModel, navTo)
                }
            }
        }
    }
}

@Preview
@Composable
fun HomeScreenPreview() {
    HomeScreen(null, navTo = {})
}