package com.hover.stax.presentation.send_money

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.hover.stax.accounts.AccountsViewModel
import com.hover.stax.ui.theme.StaxTheme
import org.koin.androidx.compose.getViewModel

@Composable
fun SendMoneyNavHost(
    navController: NavHostController = rememberNavController(),
    startDestination: String = "sendMoney",
    viewmodel: AccountsViewModel = getViewModel()
) {
    StaxTheme {
        NavHost(
            navController = navController,
            startDestination = startDestination
        ) {
            composable("sendMoney") {
                SendMoneyScreen(viewmodel, navController)
            }
            composable("sendMoneyTransaction") {
                SendMoneyTransactionScreen(viewmodel, navController)
            }
        }
    }
}