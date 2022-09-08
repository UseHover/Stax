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
import com.hover.sdk.sims.SimInfo
import com.hover.stax.R
import com.hover.stax.domain.model.Account
import com.hover.stax.domain.model.Bonus
import com.hover.stax.permissions.PermissionUtils
import com.hover.stax.presentation.home.BalanceTapListener
import com.hover.stax.presentation.home.components.TopBar
import com.hover.stax.presentation.sim.components.LinkSimCard
import com.hover.stax.presentation.sim.components.SimItem
import com.hover.stax.ui.theme.*
import com.hover.stax.utils.DateUtils
import com.hover.stax.utils.Utils
import com.hover.stax.utils.isAbsolutelyEmpty
import com.hover.stax.utils.network.NetworkMonitor
import org.koin.androidx.compose.getViewModel
import timber.log.Timber

data class SimScreenClickFunctions(
    val onClickedAddNewAccount: () -> Unit,
    val onClickedSettingsIcon: () -> Unit,
    val onClickedBuyAirtime: () -> Unit
)

private fun hasGratedSimPermission(context: Context) = PermissionUtils.hasContactPermission(context) && PermissionUtils.hasSmsPermission(context)

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
                        simScreenClickFunctions.onClickedSettingsIcon,
                        {}
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
                            Timber.i("Status: Loading")
                            item {
                                NoticeText(stringRes = R.string.loading)
                            }
                        }

                        else if(simUiState.presentSims.isEmpty()) {
                            Timber.i("Status: Detected empty sims")
                            item {
                                if(hasGratedSimPermission(context)) NoticeText(stringRes = R.string.simpage_empty_sims)
                                else ShowGrantPermissionContent(simScreenClickFunctions.onClickedAddNewAccount)
                            }
                        }

                        else {
                            items(simUiState.presentSims) { presentSim ->
                                val simAccount = simUiState.telecomAccounts.find { it.simSubscriptionId == presentSim.subscriptionId }
                                val visibleSlotIdx = presentSim.slotIdx + 1

                                if (simAccount != null) {
                                    val bonus = getSimBonusPercent(simUiState.bonuses, presentSim.osReportedHni)
                                    SimItem(
                                        simIndex = visibleSlotIdx,
                                        account = simAccount,
                                        bonusPercent = ((bonus?.bonusPercent ?: 0.0) * 100).toInt(),
                                        secondaryClickItem = simScreenClickFunctions.onClickedBuyAirtime,
                                        balanceTapListener = balanceTapListener
                                    )
                                } else {
                                    UnSupportedSim(simInfo = presentSim, slotId = visibleSlotIdx, context = LocalContext.current )
                                }
                            }
                        }
                    }
                })
        }
    }
}

private fun getSimBonusPercent(bonuses: List<Bonus>, simHni: String) : Bonus? {
    return bonuses.find { it.hniList.contains(simHni) }
}

@Preview
@Composable
private fun SimScreenPreview() {
    val accounts = listOf(
        Account.generateDummy(),
        Account.generateDummy("Safaricom", -1))

    StaxTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
            Scaffold(topBar = {
                TopBar(title = R.string.nav_home, isInternetConnected = false, {}, {})
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
                                bonusPercent = (0.05 * 100).toInt(),
                                secondaryClickItem = { },
                                balanceTapListener = null
                            )
                        } else {
                            UnSupportedSim(simInfo = null,
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
        text = stringResource(id = R.string.your_sim_cards),
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
private fun ShowGrantPermissionContent( onClickedAddNewAccount: () -> Unit ) {
    Column {
        NoticeText(stringRes = R.string.simpage_permission_alert)
        LinkSimCard(
            id = R.string.link_sim_to_stax,
            onClickedLinkSimCard = onClickedAddNewAccount
        )
    }
}

@Composable
private fun UnSupportedSim(simInfo: SimInfo?, slotId: Int, context: Context) {
    var displayedNetworkName = ""
    var emailBody = ""
    simInfo?.let {
        displayedNetworkName = if(it.operatorName.isAbsolutelyEmpty() || it.operatorName.contains("No service"))
            stringResource(id = R.string.unknown)
        else it.operatorName

        emailBody = stringResource(id = R.string.sim_card_support_request_emailBody,
            it.osReportedHni ?: "Null",
            it.operatorName ?: "Null",
            it.networkOperator ?: "Null",
            it.countryIso ?: "Null")
    }

    SimItem(
        simIndex = slotId,
        account = Account.generateDummy(displayedNetworkName),
        bonusPercent = 0,
        secondaryClickItem = { Utils.openEmail(R.string.sim_card_support_request_emailSubject, context, emailBody) },
        balanceTapListener = null
    )

}




