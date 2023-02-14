package com.hover.stax.presentation.add_accounts

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.*
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.hover.stax.R
import com.hover.stax.addChannels.UsdcViewModel
import com.hover.stax.ui.theme.BrightBlue
import com.hover.stax.ui.theme.OffWhite
import org.koin.androidx.compose.getViewModel

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun UsdcAccountSummaryScreen(viewModel: UsdcViewModel = getViewModel(), navController: NavController) {
	val accounts by viewModel.accounts.observeAsState(initial = emptyList())
	viewModel.createAccount()

	Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
		Scaffold(
			topBar = { TopBar(accounts.isEmpty()) },
		) {
			if (accounts.isNotEmpty()) {
				Text("Created something woot!")
			}
		}
	}
}

	@OptIn(ExperimentalMaterial3Api::class)
	@Composable
	fun TopBar(loading: Boolean) {
		Column(modifier = Modifier.fillMaxWidth()) {
			CenterAlignedTopAppBar(
				title = { Text(text = stringResource(if (loading) R.string.loading_human else R.string.create_account_success), fontSize = 18.sp) },
				navigationIcon = {
					if (!loading) {
						IconButton(content = {
							Icon(
								painterResource(R.drawable.ic_close),
								contentDescription = "back",
								tint = OffWhite
							)
						},
							onClick = { })
					}
				},
				colors = StaxTopBarDefaults()
			)
		}
	}
