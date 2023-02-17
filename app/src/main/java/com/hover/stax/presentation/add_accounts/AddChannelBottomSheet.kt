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
import com.hover.stax.addChannels.AddAccountViewModel
import com.hover.stax.channels.Channel
import com.hover.stax.domain.model.USSDAccount
import com.hover.stax.domain.use_case.sims.SimWithAccount
import com.hover.stax.presentation.add_accounts.components.SampleChannelProvider
import com.hover.stax.presentation.components.Logo
import com.hover.stax.presentation.components.PrimaryButton
import com.hover.stax.presentation.components.SecondaryButton
import com.hover.stax.presentation.components.SimTitle
import org.koin.androidx.compose.getViewModel

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun AddChannelBottomSheet(channel: Channel?, addAccountViewModel: AddAccountViewModel = getViewModel()) {
	val simChannels by addAccountViewModel.simChannels.observeAsState(initial = emptyList())

	Column(modifier = Modifier.fillMaxWidth().padding(bottom = 1.dp).padding(horizontal = 13.dp), Arrangement.Bottom) {
		if (channel != null) {
			SheetHeader(channel.logoUrl, getTitle(channel))
			Text(text = stringResource(id = R.string.link_to_sim),
				modifier = Modifier.fillMaxWidth().padding(vertical = 13.dp))

			val simChannel = chooseSim(simChannels, channel)
			SimTitle(simChannel.first, simChannel.second) {}

			Column(modifier = Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
				Text(text = stringResource(id = R.string.ask_check_balance),
					modifier = Modifier.fillMaxWidth().padding(vertical = 13.dp))
				Row(modifier = Modifier.fillMaxWidth().padding(vertical = 13.dp),
					Arrangement.SpaceBetween
				) {
					SecondaryButton(text = stringResource(R.string.skip_balance_btn)) {
						addAccountViewModel.createAccountWithoutBalance(channel)
					}
					PrimaryButton(text = stringResource(R.string.check_balance_btn)) {
						addAccountViewModel.balanceCheck(channel)
					}
				}
			}
		}
	}
}

@Composable
fun getTitle(channel: Channel?): String {
	return if (channel == null) {
		stringResource(R.string.loading_human)
	} else {
		stringResource(R.string.link_x, channel.name) }
}

fun chooseSim(simChannelList: List<Pair<SimInfo, Channel?>>, chosenChannel: Channel): Pair<SimInfo, Channel?> {
//	if (account.simSubscriptionId != -1 && simChannelList.map { it.first.subscriptionId }.contains(account.simSubscriptionId)) {
//		return simChannelList.find { it.first.subscriptionId == account.simSubscriptionId }!!
//	} else {
		simChannelList.forEach {
			if (chosenChannel.hniList.contains(it.first.osReportedHni)) {
				return it
			}
		}
		return simChannelList[0]
//	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SheetHeader(logo: String, title: String) {
	Column(modifier = Modifier.fillMaxWidth()) {
		CenterAlignedTopAppBar(
			navigationIcon = { Logo(logo, "$title logo") },
			title = {
				Text(text = title, style = MaterialTheme.typography.h1) },
			colors = StaxTopBarDefaults(),
		)
	}
}

@Preview
@Composable
fun AddChannelScreenPreview(@PreviewParameter(SampleChannelProvider::class) channels: List<Channel>) {
	AddChannelBottomSheet(channels[0])
}