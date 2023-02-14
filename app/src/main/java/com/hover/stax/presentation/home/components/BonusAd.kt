package com.hover.stax.presentation.home.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.navigation.NavController
import com.hover.sdk.actions.HoverAction
import com.hover.stax.presentation.home.HomeViewModel

@Composable
fun BonusAd(homeViewModel: HomeViewModel, navController: NavController) {

	val bonusActions by homeViewModel.bonusActions.observeAsState(initial = emptyList())

	if (!bonusActions.isNullOrEmpty()) {
		BonusCard(
			message = bonusActions.first().bonus_message,
			onClickedTC = { //* homeClickFunctions.onClickedTC //*
			},
			onClickedTopUp = {
				clickedOnBonus(bonusActions.first(), homeViewModel, navController)
			}
		)
	}
}

private fun clickedOnBonus(bonus: HoverAction, homeViewModel: HomeViewModel, navController: NavController) {
	homeViewModel.logBuyAirtimeFromAd()
//	navController.buyAirtimeFromAd(bonus.channel_id)
}