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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.hover.stax.R
import com.hover.stax.addChannels.ChannelsViewModel
import com.hover.stax.domain.model.Bonus
import com.hover.stax.domain.model.FinancialTip
import com.hover.stax.presentation.home.components.*
import com.hover.stax.ui.theme.StaxTheme
import com.hover.stax.utils.AnalyticsUtil
import com.hover.stax.utils.network.NetworkMonitor

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
    homeViewModel: HomeViewModel
) {
    val homeState by homeViewModel.homeState.collectAsState()
    val hasNetwork by NetworkMonitor.StateLiveData.get().observeAsState(initial = false)
    val simCountryList by channelsViewModel.simCountryList.observeAsState(initial = emptyList())
    val accounts by homeViewModel.accounts.observeAsState(initial = emptyList())
    val context = LocalContext.current

    StaxTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
            Scaffold(
                topBar = {
                    TopBar(
                        title = R.string.nav_home,
                        isInternetConnected = hasNetwork,
                        homeClickFunctions.onClickedSettingsIcon,
                        homeClickFunctions.onClickedRewards
                    )
                },
                content = {
                    LazyColumn {
                        if (homeState.bonuses.isNotEmpty() && accounts.isNotEmpty())
                            item {
                                BonusCard(message = homeState.bonuses.first().message,
                                    onClickedTC = homeClickFunctions.onClickedTC,
                                    onClickedTopUp = {
                                        clickedOnBonus(
                                            context,
                                            channelsViewModel,
                                            homeState.bonuses.first()
                                        )
                                    })
                            }

                        if (accounts.isEmpty())
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

                        if (accounts.isNotEmpty())
                            item {
                                BalanceHeader(
                                    onClickedAddAccount = homeClickFunctions.onClickedAddNewAccount, homeState.accounts.isNotEmpty()
                                )
                            }

                        items(accounts) { account ->
                            BalanceItem(
                                staxAccount = account,
                                context = context,
                                balanceTapListener = balanceTapListener
                            )
                        }

                        item {
                            homeState.financialTips.firstOrNull {
                                android.text.format.DateUtils.isToday(it.date!!)
                            }?.let {
                                if (homeState.dismissedTipId != it.id)
                                    FinancialTipCard(
                                        tipInterface = tipInterface,
                                        financialTip = homeState.financialTips.first(),
                                        homeViewModel
                                    )
                            }
                        }
                    }
                }
            )
        }
    }
}

private fun clickedOnBonus(context: Context, channelsViewModel: ChannelsViewModel, bonus: Bonus) {
    AnalyticsUtil.logAnalyticsEvent(
        context.getString(R.string.clicked_bonus_airtime_banner),
        context
    )
    channelsViewModel.validateAccounts(bonus.userChannel)
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
                    TopBar(title = R.string.nav_home, isInternetConnected = false, {}, {})
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
                            BonusCard(message = "Buy at least Ksh 50 airtime on Stax to get 3% or more bonus airtime",
                                onClickedTC = {},
                                onClickedTopUp = {})
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
                })
        }
    }
}