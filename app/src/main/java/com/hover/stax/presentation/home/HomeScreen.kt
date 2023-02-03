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
import android.content.Context
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.hover.sdk.actions.HoverAction
import com.hover.stax.R
import com.hover.stax.addChannels.ChannelsViewModel
import com.hover.stax.domain.model.FinancialTip
import com.hover.stax.presentation.home.components.BalanceHeader
import com.hover.stax.presentation.home.components.BalanceItem
import com.hover.stax.presentation.home.components.BonusCard
import com.hover.stax.presentation.home.components.EmptyBalance
import com.hover.stax.presentation.home.components.FinancialTipCard
import com.hover.stax.presentation.home.components.GuideCard
import com.hover.stax.presentation.home.components.PrimaryFeatures
import com.hover.stax.presentation.home.components.TopBar
import com.hover.stax.ui.theme.StaxTheme
import com.hover.stax.utils.AnalyticsUtil
import timber.log.Timber

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
    channelsViewModel: ChannelsViewModel,
    homeClickFunctions: HomeClickFunctions,
    balanceTapListener: BalanceTapListener,
    tipInterface: FinancialTipClickInterface,
    homeViewModel: HomeViewModel,
    navTo: (dest: Int) -> Unit,
) {
    val homeState by homeViewModel.homeState.observeAsState()
    val simCountryList by channelsViewModel.simCountryList.observeAsState(initial = emptyList())
    val context = LocalContext.current

    StaxTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
            Scaffold(
                topBar = { TopBar(title = R.string.nav_home, navTo) },
                content = {
                    LazyColumn {

                        homeState?.bonuses?.let { bonus ->
                                item {
                                    BonusCard(
                                        message = bonus.first().bonus_message,
                                        onClickedTC = homeClickFunctions.onClickedTC,
                                        onClickedTopUp = {
                                            clickedOnBonus(
                                                context,
                                                channelsViewModel,
                                                bonus.first()
                                            )
                                        }
                                    )
                                }
                        }

                        if (homeState?.accounts?.isEmpty() == true)
                            item {
                                EmptyBalance(onClickedAddAccount = homeClickFunctions.onClickedAddNewAccount)
                            }

                        item {
                            PrimaryFeatures(
                                onSendMoneyClicked = homeClickFunctions.onSendMoneyClicked,
                                onBuyAirtimeClicked = homeClickFunctions.onBuyAirtimeClicked,
                                onBuyGoodsClicked = homeClickFunctions.onBuyGoodsClicked,
                                onPayBillClicked = homeClickFunctions.onPayBillClicked,
                                onRequestMoneyClicked = homeClickFunctions.onRequestMoneyClicked,
                                showKEFeatures(simCountryList)
                            )
                        }

                        homeState?.accounts?.let { accounts ->
                            item {
                                BalanceHeader(
                                    onClickedAddAccount = homeClickFunctions.onClickedAddNewAccount, accounts.isNotEmpty()
                                )
                            }
                        }

                        homeState?.accounts?.let { accounts ->
                            items(accounts) { account ->
                                BalanceItem(
                                    staxAccount = account,
                                    context = context,
                                    balanceTapListener = balanceTapListener
                                )
                            }
                        }

                        homeState?.financialTips?.let { financialTips ->
                            item {
                                financialTips.firstOrNull {
                                    android.text.format.DateUtils.isToday(it.date!!)
                                }?.let {
                                    if (homeState?.dismissedTipId != it.id)
                                        FinancialTipCard(
                                            tipInterface = tipInterface,
                                            financialTip = financialTips.first(),
                                            homeViewModel
                                        )
                                }
                            }
                        }
                    }
                }
            )
        }
    }
}

private fun clickedOnBonus(
    context: Context,
    channelsViewModel: ChannelsViewModel,
    bonus: HoverAction
) {
    AnalyticsUtil.logAnalyticsEvent(
        context.getString(R.string.clicked_bonus_airtime_banner),
        context
    )
    channelsViewModel.payWith(bonus.channel_id)
}

private fun showKEFeatures(countryIsos: List<String>): Boolean = countryIsos.any { it.contentEquals("KE", ignoreCase = true) }

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

    StaxTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
            Scaffold(
                topBar = {
                    TopBar(title = R.string.nav_home, {})
                },
                content = { padding ->
                    LazyColumn(modifier = Modifier.padding(padding), content = {
                        item {
                            GuideCard(
                                message = stringResource(id = R.string.beginners_guide_airtime),
                                buttonString = stringResource(id = R.string.check_airtime_balance)
                            ) {}
                        }

                        item {
                            BonusCard(
                                message = "Buy at least Ksh 50 airtime on Stax to get 3% or more bonus airtime",
                                onClickedTC = {},
                                onClickedTopUp = {}
                            )
                        }
                        item {
                            PrimaryFeatures(
                                onSendMoneyClicked = { },
                                onBuyAirtimeClicked = { },
                                onBuyGoodsClicked = { },
                                onPayBillClicked = { },
                                onRequestMoneyClicked = {},
                                true
                            )
                        }
                        item {
                            BalanceScreenPreview()
                        }
                        item {
                            FinancialTipCard(tipInterface = null, financialTip = financialTip, null)
                        }
                    })
                }
            )
        }
    }
}