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
import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.hover.stax.R
import com.hover.stax.addChannels.AddAccountActivity
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
    val onClickedAddNewAccount: () -> Unit,
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
    val accounts by balancesViewModel.accounts.observeAsState(initial = emptyList())

    val context = LocalContext.current

    StaxTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Scaffold(topBar = { TopBar(0, navTo) }) {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    if (accounts.isEmpty()) {
                        EmptyBalance {
                            context.startActivity(Intent(context, AddAccountActivity::class.java))
                        }
                    } else { BonusAd(homeViewModel, navController) }

                    MoveMoneyOptions(homeClickFunctions, accounts)

                    BalancesList(accounts = accounts) {
                        context.startActivity(Intent(context, AddAccountActivity::class.java))
                    }

                    FinancialTipScreen(homeViewModel, navController)
                }
            }
        }
    }
}

@Preview
@Composable
fun HomeScreenPreview() {
    val financialTip = FinancialTip(
        id = "1234",
        title = "Do you want to save money",
        content = "This is a test content here so lets see if its going to use ellipse overflow",
        snippet = "This is a test content here so lets see if its going to use ellipse overflow, with an example here",
        date = System.currentTimeMillis(),
        shareCopy = null,
        deepLink = null
    )

    HomeScreen(null, navTo = {})
}