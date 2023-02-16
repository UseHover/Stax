package com.hover.stax.presentation.add_accounts

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hover.stax.R
import com.hover.stax.addChannels.AddAccountViewModel
import com.hover.stax.presentation.components.PrimaryButton
import com.hover.stax.presentation.components.SecondaryButton
import org.koin.androidx.compose.getViewModel

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun AccountCreatedScreen(channelId: Int?, addAccountViewModel: AddAccountViewModel = getViewModel()) {
	val channel by addAccountViewModel.chosenChannel.collectAsState(initial = null)
	addAccountViewModel.loadChannel(channelId!!)

	val simList by addAccountViewModel.sims.observeAsState(initial = emptyList())

	Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
		Scaffold(
			topBar = { TopBar(getTitle(channel)) },
		) {
			if (channel != null) {
				Column(modifier = Modifier.fillMaxWidth().padding(bottom = 13.dp).padding(horizontal = 13.dp), Arrangement.SpaceBetween) {
					Text(text = stringResource(id = R.string.link_explain))
					Text(text = stringResource(id = R.string.link_to_sim))

					Text(text = stringResource(id = R.string.ask_check_balance))
					Row() {
						SecondaryButton(text = stringResource(R.string.skip_balance_btn)) {
							addAccountViewModel.createAccountWithoutBalance(channel!!)
						}
						PrimaryButton(text = stringResource(R.string.check_balance_btn)) {
							addAccountViewModel.balanceCheck(channel!!)
						}
					}
				}
			}
		}
	}
}