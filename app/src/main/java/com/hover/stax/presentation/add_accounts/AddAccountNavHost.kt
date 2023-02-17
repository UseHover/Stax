package com.hover.stax.presentation.add_accounts

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.hover.stax.addChannels.AddAccountViewModel
import com.hover.stax.addChannels.UsdcViewModel
import com.hover.stax.ui.theme.StaxTheme
import org.koin.androidx.compose.getViewModel

@ExperimentalAnimationApi
@Composable
fun AddAccountNavHost(
	navController: NavHostController = rememberNavController(),
	startDestination: String = "findChannel",
	addAccountViewModel: AddAccountViewModel = getViewModel(),
	usdcViewModel: UsdcViewModel = getViewModel()
) {
	StaxTheme {
		NavHost(
			navController = navController,
			startDestination = startDestination
		) {
			composable("findChannel") {
				ChooseChannelScreen(addAccountViewModel, navController)
			}
			composable("createUSDC") {
				CreateUsdcAccountScreen(usdcViewModel, navController)
			}
		}
	}
}