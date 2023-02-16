package com.hover.stax.presentation.add_accounts

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.hover.stax.R
import com.hover.stax.addChannels.UsdcViewModel
import com.hover.stax.presentation.components.PrimaryButton
import com.hover.stax.presentation.components.TallTopBar
import org.koin.androidx.compose.getViewModel

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun UsdcAccountSummaryScreen(viewModel: UsdcViewModel = getViewModel()) {
	val accounts by viewModel.accounts.observeAsState(initial = emptyList())

	Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
		Scaffold(
			topBar = { TallTopBar(getTitle(accounts.isEmpty()), null) },
		) {
			if (accounts.isNotEmpty()) {
				Column(modifier = Modifier.fillMaxSize().padding(34.dp), Arrangement.SpaceAround) {
					Column(modifier = Modifier.fillMaxWidth().weight(1f)) {
						Text(
							text = stringResource(id = R.string.usdc_account_1),
							modifier = Modifier.fillMaxWidth().padding(vertical = 13.dp),
							style = MaterialTheme.typography.subtitle1
						)
						Text(
							text = stringResource(id = R.string.usdc_account_1_explain),
							modifier = Modifier.fillMaxWidth().padding(vertical = 13.dp),
							style = MaterialTheme.typography.body1
						)
						PrimaryButton(text = stringResource(R.string.usdc_account_1_action)) {

						}
					}
					Column(modifier = Modifier.fillMaxWidth().weight(1f)) {
						Text(
							text = stringResource(id = R.string.usdc_bullet_2),
							modifier = Modifier.fillMaxWidth().padding(vertical = 13.dp),
							style = MaterialTheme.typography.subtitle1
						)
						Text(
							text = stringResource(id = R.string.usdc_account_2_explain),
							modifier = Modifier.fillMaxWidth().padding(vertical = 13.dp),
							style = MaterialTheme.typography.body1
						)
						PrimaryButton(text = stringResource(R.string.usdc_account_2_action)) {

						}
					}
				}
			}
		}
	}
}

@Composable
fun getTitle(loading: Boolean): String {
	return stringResource(if (loading) R.string.loading_human else R.string.create_account_success)
}

@Preview
@Composable
fun UsdcAccountSummaryScreenPreview() {
	UsdcAccountSummaryScreen()
}