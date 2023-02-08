package com.hover.stax.presentation.add_accounts

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.NavController
import com.hover.stax.addChannels.UsdcViewModel
import org.koin.androidx.compose.getViewModel

@Composable
fun AddUsdcScreen(viewModel: UsdcViewModel = getViewModel(), navController: NavController) {

	val showingHelp = remember { mutableStateOf(false) }

	viewModel.createAccount()

//	Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
//		Scaffold(
//			topBar = { TopBar(showingHelp) }
//		)
//	}
}