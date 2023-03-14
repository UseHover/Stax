package com.hover.stax.presentation.add_accounts

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.hover.sdk.sims.SimInfo
import com.hover.stax.R
import com.hover.stax.addAccounts.AddAccountViewModel
import com.hover.stax.channels.Channel
import com.hover.stax.presentation.add_accounts.components.SampleChannelProvider
import com.hover.stax.presentation.components.*
import com.hover.stax.ui.theme.StaxStateRed
import org.koin.androidx.compose.getViewModel

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun AddChannelBottomSheet(channel: Channel?, addAccountViewModel: AddAccountViewModel = getViewModel()) {
    val simChannels by addAccountViewModel.simChannels.observeAsState(initial = emptyList())

    val error = remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 1.dp)
            .padding(horizontal = 13.dp),
        Arrangement.Bottom
    ) {
        if (channel != null) {
            val simChannel = chooseSim(simChannels, channel, error)

            SheetHeader(channel.logoUrl, getTitle(channel))
            Column(modifier = Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                if (error.value.isEmpty()) {
                    SimPresentDetails(simChannel, channel, addAccountViewModel)
                } else {
                    SimNotPresentDetails(simChannels, channel, error, addAccountViewModel)
                }
            }
        }
    }
}

@Composable
fun SimPresentDetails(simChannel: Pair<SimInfo, Channel?>, channel: Channel, addAccountViewModel: AddAccountViewModel) {
    val startingBalanceCheck = remember { mutableStateOf(false) }

    Text(
        text = stringResource(id = R.string.link_to_sim),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 13.dp)
    )

    SimTitle(simChannel.first, simChannel.second) {}

    Text(
        text = stringResource(id = R.string.ask_check_balance),
        modifier = Modifier.fillMaxWidth().padding(vertical = 13.dp)
    )

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 13.dp),
        Arrangement.SpaceBetween
    ) {
        SecondaryButton(text = stringResource(R.string.skip_balance_btn)) {
            addAccountViewModel.createAccountWithoutBalance(channel)
        }
        StaxButton(
            text = stringResource(R.string.check_balance_btn), icon = null,
            buttonType = if (startingBalanceCheck.value) DISABLED else PRIMARY
        ) {
            startingBalanceCheck.value = true
            addAccountViewModel.balanceCheck(channel)
        }
    }
}

@Composable
fun SimNotPresentDetails(simChannels: List<Pair<SimInfo, Channel?>>, channel: Channel, error: MutableState<String>, addAccountViewModel: AddAccountViewModel) {
    Text(
        text = error.value,
        modifier = Modifier.fillMaxWidth().padding(vertical = 13.dp),
        color = StaxStateRed
    )

    Text(
        text = stringResource(id = R.string.link_sim_error_details),
        modifier = Modifier.fillMaxWidth().padding(vertical = 13.dp)
    )

    Text(
        text = stringResource(id = R.string.sims_detected),
        modifier = Modifier.fillMaxWidth().padding(vertical = 13.dp)
    )

    simChannels.forEach {
        SimTitle(it.first, it.second) {}
    }

    SecondaryButton(
        text = stringResource(R.string.save_without_sim_btn),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 55.dp).padding(vertical = 13.dp)
    ) {
        addAccountViewModel.createAccountWithoutBalance(channel)
    }
}

@Composable
fun getTitle(channel: Channel?): String {
    return if (channel == null) {
        stringResource(R.string.loading_human)
    } else {
        stringResource(R.string.link_x, channel.name)
    }
}

@Composable
fun chooseSim(
    simChannelList: List<Pair<SimInfo, Channel?>>,
    chosenChannel: Channel,
    error: MutableState<String>
): Pair<SimInfo, Channel?> {
// 	if (account.simSubscriptionId != -1 && simChannelList.map { it.first.subscriptionId }.contains(account.simSubscriptionId)) {
// 		return simChannelList.find { it.first.subscriptionId == account.simSubscriptionId }!!
// 	} else {
    simChannelList.forEach {
        if (chosenChannel.hniList.contains(it.first.osReportedHni)) {
            error.value = ""
            return it
        }
    }
    error.value = stringResource(id = R.string.link_sim_error)
    return simChannelList[0]
// 	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SheetHeader(logo: String, title: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        CenterAlignedTopAppBar(
            navigationIcon = { Logo(logo, "$title logo") },
            title = {
                Text(text = title, style = MaterialTheme.typography.h1)
            },
            colors = StaxTopBarDefaults(),
        )
    }
}

@Preview
@Composable
fun AddChannelScreenPreview(@PreviewParameter(SampleChannelProvider::class) channels: List<Channel>) {
    AddChannelBottomSheet(channels[0])
}