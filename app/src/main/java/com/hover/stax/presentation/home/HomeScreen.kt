package com.hover.stax.presentation.home

import android.content.Context
import android.text.Html
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.text.HtmlCompat
import com.hover.stax.R
import com.hover.stax.addChannels.ChannelsViewModel
import com.hover.stax.domain.model.Bonus
import com.hover.stax.domain.model.FinancialTip
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
    val onClickedSettingsIcon: () -> Unit
)

interface FinancialTipClickInterface {
    fun onTipClicked(tipId: String?)
}

@Composable
fun TopBar(@StringRes title: Int = R.string.app_name, isInternetConnected: Boolean, onClickedSettingsIcon: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(all = dimensionResource(id = R.dimen.margin_13)),
    ) {
        HorizontalImageTextView(
            drawable = R.drawable.stax_logo,
            stringRes = title,
            modifier = Modifier.weight(1f),
            MaterialTheme.typography.button
        )

        if (!isInternetConnected) {
            HorizontalImageTextView(
                drawable = R.drawable.ic_internet_off,
                stringRes = R.string.working_offline,
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(horizontal = 16.dp),
                MaterialTheme.typography.button
            )
        }

        Image(
            painter = painterResource(id = R.drawable.ic_settings),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .clickable(onClick = onClickedSettingsIcon)
                .size(25.dp),
        )
    }
}

@Composable
fun BonusCard(message: String, onClickedTC: () -> Unit, onClickedTopUp: () -> Unit) {
    val size13 = dimensionResource(id = R.dimen.margin_13)
    val size10 = dimensionResource(id = R.dimen.margin_10)

    Card(modifier = Modifier.padding(all = size13), elevation = 2.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = size13)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(id = R.string.get_rewarded),
                    style = MaterialTheme.typography.h3
                )
                Text(
                    text = message,
                    modifier = Modifier.padding(vertical = size10),
                    style = MaterialTheme.typography.body1
                )
                Text(
                    text = stringResource(id = R.string.tc_apply),
                    textDecoration = TextDecoration.Underline,
                    color = colorResource(id = R.color.brightBlue),
                    style = MaterialTheme.typography.body2,
                    modifier = Modifier.clickable(onClick = onClickedTC)
                )
                Text(
                    text = stringResource(id = R.string.top_up),
                    color = colorResource(id = R.color.brightBlue),
                    style = MaterialTheme.typography.h4,
                    modifier = Modifier
                        .padding(top = size13)
                        .clickable(onClick = onClickedTopUp)
                )
            }
            Image(
                painter = painterResource(id = R.drawable.ic_bonus),
                contentDescription = stringResource(id = R.string.get_rewarded),
                modifier = Modifier
                    .size(70.dp)
                    .padding(start = size13)
                    .align(Alignment.CenterVertically)
            )
        }
    }
}

@Composable
fun PrimaryFeatures(
    onSendMoneyClicked: () -> Unit,
    onBuyAirtimeClicked: () -> Unit,
    onBuyGoodsClicked: () -> Unit,
    onPayBillClicked: () -> Unit,
    onRequestMoneyClicked: () -> Unit,
    showKenyaFeatures: Boolean
) {
    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier
            .padding(horizontal = 13.dp, vertical = 26.dp)
            .fillMaxWidth()
    ) {
        VerticalImageTextView(
            onItemClick = onSendMoneyClicked,
            drawable = R.drawable.ic_transfer_within_24,
            stringRes = R.string.cta_transfer
        )
        VerticalImageTextView(
            onItemClick = onBuyAirtimeClicked,
            drawable = R.drawable.ic_system_upate_24,
            stringRes = R.string.cta_airtime
        )
        if (showKenyaFeatures) {
            VerticalImageTextView(
                onItemClick = onBuyGoodsClicked,
                drawable = R.drawable.ic_card,
                stringRes = R.string.cta_merchant
            )
            VerticalImageTextView(
                onItemClick = onPayBillClicked,
                drawable = R.drawable.ic_utility,
                stringRes = R.string.cta_paybill_linebreak
            )
        }
        VerticalImageTextView(
            onItemClick = onRequestMoneyClicked,
            drawable = R.drawable.ic_baseline_people_24,
            stringRes = R.string.cta_request
        )
    }
}

@Composable
private fun FinancialTipCard(
    tipInterface: FinancialTipClickInterface?,
    financialTip: FinancialTip
) {
    val size13 = dimensionResource(id = R.dimen.margin_13)
    Card(elevation = 0.dp, modifier = Modifier.padding(all = size13)) {
        Column {
            Row(modifier = Modifier.fillMaxWidth().padding(all = size13)) {
                HorizontalImageTextView(drawable = R.drawable.ic_tip_of_day,
                    stringRes = R.string.tip_of_the_day,
                    Modifier.weight(1f),
                    MaterialTheme.typography.button)

                Image(painter = painterResource(id = R.drawable.ic_close_white),
                    contentDescription = null,
                    alignment = Alignment.CenterEnd)
            }

            Row(modifier = Modifier.padding(start = size13, end = size13, bottom = size13)
                .clickable { tipInterface?.onTipClicked(null) }) {

                Column(modifier = Modifier.weight(1f)) {
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(text = financialTip.title,
                        style = MaterialTheme.typography.body2,
                        textDecoration = TextDecoration.Underline)

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(text = financialTip.snippet,
                        style = MaterialTheme.typography.body2,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(bottom = size13, top = 3.dp))
                    Text(text = stringResource(id = R.string.read_more),
                        color = colorResource(id = R.color.brightBlue),
                        modifier = Modifier.clickable { tipInterface?.onTipClicked(financialTip.id) })
                }

                Image(
                    painter = painterResource(id = R.drawable.tips_fancy_icon),
                    contentDescription = null,
                    modifier = Modifier.size(60.dp).padding(start = size13)
                        .align(Alignment.CenterVertically),
                )
            }
        }
    }
}

@Composable
private fun VerticalImageTextView(
    @DrawableRes drawable: Int,
    @StringRes stringRes: Int,
    onItemClick: () -> Unit
) {
    val size24 = dimensionResource(id = R.dimen.margin_24)
    val blue = colorResource(id = R.color.stax_state_blue)
    Column(
        modifier = Modifier
            .clickable(onClick = onItemClick)
            .padding(horizontal = 2.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Image(painter = painterResource(id = drawable),
            contentDescription = null,
            Modifier
                .size(size24)
                .align(Alignment.CenterHorizontally)
                .drawBehind {
                    drawCircle(radius = this.size.minDimension, color = blue)
                })
        Text(
            text = stringResource(id = stringRes),
            color = colorResource(id = R.color.offWhite),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.caption,
            modifier = Modifier
                .padding(top = size24)
                .widthIn(min = 50.dp, max = 65.dp)
        )
    }
}

@Composable
internal fun HorizontalImageTextView(
    @DrawableRes drawable: Int,
    @StringRes stringRes: Int,
    modifier: Modifier = Modifier, textStyle: TextStyle
) {
    Row(horizontalArrangement = Arrangement.Start, modifier = modifier) {
        Image(
            painter = painterResource(id = drawable),
            contentDescription = null,
            modifier = Modifier.align(Alignment.CenterVertically),
        )
        Text(
            text = stringResource(id = stringRes),
            style = textStyle,
            modifier = Modifier
                .padding(start = dimensionResource(id = R.dimen.margin_13))
                .align(Alignment.CenterVertically),
            color = colorResource(id = R.color.offWhite)
        )
    }
}

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
                topBar = { TopBar(title = R.string.nav_home, isInternetConnected = hasNetwork, homeClickFunctions.onClickedSettingsIcon) },
                content = {
                    LazyColumn {
                        item {
                            if (homeState.bonuses.isNotEmpty()) {
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

                        item {
                            BalanceHeader(
                                onClickedAddAccount = homeClickFunctions.onClickedAddNewAccount, homeState.accounts.isNotEmpty()
                            )

                            if (accounts.isEmpty()) {
                                EmptyBalance(onClickedAddAccount = homeClickFunctions.onClickedAddNewAccount)
                            }
                        }

                        items(accounts){ account ->
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
                                FinancialTipCard(
                                    tipInterface = tipInterface,
                                    financialTip = homeState.financialTips.first()
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
                    TopBar(title = R.string.nav_home, isInternetConnected = false) {}
                },
                content = {
                    LazyColumn(content = {
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
                            FinancialTipCard(tipInterface = null, financialTip = financialTip)
                        }
                        item {
                            BalanceScreenPreview()
                        }

                    })
                })
        }
    }
}