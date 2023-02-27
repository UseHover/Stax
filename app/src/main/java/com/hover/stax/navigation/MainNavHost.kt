package com.hover.stax.navigation

import androidx.compose.material.ModalBottomSheetDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.accompanist.navigation.material.ModalBottomSheetLayout
import com.google.accompanist.navigation.material.bottomSheet
import com.google.accompanist.navigation.material.rememberBottomSheetNavigator
import com.hover.stax.accounts.AccountsViewModel
import com.hover.stax.send.PayWithScreen
import com.hover.stax.send.PaymentTypeScreen
import com.hover.stax.send.SendMoneyScreen
import org.koin.androidx.compose.koinViewModel
import timber.log.Timber

@OptIn(ExperimentalMaterialNavigationApi::class)
@Composable
fun MainNavHost() {

    val bottomSheetNavigator = rememberBottomSheetNavigator()
    val bottomSheetConfig = remember { mutableStateOf(DefaultBottomSheetConfig) }
    val navController = rememberNavController(bottomSheetNavigator)

    val accountsViewModel: AccountsViewModel = koinViewModel()

    val accountList by accountsViewModel.accountList.collectAsStateWithLifecycle()
    val actionList by accountsViewModel.institutionActions.observeAsState()

    ModalBottomSheetLayout(
        bottomSheetNavigator = bottomSheetNavigator,
        sheetShape = bottomSheetConfig.value.sheetShape,
        scrimColor = if (bottomSheetConfig.value.showScrim) {
            ModalBottomSheetDefaults.scrimColor
        } else {
            Color.Transparent
        }
    ) {
        NavHost(
            navController = navController,
            startDestination = PaymentScreens.SendMoney.route
        ) {

            composable(route = PaymentScreens.SendMoney.route) {
                SendMoneyScreen(navTo = {
                    navigate(
                        PaymentScreens.PaymentTypeScreen.route,
                        navController
                    )
                })
            }

            bottomSheet(PaymentScreens.PayWithScreen.route) {
                bottomSheetConfig.value = DefaultBottomSheetConfig
                PayWithScreen(
                    onClickBack = { navController.navigateUp() },
                    accounts = accountList.accounts,
                    onAccountSelected = { account ->
                        Timber.d("Selected account: ${account.id}")
                        navigateWithPop(
                            PaymentScreens.SendMoney.route,
                            navController
                        )
                    }
                )
            }

            bottomSheet(PaymentScreens.PaymentTypeScreen.route) {
                bottomSheetConfig.value = DefaultBottomSheetConfig
                PaymentTypeScreen(
                    onClickBack = { navController.navigateUp() },
                    actions = actionList,
                    onActionSelected = { action ->
                        Timber.d("Selected action: ${action.id}")
                    }
                )
            }
        }
    }
}

fun navigate(route: String, navController: NavController) {
    navController.navigate(route)
}

fun navigateWithPop(route: String, navController: NavController) {
    navController.navigate(route) {
        popUpTo(PaymentScreens.SendMoney.route) { inclusive = true }
    }
}