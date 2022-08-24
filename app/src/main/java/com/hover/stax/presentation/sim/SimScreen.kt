package com.hover.stax.presentation.sim

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.text.HtmlCompat
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.hover.sdk.permissions.PermissionHelper
import com.hover.stax.R
import com.hover.stax.domain.model.Account
import com.hover.stax.permissions.PermissionUtils
import com.hover.stax.presentation.home.BalanceTapListener
import com.hover.stax.presentation.home.TopBar
import com.hover.stax.ui.theme.*
import com.hover.stax.utils.DateUtils
import com.hover.stax.utils.Utils
import com.hover.stax.utils.network.NetworkMonitor
import org.koin.androidx.compose.getViewModel

data class SimScreenClickFunctions(
    val onClickedAddNewAccount: () -> Unit,
    val onClickedSettingsIcon: () -> Unit,
    val onClickedBuyAirtime: () -> Unit
)

private fun hasNotGratedSimPermission(context: Context) = !PermissionUtils.hasContactPermission(context)
                    ||
        !PermissionUtils.hasSmsPermission(context)

@Composable
fun SimScreen(
    simScreenClickFunctions: SimScreenClickFunctions,
    balanceTapListener: BalanceTapListener,
    simViewModel: SimViewModel = getViewModel()
) {
    val simUiState by simViewModel.simUiState.collectAsState()
    val hasNetwork by NetworkMonitor.StateLiveData.get().observeAsState(initial = false)

    val loading = simUiState.loading
    val context = LocalContext.current

    StaxTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
            Scaffold(
                topBar = {
                    TopBar(
                        title = R.string.nav_sim,
                        isInternetConnected = hasNetwork,
                        simScreenClickFunctions.onClickedSettingsIcon
                    )
                },
                content = { innerPadding ->
                    val paddingModifier = Modifier.padding(innerPadding)

                    LazyColumn(
                        modifier = Modifier
                            .padding(horizontal = dimensionResource(id = R.dimen.margin_13))
                            .then(paddingModifier)
                    ) {

                        item {
                            PageTitle()
                        }

                        if(loading) {
                            item {
                                NoticeText(stringRes = R.string.loading)
                            }
                        }

                       else if (hasNotGratedSimPermission(context)) {
                            item {
                                NoticeText(stringRes = R.string.simpage_permission_alert)
                            }

                            item {
                                LinkSimCard(
                                    id = R.string.link_sim_to_stax,
                                    onClickedLinkSimCard = simScreenClickFunctions.onClickedAddNewAccount
                                )
                            }
                        }

                        else if(simUiState.presentSims.isEmpty()) {
                            item {
                                NoticeText(stringRes = R.string.simpage_empty_sims)
                            }
                        }

                        else {
                            items(simUiState.presentSims) { presentSim ->
                                val simAccount = simUiState.telecomAccounts.find { it.subscriptionId == presentSim.subscriptionId }
                                val visibleSlotIdx = presentSim.slotIdx + 1

                                if (simAccount != null) {
                                    val bonus = simUiState.bonuses.firstOrNull()
                                    SimItem(
                                        simIndex = visibleSlotIdx,
                                        account = simAccount,
                                        bonus = ((bonus?.bonusPercent ?: 0.0) * 100).toInt(),
                                        onClickedBuyAirtime = simScreenClickFunctions.onClickedBuyAirtime,
                                        balanceTapListener = balanceTapListener
                                    )
                                } else {
                                    UnSupportedSim(networkName = presentSim.networkOperatorName,
                                        slotId = visibleSlotIdx,
                                        context = LocalContext.current)
                                }
                            }
                        }
                    }
                })
        }
    }
}

@Preview
@Composable
private fun SimScreenPreview() {
    val accounts = listOf(
        Account("Telecom").apply {
            id = 1
            subscriptionId = 1
            latestBalance = "NGN 200"
            latestBalanceTimestamp = 123
        },
        Account("Safaricom").apply {
            id = -1
            subscriptionId = -3
            latestBalance = "NGN 500"
            latestBalanceTimestamp = 2345
        }
    )

    StaxTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
            Scaffold(topBar = {
                TopBar(title = R.string.nav_home, isInternetConnected = false, {})
            }, content = { innerPadding ->
                val paddingModifier = Modifier.padding(innerPadding)

                LazyColumn(
                    modifier = Modifier
                        .padding(horizontal = dimensionResource(id = R.dimen.margin_13))
                        .then(paddingModifier)
                ) {
                    item {
                        PageTitle()
                    }

                    if (accounts.isEmpty())
                        item {
                            NoticeText(stringRes = R.string.simpage_permission_alert)
                        }

                    accounts.let {
                        if (it.isEmpty()) {
                            item {
                                LinkSimCard(id = R.string.link_sim_to_stax,
                                    onClickedLinkSimCard = { })
                            }
                        }
                    }

                    itemsIndexed(accounts) { index, account ->
                        if (account.id > 0) {
                            SimItem(
                                simIndex = 1,
                                account = account,
                                bonus = (0.05 * 100).toInt(),
                                onClickedBuyAirtime = { },
                                balanceTapListener = null
                            )
                        } else {
                            UnSupportedSim(networkName = account.name,
                                slotId = 2,
                                context = LocalContext.current)
                        }
                    }
                }
            })
        }
    }
}

@Composable
private fun PageTitle() {
    val size13 = dimensionResource(id = R.dimen.margin_13)
    Text(
        text = stringResource(id = R.string.your_linked_sim),
        modifier = Modifier.padding(vertical = size13),
        style = MaterialTheme.typography.button
    )
}

@Composable
private fun NoticeText(@StringRes stringRes: Int) {
    val size13 = dimensionResource(id = R.dimen.margin_13)
    val size34 = dimensionResource(id = R.dimen.margin_34)

    Text(
        text = stringResource(id = stringRes),
        modifier = Modifier
            .padding(vertical = size13, horizontal = size34)
            .fillMaxWidth(),
        textAlign = TextAlign.Center,
        color = TextGrey,
    )
}

@Composable
private fun UnSupportedSim(networkName:String, slotId: Int, context: Context) {
    val size13 = dimensionResource(id = R.dimen.margin_13)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = size13)
            .shadow(elevation = 0.dp)
            .border(width = 1.dp, color = DarkGray, shape = RoundedCornerShape(5.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(size13)
        ) {
            Text(
                text = HtmlCompat.fromHtml(stringResource( id = (R.string.unsupported_simcard_info), networkName, slotId),
                    HtmlCompat.FROM_HTML_MODE_COMPACT).toString(),
                modifier = Modifier.padding(all = size13),
                style = MaterialTheme.typography.body1
            )

            Button(
                onClick = { Utils.openUrl(R.string.stax_support_email_mailTo, context) },
                modifier = Modifier
                    .padding(top = size13)
                    .shadow(elevation = 0.dp),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = BrightBlue,
                    contentColor = ColorPrimary
                )
            ) {
                Text(
                    text = stringResource(id = R.string.email_support),
                    style = MaterialTheme.typography.body2,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }

        }
        }
}

@Composable
private fun SimItem(
    simIndex: Int,
    account: Account,
    bonus: Int,
    onClickedBuyAirtime: () -> Unit,
    balanceTapListener: BalanceTapListener?
) {

    val size13 = dimensionResource(id = R.dimen.margin_13)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = size13)
            .shadow(elevation = 0.dp)
            .border(width = 1.dp, color = DarkGray, shape = RoundedCornerShape(5.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(size13)
        ) {
            SimItemTopRow(
                simIndex = simIndex,
                account = account,
                balanceTapListener = balanceTapListener
            )
            Column {
                val notYetChecked = stringResource(id = R.string.not_yet_checked)
                Text(
                    text = stringResource(
                        id = R.string.airtime_balance_holder,
                        account.latestBalance ?: notYetChecked
                    ),
                    color = TextGrey,
                    modifier = Modifier.padding(top = size13),
                    style = MaterialTheme.typography.body1
                )

                Text(
                    text = stringResource(
                        id = R.string.as_of,
                        DateUtils.humanFriendlyDateTime(account.latestBalanceTimestamp)
                    ),
                    color = TextGrey,
                    modifier = Modifier.padding(bottom = 26.dp),
                    style = MaterialTheme.typography.body1
                )

                OutlinedButton(
                    onClick = onClickedBuyAirtime,
                    modifier = Modifier
                        .padding(bottom = 6.dp)
                        .shadow(elevation = 0.dp)
                        .wrapContentWidth(),
                    shape = MaterialTheme.shapes.medium,
                    border = BorderStroke(width = 0.5.dp, color = DarkGray),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = ColorSurface,
                        contentColor = OffWhite
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .wrapContentWidth()
                            .padding(all = 5.dp)
                    ) {
                        var buyAirtimeLabel = stringResource(id = R.string.nav_airtime)
                        if (bonus > 0) {
                            val bonusPercent = bonus.toString().plus("%")
                            buyAirtimeLabel =
                                stringResource(
                                    id = R.string.buy_airitme_with_discount,
                                    bonusPercent
                                )
                        }
                        Text(
                            text = buyAirtimeLabel,
                            style = MaterialTheme.typography.button,
                            modifier = Modifier.padding(end = 5.dp),
                            textAlign = TextAlign.Start,
                            fontSize = 14.sp
                        )

                        Spacer(modifier = Modifier.width(3.dp))

                        Image(
                            painter = painterResource(id = R.drawable.ic_bonus),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SimItemTopRow(simIndex: Int, account: Account, balanceTapListener: BalanceTapListener?) {
    val size34 = dimensionResource(id = R.dimen.margin_34)

    Row {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current).data(account.logoUrl)
                .crossfade(true).diskCachePolicy(CachePolicy.ENABLED).build(),
            contentDescription = "",
            placeholder = painterResource(id = R.drawable.img_placeholder),
            error = painterResource(id = R.drawable.img_placeholder),
            modifier = Modifier
                .size(size34)
                .clip(CircleShape)
                .align(Alignment.CenterVertically),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 13.dp)
        ) {
            Text(text = account.name, style = MaterialTheme.typography.body1)
            Text(
                text = stringResource(id = R.string.sim_index, simIndex),
                color = TextGrey,
                style = MaterialTheme.typography.body2
            )
        }

        Button(
            onClick = { balanceTapListener?.onTapBalanceRefresh(account) },
            modifier = Modifier
                .weight(1f)
                .shadow(elevation = 0.dp),
            shape = MaterialTheme.shapes.medium,
            colors = ButtonDefaults.buttonColors(
                backgroundColor = BrightBlue,
                contentColor = ColorPrimary
            )
        ) {
            Text(
                text = stringResource(id = R.string.check_balance_capitalized),
                style = MaterialTheme.typography.body2,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun LinkSimCard(@StringRes id: Int, onClickedLinkSimCard: () -> Unit, stringArg: String = "") {
    OutlinedButton(
        onClick = onClickedLinkSimCard,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 13.dp)
            .shadow(elevation = 0.dp),
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(width = 0.5.dp, color = DarkGray),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = ColorSurface,
            contentColor = OffWhite
        )
    ) {
        Text(
            text = stringResource(id = id, stringArg),
            style = MaterialTheme.typography.button,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 5.dp, bottom = 5.dp),
            textAlign = TextAlign.Center
        )
    }
}
