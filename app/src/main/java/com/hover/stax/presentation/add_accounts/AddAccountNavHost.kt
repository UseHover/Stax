package com.hover.stax.presentation.add_accounts

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.hover.stax.addChannels.ChannelsViewModel
import com.hover.stax.addChannels.UsdcViewModel
import com.hover.stax.ui.theme.StaxTheme
import org.koin.androidx.compose.getViewModel

@Composable
fun AddAccountNavHost(
	navController: NavHostController = rememberNavController(),
	startDestination: String = "findChannel",
	channelsViewModel: ChannelsViewModel = getViewModel(),
	usdcViewModel: UsdcViewModel = getViewModel()
) {
	StaxTheme {
		NavHost(
			navController = navController,
			startDestination = startDestination
		) {
			composable("findChannel") {
				ChooseChannelScreen(channelsViewModel, navController)
			}
			composable("addChannel") {
				AddChannelScreen(channelsViewModel, navController)
			}
			composable("addUSDC") {
				AddUsdcScreen(usdcViewModel, navController)
			}
		}
	}
}